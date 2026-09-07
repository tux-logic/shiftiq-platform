package com.tuxlogic.shiftiq.platform.core.infrastructure.persistence.jpa.assemblers;

import com.tuxlogic.shiftiq.platform.core.domain.model.aggregates.SubscriptionPlan;
import com.tuxlogic.shiftiq.platform.core.domain.model.valueobjects.SubscriptionPlanId;
import com.tuxlogic.shiftiq.platform.core.infrastructure.persistence.jpa.entities.SubscriptionPlanPersistenceEntity;

public final class SubscriptionPlanPersistenceAssembler {

    private SubscriptionPlanPersistenceAssembler() {}

    public static SubscriptionPlanPersistenceEntity toEntity(SubscriptionPlan subscriptionPlan, SubscriptionPlanPersistenceEntity entity) {
        if (entity == null) {
            entity = new SubscriptionPlanPersistenceEntity();
        }
        entity.setId(subscriptionPlan.getId() != null ? subscriptionPlan.getId().value() : null);
        entity.setName(subscriptionPlan.getName());
        entity.setMonthlyPrice(subscriptionPlan.getMonthlyPrice());
        entity.setMaxObd2Devices(subscriptionPlan.getMaxObd2Devices());
        entity.setMaxMonthlySnapshotsPerVehicle(subscriptionPlan.getMaxMonthlySnapshotsPerVehicle());
        entity.setMaxCustomers(subscriptionPlan.getMaxCustomers());
        entity.setMaxStaffAccounts(subscriptionPlan.getMaxStaffAccounts());
        entity.setActive(subscriptionPlan.isActive());
        return entity;
    }

    public static SubscriptionPlan toDomain(SubscriptionPlanPersistenceEntity entity) {
        return new SubscriptionPlan(
                new SubscriptionPlanId(entity.getId()),
                entity.getName(),
                entity.getMonthlyPrice(),
                entity.getMaxObd2Devices(),
                entity.getMaxMonthlySnapshotsPerVehicle(),
                entity.getMaxCustomers(),
                entity.getMaxStaffAccounts(),
                entity.isActive()
        );
    }
}
