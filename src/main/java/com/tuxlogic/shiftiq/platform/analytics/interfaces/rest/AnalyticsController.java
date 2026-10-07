package com.tuxlogic.shiftiq.platform.analytics.interfaces.rest;

import com.tuxlogic.shiftiq.platform.analytics.application.queryservices.AnalyticsQueryService;
import com.tuxlogic.shiftiq.platform.analytics.domain.model.queries.GetBranchAnalyticsByDateRangeQuery;
import com.tuxlogic.shiftiq.platform.analytics.domain.model.queries.GetBranchAnalyticsSummaryQuery;
import com.tuxlogic.shiftiq.platform.analytics.domain.model.queries.GetNetworkAnalyticsOverviewQuery;
import com.tuxlogic.shiftiq.platform.analytics.interfaces.rest.resources.BranchAnalyticsSnapshotResource;
import com.tuxlogic.shiftiq.platform.analytics.interfaces.rest.resources.BranchAnalyticsSummaryResource;
import com.tuxlogic.shiftiq.platform.analytics.interfaces.rest.resources.NetworkAnalyticsOverviewResource;
import com.tuxlogic.shiftiq.platform.shared.domain.model.valueobjects.BranchId;
import com.tuxlogic.shiftiq.platform.shared.infrastructure.security.MultiTenancySecurityService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.UUID;

@RestController
@RequestMapping(value = "/api/v1/analytics", produces = "application/json")
@Tag(name = "Analytics", description = "Analytics and Key Performance Indicators (KPIs) Endpoints")
@PreAuthorize("isAuthenticated()")
public class AnalyticsController {

    private final AnalyticsQueryService queryService;
    private final MultiTenancySecurityService multiTenancySecurityService;

    public AnalyticsController(AnalyticsQueryService queryService, MultiTenancySecurityService multiTenancySecurityService) {
        this.queryService = queryService;
        this.multiTenancySecurityService = multiTenancySecurityService;
    }

    @GetMapping("/branches/{branchId}/summary")
    @Operation(summary = "Get branch analytics summary", description = "Retrieves current day executive KPI summary for a specific branch")
    public ResponseEntity<?> getBranchSummary(@PathVariable UUID branchId) {
        if (!multiTenancySecurityService.isAuthorizedForBranch(branchId)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        var query = new GetBranchAnalyticsSummaryQuery(new BranchId(branchId));
        var result = queryService.handle(query);
        if (result.isFailure()) {
            return ResponseEntity.badRequest().body(result.failure().get());
        }
        var summary = result.success().get();
        var resource = new BranchAnalyticsSummaryResource(
                summary.branchId(),
                summary.totalRevenue(),
                summary.completedWorkOrdersCount(),
                summary.totalAppointmentsCount(),
                summary.lowStockAlertsCount(),
                summary.dtcAlertsCount()
        );
        return ResponseEntity.ok(resource);
    }

    @GetMapping("/branches/{branchId}/financial")
    @Operation(summary = "Get branch financial and operational analytics by date range", description = "Retrieves daily snapshots for a specific branch over a date range")
    public ResponseEntity<?> getBranchFinancialRange(
            @PathVariable UUID branchId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        if (!multiTenancySecurityService.isAuthorizedForBranch(branchId)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        var start = startDate != null ? startDate : LocalDate.now().minusDays(30);
        var end = endDate != null ? endDate : LocalDate.now();

        var query = new GetBranchAnalyticsByDateRangeQuery(new BranchId(branchId), start, end);
        var result = queryService.handle(query);
        if (result.isFailure()) {
            return ResponseEntity.badRequest().body(result.failure().get());
        }
        var list = result.success().get().stream()
                .map(s -> new BranchAnalyticsSnapshotResource(
                        s.getId().value(),
                        s.getBranchId().value(),
                        s.getSnapshotDate(),
                        s.getTotalRevenue(),
                        s.getCompletedWorkOrdersCount(),
                        s.getTotalAppointmentsCount(),
                        s.getLowStockAlertsCount(),
                        s.getDtcAlertsCount()
                ))
                .toList();
        return ResponseEntity.ok(list);
    }

    @GetMapping("/network/summary")
    @Operation(summary = "Get network analytics overview", description = "Retrieves consolidated multi-branch analytics overview across the workshop network")
    @PreAuthorize("hasRole('ADMIN') or hasRole('OWNER')")
    public ResponseEntity<?> getNetworkOverview() {
        var branchScope = multiTenancySecurityService.resolveNetworkAccessibleBranchIds();
        var query = new GetNetworkAnalyticsOverviewQuery(branchScope);
        var result = queryService.handle(query);
        if (result.isFailure()) {
            return ResponseEntity.badRequest().body(result.failure().get());
        }
        var overview = result.success().get();
        var resource = new NetworkAnalyticsOverviewResource(
                overview.totalActiveBranchesCount(),
                overview.totalNetworkRevenue(),
                overview.totalNetworkCompletedWorkOrders(),
                overview.totalNetworkAppointments(),
                overview.totalNetworkDtcAlerts()
        );
        return ResponseEntity.ok(resource);
    }
}
