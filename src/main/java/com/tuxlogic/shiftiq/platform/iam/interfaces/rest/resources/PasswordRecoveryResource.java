package com.tuxlogic.shiftiq.platform.iam.interfaces.rest.resources;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record PasswordRecoveryResource(
        @NotBlank(message = "iam.error.email.required")
        @Email(message = "iam.error.email.invalid")
        String email
) {
}
