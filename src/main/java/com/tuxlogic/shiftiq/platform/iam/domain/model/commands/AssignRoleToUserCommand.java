package com.tuxlogic.shiftiq.platform.iam.domain.model.commands;

import com.tuxlogic.shiftiq.platform.iam.domain.model.valueobjects.Roles;
import com.tuxlogic.shiftiq.platform.iam.domain.model.valueobjects.UserId;

public record AssignRoleToUserCommand(
        UserId userId,
        Roles role
) {
    public AssignRoleToUserCommand {
        if (userId == null) {
            throw new IllegalArgumentException("iam.error.userId.required");
        }
        if (role == null) {
            throw new IllegalArgumentException("iam.error.role.required");
        }
    }
}
