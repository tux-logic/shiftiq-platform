package com.tuxlogic.shiftiq.platform.inventory.interfaces.rest.resources;

public record UpdateProductResource(
        String name,
        String category,
        String sku,
        String description,
        Double salePrice,
        Integer minimumStock
) {
}
