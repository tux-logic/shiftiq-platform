package com.tuxlogic.shiftiq.platform.core.domain.model.aggregates;

import com.tuxlogic.shiftiq.platform.core.domain.model.valueobjects.BillingCycle;
import com.tuxlogic.shiftiq.platform.core.domain.model.valueobjects.BranchSubscriptionId;
import com.tuxlogic.shiftiq.platform.core.domain.model.valueobjects.SubscriptionPlanId;
import com.tuxlogic.shiftiq.platform.core.domain.model.valueobjects.SubscriptionStatus;
import com.tuxlogic.shiftiq.platform.shared.domain.model.valueobjects.BranchId;
import com.tuxlogic.shiftiq.platform.shared.domain.model.aggregates.AbstractDomainAggregateRoot;

import lombok.Getter;
import java.util.Date;
import java.util.UUID;

@Getter
public class BranchSubscription extends AbstractDomainAggregateRoot<BranchSubscription> {

    private BranchSubscriptionId id;
    private BranchId branchId;
    private SubscriptionPlanId planId;
    private SubscriptionStatus status;
    private BillingCycle billingCycle;
    private Date startDate;
    private Date endDate;
    private Date canceledAt;

    public BranchSubscription() {}

    public BranchSubscription(BranchId branchId, SubscriptionPlanId planId, BillingCycle billingCycle, Date startDate, Date endDate) {
        if (billingCycle == null) throw new IllegalArgumentException("core.error.billingCycle.required");
        if (startDate == null) throw new IllegalArgumentException("core.error.startDate.required");
        if (endDate == null) throw new IllegalArgumentException("core.error.endDate.required");

        this.id = new BranchSubscriptionId(UUID.randomUUID());
        this.branchId = branchId;
        this.planId = planId;
        this.status = SubscriptionStatus.ACTIVE;
        this.billingCycle = billingCycle;
        this.startDate = startDate;
        this.endDate = endDate;
    }

    public BranchSubscription(BranchSubscriptionId id, BranchId branchId, SubscriptionPlanId planId, SubscriptionStatus status, BillingCycle billingCycle, Date startDate, Date endDate, Date canceledAt) {
        this(branchId, planId, billingCycle, startDate, endDate);
        this.id = id;
        this.status = status;
        this.canceledAt = canceledAt;
    }

    public void cancel(Date canceledAt) {
        this.status = SubscriptionStatus.CANCELED;
        this.canceledAt = canceledAt;
    }
}

