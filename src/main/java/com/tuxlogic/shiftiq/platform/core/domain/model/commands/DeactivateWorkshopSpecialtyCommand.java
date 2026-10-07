package com.tuxlogic.shiftiq.platform.core.domain.model.commands;

import com.tuxlogic.shiftiq.platform.core.domain.model.valueobjects.WorkshopSpecialtyId;

public record DeactivateWorkshopSpecialtyCommand(
        WorkshopSpecialtyId specialtyId
) {
    public DeactivateWorkshopSpecialtyCommand {
        if (specialtyId == null) throw new IllegalArgumentException("core.error.specialtyId.required");
    }
}
