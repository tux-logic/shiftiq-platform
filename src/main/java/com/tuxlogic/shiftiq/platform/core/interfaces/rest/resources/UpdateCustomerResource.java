package com.tuxlogic.shiftiq.platform.core.interfaces.rest.resources;

import jakarta.validation.constraints.NotBlank;

public record UpdateCustomerResource(
        String firstName,
        String lastName,
        String businessName,
        @NotBlank(message = "documentType is required") String documentType,
        @NotBlank(message = "documentNumber is required") String documentNumber,
        @NotBlank(message = "phone is required") String phone
) {
}
