package com.tuxlogic.shiftiq.platform.iam.domain.model.commands;

import com.tuxlogic.shiftiq.platform.iam.domain.model.valueobjects.EmailAddress;
import com.tuxlogic.shiftiq.platform.iam.domain.model.valueobjects.Password;

public record SignInCommand(EmailAddress email, Password password) {
}
