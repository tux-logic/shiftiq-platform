package com.tuxlogic.shiftiq.platform.iot.application.internal.commandservices;

import com.tuxlogic.shiftiq.platform.iot.application.commandservices.VehicleCommandFailure;
import com.tuxlogic.shiftiq.platform.iot.application.commandservices.VehicleCommandService;
import com.tuxlogic.shiftiq.platform.iot.domain.model.aggregates.Obd2Device;
import com.tuxlogic.shiftiq.platform.iot.domain.model.aggregates.Obd2DeviceRegistration;
import com.tuxlogic.shiftiq.platform.iot.domain.model.aggregates.Vehicle;
import com.tuxlogic.shiftiq.platform.iot.domain.model.aggregates.VehicleRegistration;
import com.tuxlogic.shiftiq.platform.iot.domain.model.commands.DeleteVehicleCommand;
import com.tuxlogic.shiftiq.platform.iot.domain.model.commands.RegisterVehicleCommand;
import com.tuxlogic.shiftiq.platform.iot.domain.model.commands.UpdateVehicleCommand;
import com.tuxlogic.shiftiq.platform.iot.domain.repositories.Obd2DeviceRegistrationRepository;
import com.tuxlogic.shiftiq.platform.iot.domain.repositories.Obd2DeviceRepository;
import com.tuxlogic.shiftiq.platform.iot.domain.repositories.VehicleRegistrationRepository;
import com.tuxlogic.shiftiq.platform.iot.domain.repositories.VehicleRepository;
import com.tuxlogic.shiftiq.platform.shared.application.result.Result;
import com.tuxlogic.shiftiq.platform.shared.domain.model.valueobjects.VehicleId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

/**
 * Service implementation for handling Vehicle command operations.
 */
@Service
public class VehicleCommandServiceImpl implements VehicleCommandService {

    private static final Logger log = LoggerFactory.getLogger(VehicleCommandServiceImpl.class);

    private final VehicleRepository vehicleRepository;
    private final VehicleRegistrationRepository vehicleRegistrationRepository;
    private final Obd2DeviceRegistrationRepository obd2DeviceRegistrationRepository;
    private final Obd2DeviceRepository obd2DeviceRepository;

    public VehicleCommandServiceImpl(
            VehicleRepository vehicleRepository,
            VehicleRegistrationRepository vehicleRegistrationRepository,
            Obd2DeviceRegistrationRepository obd2DeviceRegistrationRepository,
            Obd2DeviceRepository obd2DeviceRepository
    ) {
        this.vehicleRepository = vehicleRepository;
        this.vehicleRegistrationRepository = vehicleRegistrationRepository;
        this.obd2DeviceRegistrationRepository = obd2DeviceRegistrationRepository;
        this.obd2DeviceRepository = obd2DeviceRepository;
    }

    @Override
    @Transactional
    public Result<VehicleRegistration, VehicleCommandFailure> handle(RegisterVehicleCommand command) {
        try {
            Optional<Vehicle> vehicleByVin = vehicleRepository.findByVin(command.vin());
            Optional<Vehicle> vehicleByPlate = vehicleRepository.findByPlateNumber(command.plateNumber());

            Vehicle vehicle;
            if (vehicleByVin.isPresent() && vehicleByPlate.isPresent()) {
                if (!vehicleByVin.get().getId().equals(vehicleByPlate.get().getId())) {
                    return Result.failure(new VehicleCommandFailure.Duplicate("iot.error.vehicle.conflict"));
                }
                vehicle = vehicleByVin.get();
            } else if (vehicleByVin.isPresent()) {
                vehicle = vehicleByVin.get();
            } else if (vehicleByPlate.isPresent()) {
                vehicle = vehicleByPlate.get();
            } else {
                vehicle = new Vehicle(
                        command.plateNumber(),
                        command.brand(),
                        command.model(),
                        command.year(),
                        command.vin()
                );
                vehicle = vehicleRepository.save(vehicle);
            }

            Optional<VehicleRegistration> activeRegistrationOpt = vehicleRegistrationRepository.findActiveByVehicleId(vehicle.getId());
            if (activeRegistrationOpt.isPresent()) {
                VehicleRegistration activeRegistration = activeRegistrationOpt.get();
                if (activeRegistration.getUserId().equals(command.userId())) {
                    return Result.success(activeRegistration);
                } else {
                    activeRegistration.deactivateRegistration();
                    vehicleRegistrationRepository.save(activeRegistration);
                }
            }

            VehicleRegistration newRegistration = new VehicleRegistration(command.userId(), vehicle.getId());
            VehicleRegistration savedRegistration = vehicleRegistrationRepository.save(newRegistration);

            return Result.success(savedRegistration);

        } catch (IllegalArgumentException e) {
            return Result.failure(new VehicleCommandFailure.InvalidState(e.getMessage()));
        } catch (Exception e) {
            log.error("Unexpected error while registering vehicle", e);
            return Result.failure(new VehicleCommandFailure.InvalidState("iot.error.vehicle.unexpected"));
        }
    }

    @Override
    @Transactional
    public Result<Vehicle, VehicleCommandFailure> handle(UpdateVehicleCommand command) {
        try {
            Optional<Vehicle> vehicleOpt = vehicleRepository.findById(new VehicleId(command.id()));
            if (vehicleOpt.isEmpty()) {
                return Result.failure(new VehicleCommandFailure.NotFound("iot.error.vehicle.notFound"));
            }
            Vehicle vehicle = vehicleOpt.get();

            Optional<Vehicle> vehicleByPlate = vehicleRepository.findByPlateNumber(command.plateNumber());
            if (vehicleByPlate.isPresent() && !vehicleByPlate.get().getId().equals(vehicle.getId())) {
                return Result.failure(new VehicleCommandFailure.Duplicate("iot.error.vehicle.conflict"));
            }

            Optional<Vehicle> vehicleByVin = vehicleRepository.findByVin(command.vin());
            if (vehicleByVin.isPresent() && !vehicleByVin.get().getId().equals(vehicle.getId())) {
                return Result.failure(new VehicleCommandFailure.Duplicate("iot.error.vehicle.conflict"));
            }

            vehicle.updateDetails(
                    command.plateNumber(),
                    command.brand(),
                    command.model(),
                    command.year(),
                    command.vin()
            );
            Vehicle updatedVehicle = vehicleRepository.save(vehicle);

            return Result.success(updatedVehicle);

        } catch (IllegalArgumentException e) {
            return Result.failure(new VehicleCommandFailure.InvalidState(e.getMessage()));
        } catch (Exception e) {
            log.error("Unexpected error while updating vehicle {}", command.id(), e);
            return Result.failure(new VehicleCommandFailure.InvalidState("iot.error.vehicle.unexpected"));
        }
    }

    @Override
    @Transactional
    public Result<Void, VehicleCommandFailure> handle(DeleteVehicleCommand command) {
        try {
            Optional<Vehicle> vehicleOpt = vehicleRepository.findById(command.vehicleId());
            if (vehicleOpt.isEmpty()) {
                return Result.failure(new VehicleCommandFailure.NotFound("iot.error.vehicle.notFound"));
            }

            Optional<VehicleRegistration> activeRegOpt = vehicleRegistrationRepository.findActiveByVehicleId(command.vehicleId());
            if (activeRegOpt.isPresent()) {
                VehicleRegistration activeReg = activeRegOpt.get();
                activeReg.deactivateRegistration();
                vehicleRegistrationRepository.save(activeReg);
            }

            Optional<Obd2DeviceRegistration> activeObd2RegOpt = obd2DeviceRegistrationRepository.findActiveByVehicleId(command.vehicleId());
            if (activeObd2RegOpt.isPresent()) {
                Obd2DeviceRegistration activeObd2Reg = activeObd2RegOpt.get();

                Optional<Obd2Device> obd2DeviceOpt = obd2DeviceRepository.findById(activeObd2Reg.getObd2DeviceId());
                if (obd2DeviceOpt.isPresent()) {
                    Obd2Device obd2Device = obd2DeviceOpt.get();
                    obd2Device.markAsAvailable();
                    obd2DeviceRepository.save(obd2Device);
                }

                activeObd2Reg.deactivate();
                obd2DeviceRegistrationRepository.save(activeObd2Reg);
            }

            vehicleRepository.delete(command.vehicleId());

            return Result.success(null);

        } catch (IllegalStateException e) {
            return Result.failure(new VehicleCommandFailure.InvalidState(e.getMessage()));
        } catch (Exception e) {
            log.error("Unexpected error while deleting vehicle {}", command.vehicleId(), e);
            return Result.failure(new VehicleCommandFailure.InvalidState("iot.error.vehicle.unexpected"));
        }
    }
}