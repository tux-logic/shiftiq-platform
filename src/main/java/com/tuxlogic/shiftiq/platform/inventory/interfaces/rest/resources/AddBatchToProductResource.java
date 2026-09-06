package com.tuxlogic.shiftiq.platform.inventory.interfaces.rest.resources;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record AddBatchToProductResource(
        @NotNull(message = "inventory.error.resource.quantity.required") Integer quantity,
        @NotNull(message = "inventory.error.resource.acquisitionCost.required")
        @Min(value = 0, message = "inventory.error.resource.acquisitionCost.min")
        Double acquisitionCost
) {
}
