package com.tuxlogic.shiftiq.platform.inventory.interfaces.rest.resources;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.util.UUID;

public record CreateProductResource(
        @NotNull(message = "inventory.error.resource.branchId.required") UUID branchId,
        @Size(max = 50, message = "inventory.error.resource.category.size")
        @NotBlank(message = "inventory.error.resource.category.required") String category,
        @Size(max = 50, message = "inventory.error.resource.name.size")
        @NotBlank(message = "inventory.error.resource.name.required") String name,
        @Size(max = 50, message = "inventory.error.resource.sku.size")
        @NotBlank(message = "inventory.error.resource.sku.required") String sku,
        @Size(max = 500, message = "inventory.error.resource.description.size")
        String description,
        @NotNull(message = "inventory.error.resource.salePrice.required")
        @DecimalMin(value = "0.01", message = "inventory.error.resource.salePrice.positive") BigDecimal salePrice,
        @NotNull(message = "inventory.error.resource.minimumStock.required")
        @Min(value = 0, message = "inventory.error.resource.minimumStock.min") Integer minimumStock
) {
}
