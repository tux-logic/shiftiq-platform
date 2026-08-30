package com.tuxlogic.shiftiq.platform.core.interfaces.rest.transform;

import com.tuxlogic.shiftiq.platform.core.domain.model.commands.AssignSubscriptionCommand;
import com.tuxlogic.shiftiq.platform.core.domain.model.valueobjects.BillingCycle;
import com.tuxlogic.shiftiq.platform.shared.domain.model.valueobjects.BranchId;
import com.tuxlogic.shiftiq.platform.core.domain.model.valueobjects.SubscriptionPlanId;
import com.tuxlogic.shiftiq.platform.core.interfaces.rest.resources.AssignSubscriptionResource;

import java.util.UUID;

public class AssignSubscriptionCommandFromResourceAssembler {
    public static AssignSubscriptionCommand toCommandFromResource(UUID branchId, AssignSubscriptionResource resource) {
        return new AssignSubscriptionCommand(
                new BranchId(branchId),
                new SubscriptionPlanId(resource.planId()),
                BillingCycle.valueOf(resource.billingCycle()),
                new com.tuxlogic.shiftiq.platform.core.domain.model.valueobjects.CreditCard(
                        resource.cardNumber(),
                        resource.cardHolderName(),
                        resource.expirationDate(),
                        resource.cvv()
                )
        );
    }
}

