package com.tuxlogic.shiftiq.platform.core.domain.model.commands;

import com.tuxlogic.shiftiq.platform.core.domain.model.valueobjects.WorkshopSpecialtyId;

public record UpdateWorkshopSpecialtyCommand(
        WorkshopSpecialtyId specialtyId,
        String name,
        String description
) {
    public UpdateWorkshopSpecialtyCommand {
        if (specialtyId == null) throw new IllegalArgumentException("core.error.specialtyId.required");
        if (name == null || name.isBlank()) throw new IllegalArgumentException("core.error.specialtyName.required");
    }
}
