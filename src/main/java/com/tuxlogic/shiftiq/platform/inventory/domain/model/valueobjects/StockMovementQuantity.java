package com.tuxlogic.shiftiq.platform.inventory.domain.model.valueobjects;

public record StockMovementQuantity(Integer value) {
    public StockMovementQuantity {
        if (value == null) {
            throw new IllegalArgumentException("inventory.error.resource.quantity.required");
        }
        if (value == 0) {
            throw new IllegalArgumentException("inventory.error.resource.quantity.nonZero");
        }
    }

    public boolean isPositive() {
        return value > 0;
    }

    public InventoryQuantity absoluteValue() {
        return new InventoryQuantity(Math.abs(value));
    }
}
