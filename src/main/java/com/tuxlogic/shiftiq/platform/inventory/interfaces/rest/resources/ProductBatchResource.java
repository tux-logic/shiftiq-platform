package com.tuxlogic.shiftiq.platform.inventory.interfaces.rest.resources;

import java.time.Instant;

public record ProductBatchResource(
        String batchId,
        Integer initialQuantity,
        Integer availableQuantity,
        Double acquisitionCost,
        Instant createdAt
) {
}
