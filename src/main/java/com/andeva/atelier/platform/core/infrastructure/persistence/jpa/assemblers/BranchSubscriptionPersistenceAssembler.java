package com.andeva.atelier.platform.core.infrastructure.persistence.jpa.assemblers;

import com.andeva.atelier.platform.core.domain.model.aggregates.BranchSubscription;
import com.andeva.atelier.platform.shared.domain.model.valueobjects.BranchId;
import com.andeva.atelier.platform.core.domain.model.valueobjects.BranchSubscriptionId;
import com.andeva.atelier.platform.core.domain.model.valueobjects.SubscriptionPlanId;
import com.andeva.atelier.platform.core.infrastructure.persistence.jpa.entities.BranchSubscriptionPersistenceEntity;

public final class BranchSubscriptionPersistenceAssembler {

    private BranchSubscriptionPersistenceAssembler() {}

    public static BranchSubscriptionPersistenceEntity toEntity(BranchSubscription branchSubscription, BranchSubscriptionPersistenceEntity entity) {
        if (entity == null) {
            entity = new BranchSubscriptionPersistenceEntity();
        }
        entity.setId(branchSubscription.getId() != null ? branchSubscription.getId().value() : null);
        entity.setBranchId(branchSubscription.getBranchId() != null ? branchSubscription.getBranchId().value() : null);
        entity.setPlanId(branchSubscription.getPlanId() != null ? branchSubscription.getPlanId().value() : null);
        entity.setStatus(branchSubscription.getStatus());
        entity.setBillingCycle(branchSubscription.getBillingCycle());
        entity.setStartDate(branchSubscription.getStartDate());
        entity.setEndDate(branchSubscription.getEndDate());
        entity.setCanceledAt(branchSubscription.getCanceledAt());
        return entity;
    }

    public static BranchSubscription toDomain(BranchSubscriptionPersistenceEntity entity) {
        return new BranchSubscription(
                new BranchSubscriptionId(entity.getId()),
                new BranchId(entity.getBranchId()),
                new SubscriptionPlanId(entity.getPlanId()),
                entity.getStatus(),
                entity.getBillingCycle(),
                entity.getStartDate(),
                entity.getEndDate(),
                entity.getCanceledAt()
        );
    }
}

