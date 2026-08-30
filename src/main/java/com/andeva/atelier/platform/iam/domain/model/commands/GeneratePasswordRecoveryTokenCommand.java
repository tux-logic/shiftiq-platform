package com.andeva.atelier.platform.iam.domain.model.commands;

import com.andeva.atelier.platform.iam.domain.model.valueobjects.EmailAddress;

public record GeneratePasswordRecoveryTokenCommand(EmailAddress email) {
}
