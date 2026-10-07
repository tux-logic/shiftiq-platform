package com.tuxlogic.shiftiq.platform.analytics.application.internal.queryservices;

import com.tuxlogic.shiftiq.platform.analytics.application.queryservices.AnalyticsQueryService;
import com.tuxlogic.shiftiq.platform.analytics.application.internal.dto.DailyAnalyticsSnapshot;
import com.tuxlogic.shiftiq.platform.analytics.application.internal.support.AnalyticsClock;
import com.tuxlogic.shiftiq.platform.analytics.domain.model.aggregates.BranchAnalyticsSnapshot;
import com.tuxlogic.shiftiq.platform.analytics.domain.model.queries.BranchAnalyticsSummary;
import com.tuxlogic.shiftiq.platform.analytics.domain.model.queries.GetBranchAnalyticsByDateRangeQuery;
import com.tuxlogic.shiftiq.platform.analytics.domain.model.queries.GetBranchAnalyticsSummaryQuery;
import com.tuxlogic.shiftiq.platform.analytics.domain.model.queries.GetNetworkAnalyticsOverviewQuery;
import com.tuxlogic.shiftiq.platform.analytics.domain.model.queries.NetworkAnalyticsOverview;
import com.tuxlogic.shiftiq.platform.analytics.domain.repositories.BranchAnalyticsRepository;
import com.tuxlogic.shiftiq.platform.shared.application.result.Result;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;

@Slf4j
@Service
public class AnalyticsQueryServiceImpl implements AnalyticsQueryService {

    private final BranchAnalyticsRepository repository;
    private final AnalyticsClock clock;

    public AnalyticsQueryServiceImpl(BranchAnalyticsRepository repository, AnalyticsClock clock) {
        this.repository = repository;
        this.clock = clock;
    }

    @Override
    public Result<BranchAnalyticsSummary, String> handle(GetBranchAnalyticsSummaryQuery query) {
        try {
            var today = clock.today();
            var snapshotOpt = repository.findByBranchIdAndSnapshotDate(query.branchId(), today);
            
            var summary = snapshotOpt.map(s -> new BranchAnalyticsSummary(
                    s.getBranchId().value(),
                    s.getTotalRevenue(),
                    s.getCompletedWorkOrdersCount(),
                    s.getTotalAppointmentsCount(),
                    s.getLowStockAlertsCount(),
                    s.getDtcAlertsCount()
            )).orElseGet(() -> new BranchAnalyticsSummary(
                    query.branchId().value(),
                    BigDecimal.ZERO, 0, 0, 0, 0
            ));

            return Result.success(summary);
        } catch (Exception ex) {
            log.error("Error retrieving branch analytics summary for branch {}: {}", query.branchId().value(), ex.getMessage());
            return Result.failure("analytics.error.retrievalFailed");
        }
    }

    @Override
    public Result<List<DailyAnalyticsSnapshot>, String> handle(GetBranchAnalyticsByDateRangeQuery query) {
        try {
            var list = repository.findByBranchIdAndSnapshotDateBetweenOrderBySnapshotDateAsc(query.branchId(), query.startDate(), query.endDate())
                    .stream()
                    .map(DailyAnalyticsSnapshot::from)
                    .toList();
            return Result.success(list);
        } catch (Exception ex) {
            log.error("Error retrieving branch analytics range for branch {}: {}", query.branchId().value(), ex.getMessage());
            return Result.failure("analytics.error.retrievalFailed");
        }
    }

    @Override
    public Result<NetworkAnalyticsOverview, String> handle(GetNetworkAnalyticsOverviewQuery query) {
        try {
            if (query.branchIds().isEmpty()) {
                return Result.success(new NetworkAnalyticsOverview(0, BigDecimal.ZERO, 0, 0, 0));
            }
            var today = clock.today();
            var snapshots = repository.findBySnapshotDateAndBranchIdIn(today, query.branchIds());

            var totalRevenue = snapshots.stream()
                    .map(BranchAnalyticsSnapshot::getTotalRevenue)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            var totalWorkOrders = snapshots.stream()
                    .mapToInt(BranchAnalyticsSnapshot::getCompletedWorkOrdersCount)
                    .sum();

            var totalAppointments = snapshots.stream()
                    .mapToInt(BranchAnalyticsSnapshot::getTotalAppointmentsCount)
                    .sum();

            var totalDtcAlerts = snapshots.stream()
                    .mapToInt(BranchAnalyticsSnapshot::getDtcAlertsCount)
                    .sum();

            var overview = new NetworkAnalyticsOverview(
                    query.branchIds().size(),
                    totalRevenue,
                    totalWorkOrders,
                    totalAppointments,
                    totalDtcAlerts
            );

            return Result.success(overview);
        } catch (Exception ex) {
            log.error("Error retrieving network analytics overview: {}", ex.getMessage());
            return Result.failure("analytics.error.retrievalFailed");
        }
    }
}
