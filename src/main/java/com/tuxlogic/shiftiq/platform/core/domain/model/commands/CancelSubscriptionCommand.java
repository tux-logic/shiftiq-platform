package com.tuxlogic.shiftiq.platform.core.domain.model.commands;

import com.tuxlogic.shiftiq.platform.shared.domain.model.valueobjects.BranchId;

public record CancelSubscriptionCommand(
        BranchId branchId
) {
}

