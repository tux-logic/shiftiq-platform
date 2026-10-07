package com.tuxlogic.shiftiq.platform.analytics.domain.model.queries;

import com.tuxlogic.shiftiq.platform.shared.domain.model.valueobjects.BranchId;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

class GetBranchAnalyticsByDateRangeQueryTest {

    private final BranchId branchId = new BranchId(UUID.randomUUID());

    @Test
    @DisplayName("rejects null branchId, startDate or endDate")
    void rejectsNullArguments() {
        var start = LocalDate.now().minusDays(1);
        var end = LocalDate.now();

        assertThatThrownBy(() -> new GetBranchAnalyticsByDateRangeQuery(null, start, end))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("analytics.error.branchId.required");

        assertThatThrownBy(() -> new GetBranchAnalyticsByDateRangeQuery(branchId, null, end))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("analytics.error.startDate.required");

        assertThatThrownBy(() -> new GetBranchAnalyticsByDateRangeQuery(branchId, start, null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("analytics.error.endDate.required");
    }

    @Test
    @DisplayName("rejects startDate after endDate")
    void rejectsInvertedRange() {
        var start = LocalDate.now();
        var end = LocalDate.now().minusDays(1);

        assertThatThrownBy(() -> new GetBranchAnalyticsByDateRangeQuery(branchId, start, end))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("analytics.error.invalidDateRange");
    }

    @Test
    @DisplayName("rejects ranges longer than 365 days")
    void rejectsRangeTooLarge() {
        var start = LocalDate.of(2025, 1, 1);
        var end = LocalDate.of(2026, 1, 2);

        assertThatThrownBy(() -> new GetBranchAnalyticsByDateRangeQuery(branchId, start, end))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("analytics.error.dateRangeTooLarge");
    }

    @Test
    @DisplayName("accepts a single day and a 365 day range")
    void acceptsValidRanges() {
        var day = LocalDate.now();
        new GetBranchAnalyticsByDateRangeQuery(branchId, day, day);

        var start = LocalDate.of(2025, 1, 1);
        new GetBranchAnalyticsByDateRangeQuery(branchId, start, start.plusDays(365));
    }
}
