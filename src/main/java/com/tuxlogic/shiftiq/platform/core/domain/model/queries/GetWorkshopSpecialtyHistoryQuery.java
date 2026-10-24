package com.tuxlogic.shiftiq.platform.core.domain.model.queries;

import com.tuxlogic.shiftiq.platform.core.domain.model.valueobjects.WorkshopId;
import com.tuxlogic.shiftiq.platform.core.domain.model.valueobjects.WorkshopSpecialtyId;

public record GetWorkshopSpecialtyHistoryQuery(WorkshopId workshopId, WorkshopSpecialtyId specialtyId) {
    public GetWorkshopSpecialtyHistoryQuery {
        if (workshopId == null) {
            throw new IllegalArgumentException("core.error.workshopId.required");
        }
    }
}
