package com.tuxlogic.shiftiq.platform.core.domain.model.queries;

import com.tuxlogic.shiftiq.platform.core.domain.model.valueobjects.WorkshopSpecialtyId;

public record GetWorkshopSpecialtyByIdQuery(
        WorkshopSpecialtyId specialtyId
) {
    public GetWorkshopSpecialtyByIdQuery {
        if (specialtyId == null) throw new IllegalArgumentException("core.error.specialtyId.required");
    }
}
