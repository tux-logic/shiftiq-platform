package com.andeva.atelier.platform.iam.domain.model.commands;

import com.andeva.atelier.platform.iam.domain.model.valueobjects.EmailAddress;
import com.andeva.atelier.platform.iam.domain.model.valueobjects.UserId;

public record UpdateUserEmailCommand(UserId userId, EmailAddress newEmail) {}
