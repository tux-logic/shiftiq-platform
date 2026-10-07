package com.tuxlogic.shiftiq.platform.core.domain.model.queries;

import com.tuxlogic.shiftiq.platform.core.domain.model.valueobjects.WorkshopId;

public record GetWorkshopSpecialtiesByWorkshopIdQuery(
        WorkshopId workshopId,
        boolean activeOnly
) {
    public GetWorkshopSpecialtiesByWorkshopIdQuery {
        if (workshopId == null) throw new IllegalArgumentException("core.error.workshopId.required");
    }

    public GetWorkshopSpecialtiesByWorkshopIdQuery(WorkshopId workshopId) {
        this(workshopId, false);
    }
}
