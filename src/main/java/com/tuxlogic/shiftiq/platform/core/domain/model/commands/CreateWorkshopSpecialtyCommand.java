package com.tuxlogic.shiftiq.platform.core.domain.model.commands;

import com.tuxlogic.shiftiq.platform.core.domain.model.valueobjects.WorkshopId;

public record CreateWorkshopSpecialtyCommand(
        WorkshopId workshopId,
        String name,
        String code,
        String description
) {
    public CreateWorkshopSpecialtyCommand {
        if (workshopId == null) throw new IllegalArgumentException("core.error.workshopId.required");
        if (name == null || name.isBlank()) throw new IllegalArgumentException("core.error.specialtyName.required");
        if (code == null || code.isBlank()) throw new IllegalArgumentException("core.error.specialtyCode.required");
    }
}
