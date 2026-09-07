package com.tuxlogic.shiftiq.platform.inventory.domain.model.events;

import com.tuxlogic.shiftiq.platform.shared.domain.model.valueobjects.BranchId;

import java.util.UUID;

public record StockReservedEvent(Object source, BranchId branchId, UUID productId, int reservedQuantity, int remainingStock) {
}
