package com.tuxlogic.shiftiq.platform.iam.interfaces.rest.resources;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record SignUpResource(
        @NotBlank(message = "iam.error.email.required")
        @Email(message = "iam.error.email.invalid")
        String email,

        @NotBlank(message = "iam.error.password.required")
        @Size(min = 8, message = "iam.error.password.length")
        String password
) {
}
