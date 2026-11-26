package com.tuxlogic.shiftiq.platform.core.domain.model.commands;

import com.tuxlogic.shiftiq.platform.core.domain.model.valueobjects.BayType;
import com.tuxlogic.shiftiq.platform.shared.domain.model.valueobjects.BranchId;

public record CreateBranchBayCommand(
        BranchId branchId,
        String code,
        String name,
        BayType type
) {
    public CreateBranchBayCommand {
        if (branchId == null) {
            throw new IllegalArgumentException("core.error.branchId.required");
        }
        if (code == null || code.isBlank()) {
            throw new IllegalArgumentException("core.error.bayCode.required");
        }
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("core.error.bayName.required");
        }
        if (type == null) {
            throw new IllegalArgumentException("core.error.bayType.required");
        }
    }
}