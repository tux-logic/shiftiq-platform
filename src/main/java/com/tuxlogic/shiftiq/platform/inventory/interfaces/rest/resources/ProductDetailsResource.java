package com.tuxlogic.shiftiq.platform.inventory.interfaces.rest.resources;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

public record ProductDetailsResource(
        UUID id,
        String branchId,
        String category,
        String name,
        String sku,
        String description,
        BigDecimal salePrice,
        Integer minimumStock,
        Integer currentStock,
        Boolean lowStockAlert,
        List<ProductBatchResource> batches
) {
}
