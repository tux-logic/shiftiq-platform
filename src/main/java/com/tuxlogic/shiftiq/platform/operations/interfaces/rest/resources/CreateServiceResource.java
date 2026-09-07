package com.tuxlogic.shiftiq.platform.operations.interfaces.rest.resources;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.util.UUID;

public record CreateServiceResource(
        @NotNull(message = "operations.error.resource.branchId.required")
        UUID branchId,

        @NotBlank(message = "operations.error.resource.name.required")
        String name,

        @NotNull(message = "operations.error.resource.price.required")
        @Positive(message = "operations.error.resource.price.positive")
        Double price
) {}
