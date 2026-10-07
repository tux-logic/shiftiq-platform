package com.tuxlogic.shiftiq.platform.analytics.domain.model.queries;

import com.tuxlogic.shiftiq.platform.shared.domain.model.valueobjects.BranchId;

public record GetBranchAnalyticsSummaryQuery(BranchId branchId) {
    public GetBranchAnalyticsSummaryQuery {
        if (branchId == null) {
            throw new IllegalArgumentException("analytics.error.branchId.required");
        }
    }
}
