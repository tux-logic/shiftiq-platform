package com.tuxlogic.shiftiq.platform.iam.interfaces.rest.resources;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;

import java.util.Set;

public record CreateWorkshopRoleResource(
        @NotBlank(message = "iam.error.workshopRole.nameRequired") String name,
        String description,
        @NotEmpty(message = "iam.error.workshopRole.permissionsRequired") Set<String> permissions
) {
}
