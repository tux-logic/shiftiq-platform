package com.tuxlogic.shiftiq.platform.iot.application.internal.queryservices;

import com.tuxlogic.shiftiq.platform.iot.application.queryservices.VehicleQueryService;
import com.tuxlogic.shiftiq.platform.iot.domain.model.aggregates.Vehicle;
import com.tuxlogic.shiftiq.platform.iot.domain.model.aggregates.VehicleRegistration;
import com.tuxlogic.shiftiq.platform.iot.domain.model.queries.GetActiveVehiclesByCustomerIdQuery;
import com.tuxlogic.shiftiq.platform.iot.domain.model.queries.GetVehiclesAvailableForLinkingQuery;
import com.tuxlogic.shiftiq.platform.iot.domain.model.queries.GetVehicleByIdQuery;
import com.tuxlogic.shiftiq.platform.iot.domain.repositories.Obd2DeviceRegistrationRepository;
import com.tuxlogic.shiftiq.platform.iot.domain.repositories.VehicleRegistrationRepository;
import com.tuxlogic.shiftiq.platform.iot.domain.repositories.VehicleRepository;
import com.tuxlogic.shiftiq.platform.iot.domain.services.CustomerDirectoryPort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

/**
 * Service implementation for handling Vehicle queries inside the iot context.
 */
@Service
public class VehicleQueryServiceImpl implements VehicleQueryService {

    private final VehicleRepository vehicleRepository;
    private final CustomerDirectoryPort customerDirectoryPort;
    private final VehicleRegistrationRepository vehicleRegistrationRepository;
    private final Obd2DeviceRegistrationRepository obd2DeviceRegistrationRepository;

    public VehicleQueryServiceImpl(
            VehicleRepository vehicleRepository,
            CustomerDirectoryPort customerDirectoryPort,
            VehicleRegistrationRepository vehicleRegistrationRepository,
            Obd2DeviceRegistrationRepository obd2DeviceRegistrationRepository
    ) {
        this.vehicleRepository = vehicleRepository;
        this.customerDirectoryPort = customerDirectoryPort;
        this.vehicleRegistrationRepository = vehicleRegistrationRepository;
        this.obd2DeviceRegistrationRepository = obd2DeviceRegistrationRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public List<Vehicle> handle(GetVehiclesAvailableForLinkingQuery query) {
        var customerIds = customerDirectoryPort.findActiveCustomerIdsByBranchId(query.branchId());
        if (customerIds.isEmpty()) {
            return List.of();
        }

        var userIds = customerDirectoryPort.findUserIdsByCustomerIds(customerIds);
        if (userIds.isEmpty()) {
            return List.of();
        }

        var activeVehicleRegistrations = vehicleRegistrationRepository.findAllActiveByUserIds(userIds);
        if (activeVehicleRegistrations.isEmpty()) {
            return List.of();
        }

        var vehicleIds = activeVehicleRegistrations.stream()
                .map(VehicleRegistration::getVehicleId)
                .distinct()
                .toList();

        var vehicles = vehicleRepository.findAllByIds(vehicleIds);
        if (vehicles.isEmpty()) {
            return List.of();
        }

        var linkedVehicleIds = obd2DeviceRegistrationRepository.findVehicleIdsWithActiveRegistration(vehicleIds);

        return vehicles.stream()
                .filter(vehicle -> !linkedVehicleIds.contains(vehicle.getId()))
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<Vehicle> handle(GetActiveVehiclesByCustomerIdQuery query) {
        var userIdOpt = customerDirectoryPort.findUserIdByCustomerId(query.customerId().value());
        if (userIdOpt.isEmpty()) {
            return List.of();
        }
        var activeRegistrations = vehicleRegistrationRepository.findAllActiveByUserId(userIdOpt.get());
        if (activeRegistrations.isEmpty()) {
            return List.of();
        }
        var vehicleIds = activeRegistrations.stream()
                .map(VehicleRegistration::getVehicleId)
                .distinct()
                .toList();
        return vehicleRepository.findAllByIds(vehicleIds);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Vehicle> handle(GetVehicleByIdQuery query) {
        return vehicleRepository.findById(query.vehicleId());
    }
}