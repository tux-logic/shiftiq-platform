package com.andeva.atelier.platform.core.interfaces.rest.transform;

import com.andeva.atelier.platform.core.domain.model.aggregates.BranchSubscription;
import com.andeva.atelier.platform.core.interfaces.rest.resources.BranchSubscriptionResource;

public class BranchSubscriptionResourceFromEntityAssembler {
    public static BranchSubscriptionResource toResourceFromEntity(BranchSubscription entity) {
        return new BranchSubscriptionResource(
                entity.getId() != null ? entity.getId().value() : null,
                entity.getBranchId() != null ? entity.getBranchId().value() : null,
                entity.getPlanId() != null ? entity.getPlanId().value() : null,
                entity.getBillingCycle().name(),
                entity.getStatus().name(),
                entity.getStartDate(),
                entity.getEndDate()
        );
    }
}
