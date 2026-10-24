package com.tuxlogic.shiftiq.platform.iam.interfaces.rest.resources;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record VerifyOtpResource(
        @NotBlank(message = "iam.error.email.required")
        @Email(message = "iam.error.email.invalid")
        String email,

        @NotBlank(message = "iam.error.otp.required")
        @Pattern(regexp = "^[0-9]{6}$", message = "iam.error.otp.invalid")
        String code
) {
}
