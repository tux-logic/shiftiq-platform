package com.tuxlogic.shiftiq.platform.core.interfaces.rest.resources;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.UUID;

public record CreateCustomerResource(
        @NotNull(message = "userId is required") UUID userId,
        boolean isCorporate,
        String firstName,
        String lastName,
        String businessName,
        @NotBlank(message = "documentType is required") String documentType,
        @NotBlank(message = "documentNumber is required") String documentNumber,
        @NotBlank(message = "phone is required") String phone
) {
}
