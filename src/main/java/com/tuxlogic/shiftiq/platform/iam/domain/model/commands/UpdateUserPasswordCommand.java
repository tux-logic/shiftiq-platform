package com.tuxlogic.shiftiq.platform.iam.domain.model.commands;

import com.tuxlogic.shiftiq.platform.iam.domain.model.valueobjects.Password;
import com.tuxlogic.shiftiq.platform.iam.domain.model.valueobjects.UserId;

public record UpdateUserPasswordCommand(
        UserId userId,
        Password currentPassword,
        Password newPassword
) {
}
