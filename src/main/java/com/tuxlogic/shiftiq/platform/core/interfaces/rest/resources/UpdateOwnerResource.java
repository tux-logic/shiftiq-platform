package com.tuxlogic.shiftiq.platform.core.interfaces.rest.resources;

import jakarta.validation.constraints.NotBlank;

public record UpdateOwnerResource(
        @NotBlank(message = "firstName is required") String firstName,
        @NotBlank(message = "lastName is required") String lastName,
        @NotBlank(message = "documentType is required") String documentType,
        @NotBlank(message = "documentNumber is required") String documentNumber,
        @NotBlank(message = "phone is required") String phone
) {
}
