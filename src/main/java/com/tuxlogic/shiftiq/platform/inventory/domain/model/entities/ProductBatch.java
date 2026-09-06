package com.tuxlogic.shiftiq.platform.inventory.domain.model.entities;

import com.tuxlogic.shiftiq.platform.inventory.domain.model.valueobjects.InventoryQuantity;
import com.tuxlogic.shiftiq.platform.shared.domain.model.valueobjects.Money;

import java.time.Instant;
import java.util.UUID;

public class ProductBatch {
    private final UUID batchId;
    private final InventoryQuantity initialQuantity;
    private InventoryQuantity availableQuantity;
    private final Money acquisitionCost;
    private final Instant receptionDate;
    private Long version;

    public ProductBatch(UUID batchId, InventoryQuantity initialQuantity, Money acquisitionCost) {
        this.batchId = batchId != null ? batchId : UUID.randomUUID();
        this.initialQuantity = initialQuantity;
        this.availableQuantity = initialQuantity;
        this.acquisitionCost = acquisitionCost;
        this.receptionDate = Instant.now();
    }

    private ProductBatch(UUID batchId, InventoryQuantity initialQuantity, InventoryQuantity availableQuantity, Money acquisitionCost, Instant receptionDate, Long version) {
        this.batchId = batchId;
        this.initialQuantity = initialQuantity;
        this.availableQuantity = availableQuantity;
        this.acquisitionCost = acquisitionCost;
        this.receptionDate = receptionDate != null ? receptionDate : Instant.now();
        this.version = version;
    }

    public static ProductBatch reconstitute(UUID batchId, InventoryQuantity initialQuantity, InventoryQuantity availableQuantity, Money acquisitionCost, Instant receptionDate, Long version) {
        return new ProductBatch(batchId, initialQuantity, availableQuantity, acquisitionCost, receptionDate, version);
    }

    public UUID getBatchId() { return batchId; }
    public InventoryQuantity getInitialQuantity() { return initialQuantity; }
    public InventoryQuantity getAvailableQuantity() { return availableQuantity; }
    public Money getAcquisitionCost() { return acquisitionCost; }
    public Instant getReceptionDate() { return receptionDate; }
    public Long getVersion() { return version; }
    
    public void deductQuantity(InventoryQuantity amount) { this.availableQuantity = this.availableQuantity.subtract(amount); }
    public void addQuantity(InventoryQuantity amount) { this.availableQuantity = this.availableQuantity.add(amount); }

    public static ProductBatch forStockAdjustment(int signedQuantity, Money acquisitionCost, int resultingStock) {
        return reconstitute(
                UUID.randomUUID(),
                new InventoryQuantity(0),
                new InventoryQuantity(resultingStock),
                acquisitionCost,
                Instant.now(),
                null
        );
    }
}
