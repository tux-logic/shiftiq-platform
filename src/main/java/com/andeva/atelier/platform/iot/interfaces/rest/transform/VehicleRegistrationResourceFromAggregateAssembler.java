package com.andeva.atelier.platform.iot.interfaces.rest.transform;

import com.andeva.atelier.platform.iot.domain.model.aggregates.VehicleRegistration;
import com.andeva.atelier.platform.iot.interfaces.rest.resources.VehicleRegistrationResource;

/**
 * Assembler to translate VehicleRegistration domain aggregate to VehicleRegistrationResource DTO.
 */
public class VehicleRegistrationResourceFromAggregateAssembler {
    public static VehicleRegistrationResource toResourceFromAggregate(VehicleRegistration aggregate) {
        return new VehicleRegistrationResource(
                aggregate.getId() != null ? aggregate.getId().value() : null,
                aggregate.getUserId(),
                aggregate.getVehicleId() != null ? aggregate.getVehicleId().value() : null,
                aggregate.getStatus() != null ? aggregate.getStatus().value() : null,
                aggregate.getCreatedAt()
        );
    }
}
