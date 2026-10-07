package com.tuxlogic.shiftiq.platform.analytics.domain.model.queries;

import com.tuxlogic.shiftiq.platform.shared.domain.model.valueobjects.BranchId;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

public record GetBranchAnalyticsByDateRangeQuery(
        BranchId branchId,
        LocalDate startDate,
        LocalDate endDate
) {
    private static final long MAX_RANGE_DAYS = 365L;

    public GetBranchAnalyticsByDateRangeQuery {
        if (branchId == null) throw new IllegalArgumentException("analytics.error.branchId.required");
        if (startDate == null) throw new IllegalArgumentException("analytics.error.startDate.required");
        if (endDate == null) throw new IllegalArgumentException("analytics.error.endDate.required");
        if (startDate.isAfter(endDate)) throw new IllegalArgumentException("analytics.error.invalidDateRange");
        if (ChronoUnit.DAYS.between(startDate, endDate) > MAX_RANGE_DAYS) {
            throw new IllegalArgumentException("analytics.error.dateRangeTooLarge");
        }
    }
}
