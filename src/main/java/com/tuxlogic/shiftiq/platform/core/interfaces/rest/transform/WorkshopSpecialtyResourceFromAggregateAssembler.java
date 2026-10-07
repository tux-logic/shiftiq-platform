package com.tuxlogic.shiftiq.platform.core.interfaces.rest.transform;

import com.tuxlogic.shiftiq.platform.core.domain.model.aggregates.WorkshopSpecialty;
import com.tuxlogic.shiftiq.platform.core.interfaces.rest.resources.WorkshopSpecialtyResource;

public class WorkshopSpecialtyResourceFromAggregateAssembler {

    public static WorkshopSpecialtyResource toResource(WorkshopSpecialty aggregate) {
        if (aggregate == null) return null;
        return new WorkshopSpecialtyResource(
                aggregate.getId() != null ? aggregate.getId().value() : null,
                aggregate.getWorkshopId() != null ? aggregate.getWorkshopId().value() : null,
                aggregate.getName(),
                aggregate.getCode(),
                aggregate.getDescription(),
                aggregate.isActive(),
                aggregate.getCreatedAt(),
                aggregate.getUpdatedAt()
        );
    }
}
