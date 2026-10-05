package com.tuxlogic.shiftiq.platform.iam.domain.model.commands;

import com.tuxlogic.shiftiq.platform.iam.domain.model.valueobjects.UserId;
import com.tuxlogic.shiftiq.platform.shared.domain.model.valueobjects.BranchId;

public record AssignBranchToUserCommand(
        UserId userId,
        BranchId branchId
) {
    public AssignBranchToUserCommand {
        if (userId == null) {
            throw new IllegalArgumentException("iam.error.userId.required");
        }
        if (branchId == null) {
            throw new IllegalArgumentException("iam.error.branchId.required");
        }
    }
}
