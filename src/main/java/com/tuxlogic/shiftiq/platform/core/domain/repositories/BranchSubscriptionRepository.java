package com.tuxlogic.shiftiq.platform.core.domain.repositories;

import com.tuxlogic.shiftiq.platform.core.domain.model.aggregates.BranchSubscription;
import com.tuxlogic.shiftiq.platform.shared.domain.model.valueobjects.BranchId;
import com.tuxlogic.shiftiq.platform.core.domain.model.valueobjects.BranchSubscriptionId;

import java.util.List;
import java.util.Optional;

public interface BranchSubscriptionRepository {
    BranchSubscription save(BranchSubscription branchSubscription);
    Optional<BranchSubscription> findById(BranchSubscriptionId id);
    List<BranchSubscription> findAllByBranchId(BranchId branchId);
    Optional<BranchSubscription> findActiveByBranchId(BranchId branchId);
}

