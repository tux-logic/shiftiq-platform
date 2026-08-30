package com.andeva.atelier.platform.iot.interfaces.rest.transform;

import com.andeva.atelier.platform.iot.domain.model.aggregates.Obd2DeviceRegistration;
import com.andeva.atelier.platform.iot.interfaces.rest.resources.Obd2DeviceRegistrationResource;

/**
 * Assembler to translate Obd2DeviceRegistration domain aggregate to Obd2DeviceRegistrationResource DTO.
 */
public class Obd2DeviceRegistrationResourceFromAggregateAssembler {
    public static Obd2DeviceRegistrationResource toResourceFromAggregate(Obd2DeviceRegistration aggregate) {
        return new Obd2DeviceRegistrationResource(
                aggregate.getId() != null ? aggregate.getId().value() : null,
                aggregate.getObd2DeviceId() != null ? aggregate.getObd2DeviceId().value() : null,
                aggregate.getBranchId() != null ? aggregate.getBranchId().value() : null,
                aggregate.getVehicleId() != null ? aggregate.getVehicleId().value() : null,
                aggregate.getStatus() != null ? aggregate.getStatus().value() : null,
                aggregate.getCreatedAt()
        );
    }
}
