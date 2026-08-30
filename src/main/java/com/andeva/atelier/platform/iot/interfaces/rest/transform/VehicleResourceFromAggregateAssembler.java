package com.andeva.atelier.platform.iot.interfaces.rest.transform;

import com.andeva.atelier.platform.iot.domain.model.aggregates.Vehicle;
import com.andeva.atelier.platform.iot.interfaces.rest.resources.VehicleResource;

/**
 * Assembler to translate Vehicle domain aggregate to VehicleResource DTO.
 */
public class VehicleResourceFromAggregateAssembler {
    public static VehicleResource toResourceFromAggregate(Vehicle aggregate) {
        return new VehicleResource(
                aggregate.getId() != null ? aggregate.getId().value() : null,
                aggregate.getPlateNumber(),
                aggregate.getBrand(),
                aggregate.getModel(),
                aggregate.getYear(),
                aggregate.getVin()
        );
    }
}
