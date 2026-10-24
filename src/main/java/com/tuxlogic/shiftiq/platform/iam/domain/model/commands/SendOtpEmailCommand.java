package com.tuxlogic.shiftiq.platform.iam.domain.model.commands;

import com.tuxlogic.shiftiq.platform.iam.domain.model.valueobjects.EmailAddress;

public record SendOtpEmailCommand(EmailAddress email) {
    public SendOtpEmailCommand {
        if (email == null) {
            throw new IllegalArgumentException("iam.error.email.required");
        }
    }
}
