package com.tuxlogic.shiftiq.platform.inventory.interfaces.rest.resources;

import java.util.UUID;
import java.math.BigDecimal;

public record ProductResource(
        UUID id,
        String branchId,
        String category,
        String name,
        String sku,
        String description,
        BigDecimal salePrice,
        Integer minimumStock,
        Integer currentStock,
        Boolean lowStockAlert
) {
}
