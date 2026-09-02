package com.tuxlogic.shiftiq.platform.inventory.interfaces.rest.resources;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record AddBatchToProductResource(
        @NotNull(message = "inventory.error.resource.quantity.required")
        @Positive(message = "inventory.error.resource.quantity.positive") Integer quantity,
        @NotNull(message = "inventory.error.resource.acquisitionCost.required")
        @Positive(message = "inventory.error.resource.acquisitionCost.positive") Double acquisitionCost
) {
}
