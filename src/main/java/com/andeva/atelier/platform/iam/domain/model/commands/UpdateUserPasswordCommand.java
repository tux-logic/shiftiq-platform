package com.andeva.atelier.platform.iam.domain.model.commands;

import com.andeva.atelier.platform.iam.domain.model.valueobjects.Password;
import com.andeva.atelier.platform.iam.domain.model.valueobjects.UserId;

public record UpdateUserPasswordCommand(
        UserId userId,
        Password currentPassword,
        Password newPassword
) {
}
