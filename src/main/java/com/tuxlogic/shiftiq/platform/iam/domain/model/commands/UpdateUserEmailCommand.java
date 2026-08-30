package com.tuxlogic.shiftiq.platform.iam.domain.model.commands;

import com.tuxlogic.shiftiq.platform.iam.domain.model.valueobjects.EmailAddress;
import com.tuxlogic.shiftiq.platform.iam.domain.model.valueobjects.UserId;

public record UpdateUserEmailCommand(UserId userId, EmailAddress newEmail) {}
