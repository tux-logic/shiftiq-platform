package com.tuxlogic.shiftiq.platform.inventory.interfaces.rest.resources;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record UpdateProductResource(
        @NotBlank(message = "inventory.error.resource.name.required") String name,
        @NotBlank(message = "inventory.error.resource.category.required") String category,
        @NotBlank(message = "inventory.error.resource.sku.required") String sku,
        String description,
        @NotNull(message = "inventory.error.resource.salePrice.required")
        @Positive(message = "inventory.error.resource.salePrice.positive") Double salePrice,
        @NotNull(message = "inventory.error.resource.minimumStock.required")
        @Min(value = 0, message = "inventory.error.resource.minimumStock.min") Integer minimumStock
) {
}
