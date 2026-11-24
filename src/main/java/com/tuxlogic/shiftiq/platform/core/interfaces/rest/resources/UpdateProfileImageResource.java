package com.tuxlogic.shiftiq.platform.core.interfaces.rest.resources;

import jakarta.validation.constraints.NotBlank;

public record UpdateProfileImageResource(
        @NotBlank(message = "profileImageUrl is required") String profileImageUrl
) {
}
