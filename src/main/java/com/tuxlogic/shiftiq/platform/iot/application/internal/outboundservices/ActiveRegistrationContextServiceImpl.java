package com.tuxlogic.shiftiq.platform.iot.application.internal.outboundservices;

import com.tuxlogic.shiftiq.platform.iot.domain.model.valueobjects.ActiveRegistrationContext;
import com.tuxlogic.shiftiq.platform.iot.domain.repositories.Obd2DeviceRegistrationRepository;
import com.tuxlogic.shiftiq.platform.iot.domain.repositories.VehicleRegistrationRepository;
import com.tuxlogic.shiftiq.platform.iot.domain.services.ActiveRegistrationContextService;
import com.tuxlogic.shiftiq.platform.shared.domain.model.valueobjects.VehicleId;
import org.springframework.stereotype.Service;

import java.util.Optional;

/**
 * Service implementation for resolving active registration context of a vehicle.
 */
@Service
public class ActiveRegistrationContextServiceImpl implements ActiveRegistrationContextService {

    private final VehicleRegistrationRepository vehicleRegistrationRepository;
    private final Obd2DeviceRegistrationRepository obd2DeviceRegistrationRepository;

    public ActiveRegistrationContextServiceImpl(
            VehicleRegistrationRepository vehicleRegistrationRepository,
            Obd2DeviceRegistrationRepository obd2DeviceRegistrationRepository
    ) {
        this.vehicleRegistrationRepository = vehicleRegistrationRepository;
        this.obd2DeviceRegistrationRepository = obd2DeviceRegistrationRepository;
    }

    @Override
    public Optional<ActiveRegistrationContext> resolveActiveContextForVehicle(VehicleId vehicleId) {
        var activeVehicleRegOpt = vehicleRegistrationRepository.findActiveByVehicleId(vehicleId);
        if (activeVehicleRegOpt.isEmpty()) {
            return Optional.empty();
        }
        var startTimestamp = activeVehicleRegOpt.get().getCreatedAt();

        var activeObd2RegOpt = obd2DeviceRegistrationRepository.findActiveByVehicleId(vehicleId);
        if (activeObd2RegOpt.isEmpty()) {
            return Optional.empty();
        }

        return Optional.of(new ActiveRegistrationContext(activeObd2RegOpt.get().getId(), startTimestamp));
    }
}
