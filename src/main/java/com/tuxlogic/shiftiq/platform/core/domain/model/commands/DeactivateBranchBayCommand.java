package com.tuxlogic.shiftiq.platform.core.domain.model.commands;

import com.tuxlogic.shiftiq.platform.core.domain.model.valueobjects.BranchBayId;

public record DeactivateBranchBayCommand(BranchBayId bayId) {
    public DeactivateBranchBayCommand {
        if (bayId == null) {
            throw new IllegalArgumentException("core.error.bayId.required");
        }
    }
}