package com.andeva.atelier.platform.iam.domain.model.commands;

import com.andeva.atelier.platform.iam.domain.model.valueobjects.Password;

public record ResetPasswordCommand(String token, Password newPassword) {
}
