package com.tuxlogic.shiftiq.platform.inventory.domain.model.aggregates;

import com.tuxlogic.shiftiq.platform.inventory.domain.exceptions.InsufficientStockException;
import com.tuxlogic.shiftiq.platform.inventory.domain.model.entities.ProductBatch;
import com.tuxlogic.shiftiq.platform.inventory.domain.model.events.LowStockAlertClearedEvent;
import com.tuxlogic.shiftiq.platform.inventory.domain.model.events.LowStockAlertTriggeredEvent;
import com.tuxlogic.shiftiq.platform.inventory.domain.model.events.ProductCreatedEvent;
import com.tuxlogic.shiftiq.platform.inventory.domain.model.events.ProductUpdatedEvent;
import com.tuxlogic.shiftiq.platform.inventory.domain.model.events.StockMovementAppliedEvent;
import com.tuxlogic.shiftiq.platform.inventory.domain.model.events.StockReleasedEvent;
import com.tuxlogic.shiftiq.platform.inventory.domain.model.events.StockReservedEvent;
import com.tuxlogic.shiftiq.platform.inventory.domain.model.valueobjects.*;
import com.tuxlogic.shiftiq.platform.shared.domain.model.valueobjects.BranchId;
import com.tuxlogic.shiftiq.platform.shared.domain.model.valueobjects.Money;
import org.springframework.data.domain.AbstractAggregateRoot;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class Product extends AbstractAggregateRoot<Product> {
    private final UUID id;
    private final BranchId branchId;
    private ProductCategory category;
    private ProductName name;
    private Sku sku;
    private InventoryQuantity currentStock;
    private Money currentSellingPrice;
    private String description;
    private Integer minimumStock;
    private boolean lowStockAlert;
    private Long version;
    private final List<ProductBatch> batches;

    public Product(UUID id, BranchId branchId, ProductCategory category, ProductName name, Sku sku, Money currentSellingPrice, String description, Integer minimumStock) {
        this.id = id != null ? id : UUID.randomUUID();
        this.branchId = branchId;
        this.category = category;
        this.name = name;
        this.sku = sku;
        this.currentSellingPrice = currentSellingPrice;
        this.description = description;
        this.minimumStock = minimumStock;
        this.currentStock = new InventoryQuantity(0);
        this.lowStockAlert = false;
        this.batches = new ArrayList<>();
        this.registerEvent(new ProductCreatedEvent(this, this.branchId, this.id));
    }

    private Product(UUID id, BranchId branchId, ProductCategory category, ProductName name, Sku sku, InventoryQuantity currentStock, Money currentSellingPrice, String description, Integer minimumStock, boolean lowStockAlert, Long version) {
        this.id = id;
        this.branchId = branchId;
        this.category = category;
        this.name = name;
        this.sku = sku;
        this.currentStock = currentStock;
        this.currentSellingPrice = currentSellingPrice;
        this.description = description;
        this.minimumStock = minimumStock;
        this.lowStockAlert = lowStockAlert;
        this.version = version;
        this.batches = new ArrayList<>();
    }

    public static Product reconstitute(UUID id, BranchId branchId, ProductCategory category, ProductName name, Sku sku, InventoryQuantity currentStock, Money currentSellingPrice, String description, Integer minimumStock, boolean lowStockAlert, Long version, List<ProductBatch> batches) {
        Product p = new Product(id, branchId, category, name, sku, currentStock, currentSellingPrice, description, minimumStock, lowStockAlert, version);
        if (batches != null) {
            p.batches.addAll(batches);
        }
        return p;
    }

    public UUID getId() { return id; }
    public BranchId getBranchId() { return branchId; }
    public ProductCategory getCategory() { return category; }
    public ProductName getName() { return name; }
    public Sku getSku() { return sku; }
    public InventoryQuantity getCurrentStock() { return currentStock; }
    public Money getCurrentSellingPrice() { return currentSellingPrice; }
    public String getDescription() { return description; }
    public Integer getMinimumStock() { return minimumStock; }
    public boolean isLowStockAlert() { return lowStockAlert; }
    public Long getVersion() { return version; }
    public List<ProductBatch> getBatches() { return Collections.unmodifiableList(batches); }

    public void addBatch(ProductBatch batch) {
        this.batches.add(batch);
        this.currentStock = this.currentStock.add(batch.getAvailableQuantity());
        refreshLowStockAlert();
    }

    public Optional<ProductBatch> applyStockMovement(int signedQuantity, Money acquisitionCost) {
        if (signedQuantity == 0) {
            throw new IllegalArgumentException("inventory.error.resource.quantity.nonZero");
        }
        if (signedQuantity > 0) {
            var batch = new ProductBatch(UUID.randomUUID(), new InventoryQuantity(signedQuantity), acquisitionCost);
            addBatch(batch);
            registerEvent(new StockMovementAppliedEvent(this, this.branchId, this.id, signedQuantity, this.currentStock.value()));
            return Optional.of(batch);
        }
        reserveStock(new InventoryQuantity(-signedQuantity));
        registerEvent(new StockMovementAppliedEvent(this, this.branchId, this.id, signedQuantity, this.currentStock.value()));
        return Optional.empty();
    }

    public void updateDetails(ProductName name, ProductCategory category, Sku sku, Money currentSellingPrice, String description, Integer minimumStock) {
        this.name = name;
        this.category = category;
        this.sku = sku;
        this.currentSellingPrice = currentSellingPrice;
        this.description = description;
        this.minimumStock = minimumStock;
        refreshLowStockAlert();
        registerEvent(new ProductUpdatedEvent(this, this.branchId, this.id));
    }

    public boolean refreshLowStockAlert() {
        boolean shouldAlert = this.currentStock.value() <= this.minimumStock;
        if (shouldAlert == this.lowStockAlert) {
            return false;
        }
        this.lowStockAlert = shouldAlert;
        if (shouldAlert) {
            registerEvent(new LowStockAlertTriggeredEvent(this.id, this.branchId, this.currentStock.value(), this.minimumStock));
        } else {
            registerEvent(new LowStockAlertClearedEvent(this.id, this.branchId, this.currentStock.value(), this.minimumStock));
        }
        return true;
    }

    public void reserveStock(InventoryQuantity amount) {
        if (this.currentStock.value() < amount.value()) {
            throw new InsufficientStockException("inventory.error.product.insufficientStock");
        }

        int remainingToDeduct = amount.value();
        for (ProductBatch batch : this.batches) {
            if (remainingToDeduct <= 0) break;
            int batchAvail = batch.getAvailableQuantity().value();
            if (batchAvail > 0) {
                int deduct = Math.min(batchAvail, remainingToDeduct);
                batch.deductQuantity(new InventoryQuantity(deduct));
                remainingToDeduct -= deduct;
            }
        }
        this.currentStock = this.currentStock.subtract(amount);
        refreshLowStockAlert();
        registerEvent(new StockReservedEvent(this, this.branchId, this.id, amount.value(), this.currentStock.value()));
    }

    public void releaseStock(InventoryQuantity amount) {
        int remainingToAdd = amount.value();
        for (ProductBatch batch : this.batches) {
            if (remainingToAdd <= 0) break;
            int capacity = batch.getInitialQuantity().value() - batch.getAvailableQuantity().value();
            if (capacity > 0) {
                int add = Math.min(capacity, remainingToAdd);
                batch.addQuantity(new InventoryQuantity(add));
                remainingToAdd -= add;
            }
        }
        this.currentStock = this.currentStock.add(amount);
        refreshLowStockAlert();
        registerEvent(new StockReleasedEvent(this, this.branchId, this.id, amount.value(), this.currentStock.value()));
    }
}
