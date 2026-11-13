package com.tuxlogic.shiftiq.platform.iam.interfaces.rest.resources;

import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record AssignWorkshopRoleResource(
        @NotNull(message = "iam.error.userId.required") UUID userId
) {
}
