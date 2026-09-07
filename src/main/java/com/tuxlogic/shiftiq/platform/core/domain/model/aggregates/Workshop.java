package com.tuxlogic.shiftiq.platform.core.domain.model.aggregates;

import com.tuxlogic.shiftiq.platform.core.domain.model.valueobjects.OwnerId;
import com.tuxlogic.shiftiq.platform.core.domain.model.valueobjects.TaxId;
import com.tuxlogic.shiftiq.platform.core.domain.model.valueobjects.WorkshopId;
import com.tuxlogic.shiftiq.platform.shared.domain.model.aggregates.AbstractDomainAggregateRoot;

import com.tuxlogic.shiftiq.platform.core.domain.model.events.WorkshopCreatedEvent;

import lombok.Getter;

import java.time.Instant;
import java.util.UUID;

import com.tuxlogic.shiftiq.platform.core.domain.model.valueobjects.MileageIntervalConfig;

@Getter
public class Workshop extends AbstractDomainAggregateRoot<Workshop> {

    private WorkshopId id;
    private OwnerId ownerId;
    private String businessName;
    private String brandName;
    private TaxId taxId;
    private MileageIntervalConfig mileageIntervalConfig;
    private Instant createdAt;
    private Instant updatedAt;
    private Instant deletedAt;
    private Long version;

    public Workshop() {
        this.mileageIntervalConfig = new MileageIntervalConfig(1);
    }

    public Workshop(WorkshopId id, OwnerId ownerId, String businessName, String brandName, TaxId taxId, MileageIntervalConfig mileageIntervalConfig, Instant createdAt, Instant updatedAt, Instant deletedAt, Long version) {
        if (businessName == null || businessName.isBlank()) throw new IllegalArgumentException("core.error.businessName.required");
        if (brandName == null || brandName.isBlank()) throw new IllegalArgumentException("core.error.brandName.required");
        if (mileageIntervalConfig == null) throw new IllegalArgumentException("core.error.mileageIntervalConfig.required");
        this.id = id;
        this.ownerId = ownerId;
        this.businessName = businessName;
        this.brandName = brandName;
        this.taxId = taxId;
        this.mileageIntervalConfig = mileageIntervalConfig;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.deletedAt = deletedAt;
        this.version = version;
    }

    public Workshop(OwnerId ownerId, String businessName, String brandName, TaxId taxId, MileageIntervalConfig mileageIntervalConfig) {
        if (businessName == null || businessName.isBlank()) throw new IllegalArgumentException("core.error.businessName.required");
        if (brandName == null || brandName.isBlank()) throw new IllegalArgumentException("core.error.brandName.required");
        if (mileageIntervalConfig == null) throw new IllegalArgumentException("core.error.mileageIntervalConfig.required");
        this.id = new WorkshopId(UUID.randomUUID());
        this.ownerId = ownerId;
        this.businessName = businessName;
        this.brandName = brandName;
        this.taxId = taxId;
        this.mileageIntervalConfig = mileageIntervalConfig;
        this.registerDomainEvent(new WorkshopCreatedEvent(this, this.id.value(), this.ownerId != null ? this.ownerId.value() : null));
    }

    public void update(String businessName, String brandName, TaxId taxId, MileageIntervalConfig mileageIntervalConfig) {
        if (businessName == null || businessName.isBlank()) throw new IllegalArgumentException("core.error.businessName.required");
        if (brandName == null || brandName.isBlank()) throw new IllegalArgumentException("core.error.brandName.required");
        if (mileageIntervalConfig == null) throw new IllegalArgumentException("core.error.mileageIntervalConfig.required");

        this.businessName = businessName;
        this.brandName = brandName;
        this.taxId = taxId;
        this.mileageIntervalConfig = mileageIntervalConfig;
        this.registerDomainEvent(new com.tuxlogic.shiftiq.platform.core.domain.model.events.WorkshopUpdatedEvent(this, this.id.value()));
    }
}
