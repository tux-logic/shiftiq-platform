package com.tuxlogic.shiftiq.platform.iam.interfaces.rest.resources;

import jakarta.validation.constraints.NotBlank;

public record GoogleSignInResource(
        @NotBlank(message = "iam.error.idToken.required")
        String idToken
) {}
