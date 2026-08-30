package com.andeva.atelier.platform.core.domain.model.commands;

import com.andeva.atelier.platform.core.domain.model.valueobjects.BillingCycle;
import com.andeva.atelier.platform.shared.domain.model.valueobjects.BranchId;
import com.andeva.atelier.platform.core.domain.model.valueobjects.SubscriptionPlanId;

public record AssignSubscriptionCommand(
        BranchId branchId,
        SubscriptionPlanId planId,
        BillingCycle billingCycle,
        com.andeva.atelier.platform.core.domain.model.valueobjects.CreditCard creditCard
) {
}

