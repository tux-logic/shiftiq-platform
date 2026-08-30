package com.andeva.atelier.platform.core.domain.model.commands;

import com.andeva.atelier.platform.shared.domain.model.valueobjects.BranchId;

public record CancelSubscriptionCommand(
        BranchId branchId
) {
}

