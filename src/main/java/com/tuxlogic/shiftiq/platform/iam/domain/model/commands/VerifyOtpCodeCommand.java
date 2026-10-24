package com.tuxlogic.shiftiq.platform.iam.domain.model.commands;

import com.tuxlogic.shiftiq.platform.iam.domain.model.valueobjects.EmailAddress;

public record VerifyOtpCodeCommand(EmailAddress email, String code) {
    public VerifyOtpCodeCommand {
        if (email == null) {
            throw new IllegalArgumentException("iam.error.email.required");
        }
        if (code == null || code.isBlank()) {
            throw new IllegalArgumentException("iam.error.otp.required");
        }
    }
}
