package com.tuxlogic.shiftiq.platform.iam.interfaces.rest.resources;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record UpdateUserPasswordResource(
        @NotBlank(message = "iam.error.password.required")
        String currentPassword,

        @NotBlank(message = "iam.error.password.required")
        @Size(min = 8, message = "iam.error.password.length")
        String newPassword
) {
}
