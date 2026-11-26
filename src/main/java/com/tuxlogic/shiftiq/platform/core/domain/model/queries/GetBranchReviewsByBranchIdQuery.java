package com.tuxlogic.shiftiq.platform.core.domain.model.queries;

import com.tuxlogic.shiftiq.platform.shared.domain.model.valueobjects.BranchId;

public record GetBranchReviewsByBranchIdQuery(BranchId branchId) {
    public GetBranchReviewsByBranchIdQuery {
        if (branchId == null) {
            throw new IllegalArgumentException("core.error.branchId.required");
        }
    }
}