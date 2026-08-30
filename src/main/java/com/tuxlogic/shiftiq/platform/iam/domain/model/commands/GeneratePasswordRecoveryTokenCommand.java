package com.tuxlogic.shiftiq.platform.iam.domain.model.commands;

import com.tuxlogic.shiftiq.platform.iam.domain.model.valueobjects.EmailAddress;

public record GeneratePasswordRecoveryTokenCommand(EmailAddress email) {
}
