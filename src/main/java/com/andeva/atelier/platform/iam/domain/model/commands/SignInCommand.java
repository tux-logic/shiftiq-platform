package com.andeva.atelier.platform.iam.domain.model.commands;

import com.andeva.atelier.platform.iam.domain.model.valueobjects.EmailAddress;
import com.andeva.atelier.platform.iam.domain.model.valueobjects.Password;

public record SignInCommand(EmailAddress email, Password password) {
}
