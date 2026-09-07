package com.tuxlogic.shiftiq.platform.operations.interfaces.rest.resources;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record UpdateServiceResource(
        @NotBlank(message = "operations.error.resource.name.required")
        String name,

        @NotNull(message = "operations.error.resource.price.required")
        @Positive(message = "operations.error.resource.price.positive")
        Double price
) {}
