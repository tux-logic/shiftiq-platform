package com.andeva.atelier.platform.core.interfaces.rest.transform;

import com.andeva.atelier.platform.core.domain.model.commands.AssignSubscriptionCommand;
import com.andeva.atelier.platform.core.domain.model.valueobjects.BillingCycle;
import com.andeva.atelier.platform.shared.domain.model.valueobjects.BranchId;
import com.andeva.atelier.platform.core.domain.model.valueobjects.SubscriptionPlanId;
import com.andeva.atelier.platform.core.interfaces.rest.resources.AssignSubscriptionResource;

import java.util.UUID;

public class AssignSubscriptionCommandFromResourceAssembler {
    public static AssignSubscriptionCommand toCommandFromResource(UUID branchId, AssignSubscriptionResource resource) {
        return new AssignSubscriptionCommand(
                new BranchId(branchId),
                new SubscriptionPlanId(resource.planId()),
                BillingCycle.valueOf(resource.billingCycle()),
                new com.andeva.atelier.platform.core.domain.model.valueobjects.CreditCard(
                        resource.cardNumber(),
                        resource.cardHolderName(),
                        resource.expirationDate(),
                        resource.cvv()
                )
        );
    }
}

