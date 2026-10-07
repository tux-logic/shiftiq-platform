package com.tuxlogic.shiftiq.platform.analytics.domain.model.queries;

import com.tuxlogic.shiftiq.platform.shared.domain.model.valueobjects.BranchId;

import java.time.LocalDate;

public record GetBranchAnalyticsByDateRangeQuery(
        BranchId branchId,
        LocalDate startDate,
        LocalDate endDate
) {
    public GetBranchAnalyticsByDateRangeQuery {
        if (branchId == null) throw new IllegalArgumentException("analytics.error.branchId.required");
        if (startDate == null) throw new IllegalArgumentException("analytics.error.startDate.required");
        if (endDate == null) throw new IllegalArgumentException("analytics.error.endDate.required");
    }
}
