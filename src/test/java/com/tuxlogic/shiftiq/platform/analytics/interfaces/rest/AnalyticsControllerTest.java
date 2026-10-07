package com.tuxlogic.shiftiq.platform.analytics.interfaces.rest;

import com.tuxlogic.shiftiq.platform.analytics.application.queryservices.AnalyticsQueryService;
import com.tuxlogic.shiftiq.platform.analytics.domain.model.aggregates.BranchAnalyticsSnapshot;
import com.tuxlogic.shiftiq.platform.analytics.domain.model.queries.BranchAnalyticsSummary;
import com.tuxlogic.shiftiq.platform.analytics.domain.model.queries.GetBranchAnalyticsByDateRangeQuery;
import com.tuxlogic.shiftiq.platform.analytics.domain.model.queries.GetBranchAnalyticsSummaryQuery;
import com.tuxlogic.shiftiq.platform.analytics.domain.model.queries.GetNetworkAnalyticsOverviewQuery;
import com.tuxlogic.shiftiq.platform.analytics.domain.model.queries.NetworkAnalyticsOverview;
import com.tuxlogic.shiftiq.platform.analytics.interfaces.rest.resources.BranchAnalyticsSummaryResource;
import com.tuxlogic.shiftiq.platform.analytics.interfaces.rest.resources.NetworkAnalyticsOverviewResource;
import com.tuxlogic.shiftiq.platform.shared.application.result.Result;
import com.tuxlogic.shiftiq.platform.shared.domain.model.valueobjects.BranchId;
import com.tuxlogic.shiftiq.platform.shared.infrastructure.security.MultiTenancySecurityService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

class AnalyticsControllerTest {

    private AnalyticsQueryService queryService;
    private MultiTenancySecurityService securityService;
    private AnalyticsController controller;

    @BeforeEach
    void setUp() {
        queryService = Mockito.mock(AnalyticsQueryService.class);
        securityService = Mockito.mock(MultiTenancySecurityService.class);
        controller = new AnalyticsController(queryService, securityService);
    }

    @Test
    @DisplayName("getBranchSummary returns 403 when user not authorized for branch")
    void getBranchSummaryUnauthorized() {
        var branchId = UUID.randomUUID();
        when(securityService.isAuthorizedForBranch(branchId)).thenReturn(false);

        ResponseEntity<?> response = controller.getBranchSummary(branchId);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
    }

    @Test
    @DisplayName("getBranchSummary returns 200 OK with summary resource")
    void getBranchSummarySuccess() {
        var branchId = UUID.randomUUID();
        when(securityService.isAuthorizedForBranch(branchId)).thenReturn(true);

        var summary = new BranchAnalyticsSummary(branchId, new BigDecimal("1200.00"), 4, 6, 1, 0);
        when(queryService.handle(any(GetBranchAnalyticsSummaryQuery.class)))
                .thenReturn(Result.success(summary));

        ResponseEntity<?> response = controller.getBranchSummary(branchId);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isInstanceOf(BranchAnalyticsSummaryResource.class);
        var body = (BranchAnalyticsSummaryResource) response.getBody();
        assertThat(body.totalRevenue()).isEqualTo(new BigDecimal("1200.00"));
        assertThat(body.completedWorkOrdersCount()).isEqualTo(4);
    }

    @Test
    @DisplayName("getBranchFinancialRange returns 200 OK with list of snapshots")
    void getBranchFinancialRangeSuccess() {
        var branchId = UUID.randomUUID();
        when(securityService.isAuthorizedForBranch(branchId)).thenReturn(true);

        var snapshot = new BranchAnalyticsSnapshot(new BranchId(branchId), LocalDate.now());
        when(queryService.handle(any(GetBranchAnalyticsByDateRangeQuery.class)))
                .thenReturn(Result.success(List.of(snapshot)));

        ResponseEntity<?> response = controller.getBranchFinancialRange(branchId, null, null);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isInstanceOf(List.class);
    }

    @Test
    @DisplayName("getNetworkOverview returns 200 OK with overview resource scoped to the caller branches")
    void getNetworkOverviewSuccess() {
        var scope = Set.of(UUID.randomUUID(), UUID.randomUUID());
        when(securityService.resolveNetworkAccessibleBranchIds()).thenReturn(scope);
        var overview = new NetworkAnalyticsOverview(3, new BigDecimal("5000.00"), 15, 20, 2);
        when(queryService.handle(any(GetNetworkAnalyticsOverviewQuery.class)))
                .thenReturn(Result.success(overview));

        ResponseEntity<?> response = controller.getNetworkOverview();

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isInstanceOf(NetworkAnalyticsOverviewResource.class);
        Mockito.verify(queryService).handle(eq(new GetNetworkAnalyticsOverviewQuery(scope)));
    }

    @Test
    @DisplayName("getNetworkOverview passes an unrestricted scope for platform admins")
    void getNetworkOverviewUnrestrictedScopeForAdmins() {
        when(securityService.resolveNetworkAccessibleBranchIds()).thenReturn(null);
        var overview = new NetworkAnalyticsOverview(3, new BigDecimal("5000.00"), 15, 20, 2);
        when(queryService.handle(any(GetNetworkAnalyticsOverviewQuery.class)))
                .thenReturn(Result.success(overview));

        ResponseEntity<?> response = controller.getNetworkOverview();

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        Mockito.verify(queryService).handle(eq(new GetNetworkAnalyticsOverviewQuery(null)));
    }
}
