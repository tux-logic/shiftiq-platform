package com.tuxlogic.shiftiq.platform.analytics.application.internal.queryservices;

import com.tuxlogic.shiftiq.platform.analytics.application.internal.dto.DailyAnalyticsSnapshot;
import com.tuxlogic.shiftiq.platform.analytics.application.internal.support.AnalyticsClock;
import com.tuxlogic.shiftiq.platform.analytics.domain.model.aggregates.BranchAnalyticsSnapshot;
import com.tuxlogic.shiftiq.platform.analytics.domain.model.queries.CustomerDashboardSummary;
import com.tuxlogic.shiftiq.platform.analytics.domain.model.queries.EmployeeDashboardSummary;
import com.tuxlogic.shiftiq.platform.analytics.domain.model.queries.GetBranchAnalyticsByDateRangeQuery;
import com.tuxlogic.shiftiq.platform.analytics.domain.model.queries.GetBranchAnalyticsSummaryQuery;
import com.tuxlogic.shiftiq.platform.analytics.domain.model.queries.GetCustomerDashboardSummaryQuery;
import com.tuxlogic.shiftiq.platform.analytics.domain.model.queries.GetEmployeeDashboardSummaryQuery;
import com.tuxlogic.shiftiq.platform.analytics.domain.model.queries.GetNetworkAnalyticsOverviewQuery;
import com.tuxlogic.shiftiq.platform.analytics.domain.repositories.BranchAnalyticsRepository;
import com.tuxlogic.shiftiq.platform.analytics.domain.services.CustomerDashboardPort;
import com.tuxlogic.shiftiq.platform.analytics.domain.services.EmployeeDashboardPort;
import com.tuxlogic.shiftiq.platform.shared.domain.model.valueobjects.BranchId;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

class AnalyticsQueryServiceImplTest {

    private BranchAnalyticsRepository repository;
    private CustomerDashboardPort customerDashboardPort;
    private EmployeeDashboardPort employeeDashboardPort;
    private AnalyticsQueryServiceImpl queryService;

    @BeforeEach
    void setUp() {
        repository = Mockito.mock(BranchAnalyticsRepository.class);
        customerDashboardPort = Mockito.mock(CustomerDashboardPort.class);
        employeeDashboardPort = Mockito.mock(EmployeeDashboardPort.class);
        queryService = new AnalyticsQueryServiceImpl(
                repository,
                new AnalyticsClock("America/Lima"),
                customerDashboardPort,
                employeeDashboardPort
        );
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
    @DisplayName("handle GetBranchAnalyticsByDateRangeQuery returns read model DTOs ordered by date")
    void handleDateRange() {
        var branchId = new BranchId(UUID.randomUUID());
        var start = LocalDate.now().minusDays(7);
        var end = LocalDate.now();
        var snapshot = new BranchAnalyticsSnapshot(branchId, end);

        when(repository.findByBranchIdAndSnapshotDateBetweenOrderBySnapshotDateAsc(eq(branchId), eq(start), eq(end)))
                .thenReturn(List.of(snapshot));

        var result = queryService.handle(new GetBranchAnalyticsByDateRangeQuery(branchId, start, end));

        assertThat(result.isSuccess()).isTrue();
        var range = result.success().get();
        assertThat(range).hasSize(1);
        assertThat(range.get(0)).isInstanceOf(DailyAnalyticsSnapshot.class);
        assertThat(range.get(0).branchId()).isEqualTo(branchId.value());
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

        var scope = Set.of(b1.getBranchId().value(), b2.getBranchId().value());
        when(repository.findBySnapshotDateAndBranchIdIn(any(LocalDate.class), eq(scope)))
                .thenReturn(List.of(b1, b2));

        var result = queryService.handle(new GetNetworkAnalyticsOverviewQuery(scope));

        assertThat(result.isSuccess()).isTrue();
        var overview = result.success().get();
        assertThat(overview.totalActiveBranchesCount()).isEqualTo(2);
        assertThat(overview.totalNetworkRevenue()).isEqualTo(new BigDecimal("500.00"));
        assertThat(overview.totalNetworkCompletedWorkOrders()).isEqualTo(2);
    }

    @Test
    @DisplayName("totalActiveBranchesCount counts every branch in scope, not only branches with a snapshot")
    void networkOverviewCountsBranchesWithoutSnapshot() {
        var activeBranch = new BranchId(UUID.randomUUID());
        var withoutActivity = UUID.randomUUID();
        var scope = Set.of(activeBranch.value(), withoutActivity, UUID.randomUUID());

        var snapshot = new BranchAnalyticsSnapshot(activeBranch, LocalDate.now());
        snapshot.addRevenue(new BigDecimal("90.00"));

        when(repository.findBySnapshotDateAndBranchIdIn(any(LocalDate.class), eq(scope)))
                .thenReturn(List.of(snapshot));

        var result = queryService.handle(new GetNetworkAnalyticsOverviewQuery(scope));

        assertThat(result.isSuccess()).isTrue();
        var overview = result.success().get();
        assertThat(overview.totalActiveBranchesCount()).isEqualTo(3);
        assertThat(overview.totalNetworkRevenue()).isEqualTo(new BigDecimal("90.00"));
    }

    @Test
    @DisplayName("handle GetNetworkAnalyticsOverviewQuery only aggregates the caller branch scope")
    void handleNetworkOverviewScopedToBranchIds() {
        var allowedBranch = new BranchId(UUID.randomUUID());

        var allowed = new BranchAnalyticsSnapshot(allowedBranch, LocalDate.now());
        allowed.addRevenue(new BigDecimal("150.00"));

        when(repository.findBySnapshotDateAndBranchIdIn(any(LocalDate.class), any()))
                .thenReturn(List.of(allowed));

        var result = queryService.handle(new GetNetworkAnalyticsOverviewQuery(Set.of(allowedBranch.value())));

        assertThat(result.isSuccess()).isTrue();
        var overview = result.success().get();
        assertThat(overview.totalActiveBranchesCount()).isEqualTo(1);
        assertThat(overview.totalNetworkRevenue()).isEqualTo(new BigDecimal("150.00"));
    }

    @Test
    @DisplayName("handle GetNetworkAnalyticsOverviewQuery returns zeros when the scope is empty")
    void handleNetworkOverviewWithEmptyScopeDoesNotQuery() {
        var result = queryService.handle(new GetNetworkAnalyticsOverviewQuery(Set.of()));

        assertThat(result.isSuccess()).isTrue();
        var overview = result.success().get();
        assertThat(overview.totalActiveBranchesCount()).isZero();
        assertThat(overview.totalNetworkRevenue()).isZero();
        Mockito.verifyNoInteractions(repository);
    }

    @Test
    @DisplayName("handle GetNetworkAnalyticsOverviewQuery rejects a null scope")
    void handleNetworkOverviewRejectsNullScope() {
        assertThatThrownBy(() -> new GetNetworkAnalyticsOverviewQuery(null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("analytics.error.branchScope.required");
    }

    @Test
    @DisplayName("handle returns failure when the repository raises an unexpected error")
    void handleReturnsFailureOnRepositoryError() {
        var branchId = new BranchId(UUID.randomUUID());
        when(repository.findByBranchIdAndSnapshotDate(eq(branchId), any(LocalDate.class)))
                .thenThrow(new RuntimeException("boom"));

        var result = queryService.handle(new GetBranchAnalyticsSummaryQuery(branchId));

        assertThat(result.isFailure()).isTrue();
        assertThat(result.failure().get()).isEqualTo("analytics.error.retrievalFailed");
    }

    @Test
    @DisplayName("handle GetCustomerDashboardSummaryQuery delegates to CustomerDashboardPort")
    void handleCustomerDashboardSummarySuccess() {
        var userId = UUID.randomUUID();
        var summary = new CustomerDashboardSummary(
                UUID.randomUUID(), 2, 1, 1, 1, 0, List.of(), List.of(), List.of());
        when(customerDashboardPort.loadSummaryForUser(userId)).thenReturn(Optional.of(summary));

        var result = queryService.handle(new GetCustomerDashboardSummaryQuery(userId));

        assertThat(result.isSuccess()).isTrue();
        assertThat(result.success().get()).isEqualTo(summary);
    }

    @Test
    @DisplayName("handle GetCustomerDashboardSummaryQuery returns customerNotFound when port returns empty")
    void handleCustomerDashboardSummaryNotFound() {
        var userId = UUID.randomUUID();
        when(customerDashboardPort.loadSummaryForUser(userId)).thenReturn(Optional.empty());

        var result = queryService.handle(new GetCustomerDashboardSummaryQuery(userId));

        assertThat(result.isFailure()).isTrue();
        assertThat(result.failure().get()).isEqualTo("analytics.error.customerNotFound");
    }

    @Test
    @DisplayName("handle GetEmployeeDashboardSummaryQuery delegates to EmployeeDashboardPort")
    void handleEmployeeDashboardSummarySuccess() {
        var userId = UUID.randomUUID();
        var summary = new EmployeeDashboardSummary(
                UUID.randomUUID(), List.of(UUID.randomUUID()), 3, 2, 4, 1, List.of());
        when(employeeDashboardPort.loadSummaryForUser(userId)).thenReturn(Optional.of(summary));

        var result = queryService.handle(new GetEmployeeDashboardSummaryQuery(userId));

        assertThat(result.isSuccess()).isTrue();
        assertThat(result.success().get()).isEqualTo(summary);
    }
}
