package com.tuxlogic.shiftiq.platform.inventory.interfaces.rest.resources;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.DecimalMin;

import java.math.BigDecimal;

public record AddBatchToProductResource(
        @NotNull(message = "inventory.error.resource.quantity.required") Integer quantity,
        @NotNull(message = "inventory.error.resource.acquisitionCost.required")
        @DecimalMin(value = "0.00", inclusive = false, message = "inventory.error.resource.acquisitionCost.positive")
        BigDecimal acquisitionCost
) {
}
