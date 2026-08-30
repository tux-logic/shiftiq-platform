package com.tuxlogic.shiftiq.platform.core.domain.model.commands;

import com.tuxlogic.shiftiq.platform.core.domain.model.valueobjects.BillingCycle;
import com.tuxlogic.shiftiq.platform.shared.domain.model.valueobjects.BranchId;
import com.tuxlogic.shiftiq.platform.core.domain.model.valueobjects.SubscriptionPlanId;

public record AssignSubscriptionCommand(
        BranchId branchId,
        SubscriptionPlanId planId,
        BillingCycle billingCycle,
        com.tuxlogic.shiftiq.platform.core.domain.model.valueobjects.CreditCard creditCard
) {
}

