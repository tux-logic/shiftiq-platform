package com.tuxlogic.shiftiq.platform.core.interfaces.rest.resources;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CreateWorkshopSpecialtyResource(
        @NotBlank(message = "core.error.resource.specialtyName.required")
        @Size(max = 100, message = "core.error.resource.specialtyName.size")
        String name,

        @NotBlank(message = "core.error.resource.specialtyCode.required")
        @Size(max = 50, message = "core.error.resource.specialtyCode.size")
        String code,

        @Size(max = 255, message = "core.error.resource.description.size")
        String description
) {
}
