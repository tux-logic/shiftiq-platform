package com.tuxlogic.shiftiq.platform.analytics.application.internal.queryservices;

import com.tuxlogic.shiftiq.platform.analytics.domain.model.aggregates.BranchAnalyticsSnapshot;
import com.tuxlogic.shiftiq.platform.analytics.domain.model.queries.GetBranchAnalyticsByDateRangeQuery;
import com.tuxlogic.shiftiq.platform.analytics.domain.model.queries.GetBranchAnalyticsSummaryQuery;
import com.tuxlogic.shiftiq.platform.analytics.domain.model.queries.GetNetworkAnalyticsOverviewQuery;
import com.tuxlogic.shiftiq.platform.analytics.domain.repositories.BranchAnalyticsRepository;
import com.tuxlogic.shiftiq.platform.shared.domain.model.valueobjects.BranchId;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

class AnalyticsQueryServiceImplTest {

    private BranchAnalyticsRepository repository;
    private AnalyticsQueryServiceImpl queryService;

    @BeforeEach
    void setUp() {
        repository = Mockito.mock(BranchAnalyticsRepository.class);
        queryService = new AnalyticsQueryServiceImpl(repository);
    }

    @Test
    @DisplayName("handle GetBranchAnalyticsSummaryQuery returns existing snapshot summary")
    void handleSummaryWithExistingSnapshot() {
        var branchId = new BranchId(UUID.randomUUID());
        var snapshot = new BranchAnalyticsSnapshot(branchId, LocalDate.now());
        snapshot.addRevenue(new BigDecimal("500.00"));
        snapshot.incrementCompletedWorkOrders();

        when(repository.findByBranchIdAndSnapshotDate(eq(branchId), any(LocalDate.class)))
                .thenReturn(Optional.of(snapshot));

        var result = queryService.handle(new GetBranchAnalyticsSummaryQuery(branchId));

        assertThat(result.isSuccess()).isTrue();
        var summary = result.success().get();
        assertThat(summary.branchId()).isEqualTo(branchId.value());
        assertThat(summary.totalRevenue()).isEqualTo(new BigDecimal("500.00"));
        assertThat(summary.completedWorkOrdersCount()).isEqualTo(1);
    }

    @Test
    @DisplayName("handle GetBranchAnalyticsSummaryQuery returns default zero summary when no snapshot")
    void handleSummaryWithNoSnapshot() {
        var branchId = new BranchId(UUID.randomUUID());

        when(repository.findByBranchIdAndSnapshotDate(eq(branchId), any(LocalDate.class)))
                .thenReturn(Optional.empty());

        var result = queryService.handle(new GetBranchAnalyticsSummaryQuery(branchId));

        assertThat(result.isSuccess()).isTrue();
        var summary = result.success().get();
        assertThat(summary.branchId()).isEqualTo(branchId.value());
        assertThat(summary.totalRevenue()).isEqualTo(BigDecimal.ZERO);
        assertThat(summary.completedWorkOrdersCount()).isEqualTo(0);
    }

    @Test
    @DisplayName("handle GetBranchAnalyticsByDateRangeQuery returns list of snapshots")
    void handleDateRange() {
        var branchId = new BranchId(UUID.randomUUID());
        var start = LocalDate.now().minusDays(7);
        var end = LocalDate.now();
        var snapshot = new BranchAnalyticsSnapshot(branchId, end);

        when(repository.findByBranchIdAndSnapshotDateBetween(eq(branchId), eq(start), eq(end)))
                .thenReturn(List.of(snapshot));

        var result = queryService.handle(new GetBranchAnalyticsByDateRangeQuery(branchId, start, end));

        assertThat(result.isSuccess()).isTrue();
        assertThat(result.success().get()).hasSize(1);
    }

    @Test
    @DisplayName("handle GetNetworkAnalyticsOverviewQuery aggregates across snapshots")
    void handleNetworkOverview() {
        var b1 = new BranchAnalyticsSnapshot(new BranchId(UUID.randomUUID()), LocalDate.now());
        b1.addRevenue(new BigDecimal("300.00"));
        b1.incrementCompletedWorkOrders();

        var b2 = new BranchAnalyticsSnapshot(new BranchId(UUID.randomUUID()), LocalDate.now());
        b2.addRevenue(new BigDecimal("200.00"));
        b2.incrementCompletedWorkOrders();

        when(repository.findBySnapshotDate(any(LocalDate.class)))
                .thenReturn(List.of(b1, b2));

        var result = queryService.handle(new GetNetworkAnalyticsOverviewQuery());

        assertThat(result.isSuccess()).isTrue();
        var overview = result.success().get();
        assertThat(overview.totalActiveBranchesCount()).isEqualTo(2);
        assertThat(overview.totalNetworkRevenue()).isEqualTo(new BigDecimal("500.00"));
        assertThat(overview.totalNetworkCompletedWorkOrders()).isEqualTo(2);
    }
}
