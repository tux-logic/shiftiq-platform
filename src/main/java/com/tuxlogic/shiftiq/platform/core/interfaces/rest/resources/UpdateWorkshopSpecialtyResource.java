package com.tuxlogic.shiftiq.platform.core.interfaces.rest.resources;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record UpdateWorkshopSpecialtyResource(
        @NotBlank(message = "core.error.resource.specialtyName.required")
        @Size(max = 100, message = "core.error.resource.specialtyName.size")
        String name,

        @Size(max = 255, message = "core.error.resource.description.size")
        String description
) {
}
