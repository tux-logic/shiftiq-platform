package com.tuxlogic.shiftiq.platform.core.domain.model.commands;

import com.tuxlogic.shiftiq.platform.core.domain.model.valueobjects.BayType;
import com.tuxlogic.shiftiq.platform.core.domain.model.valueobjects.BranchBayId;

public record UpdateBranchBayCommand(
        BranchBayId bayId,
        String name,
        BayType type
) {
    public UpdateBranchBayCommand {
        if (bayId == null) {
            throw new IllegalArgumentException("core.error.bayId.required");
        }
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("core.error.bayName.required");
        }
        if (type == null) {
            throw new IllegalArgumentException("core.error.bayType.required");
        }
    }
}