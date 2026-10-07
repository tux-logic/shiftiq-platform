package com.tuxlogic.shiftiq.platform.analytics.interfaces.rest;

import com.tuxlogic.shiftiq.platform.analytics.application.internal.dto.DailyAnalyticsSnapshot;
import com.tuxlogic.shiftiq.platform.analytics.application.internal.support.AnalyticsClock;
import com.tuxlogic.shiftiq.platform.analytics.application.queryservices.AnalyticsQueryService;
import com.tuxlogic.shiftiq.platform.analytics.domain.model.queries.BranchAnalyticsSummary;
import com.tuxlogic.shiftiq.platform.analytics.domain.model.queries.GetBranchAnalyticsByDateRangeQuery;
import com.tuxlogic.shiftiq.platform.analytics.domain.model.queries.GetBranchAnalyticsSummaryQuery;
import com.tuxlogic.shiftiq.platform.analytics.domain.model.queries.GetNetworkAnalyticsOverviewQuery;
import com.tuxlogic.shiftiq.platform.analytics.domain.model.queries.NetworkAnalyticsOverview;
import com.tuxlogic.shiftiq.platform.analytics.interfaces.rest.resources.BranchAnalyticsSnapshotResource;
import com.tuxlogic.shiftiq.platform.analytics.interfaces.rest.resources.BranchAnalyticsSummaryResource;
import com.tuxlogic.shiftiq.platform.analytics.interfaces.rest.resources.NetworkAnalyticsOverviewResource;
import com.tuxlogic.shiftiq.platform.shared.application.result.Result;
import com.tuxlogic.shiftiq.platform.shared.infrastructure.security.MultiTenancySecurityService;
import com.tuxlogic.shiftiq.platform.shared.interfaces.rest.resources.ErrorResource;
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
        controller = new AnalyticsController(queryService, securityService, new AnalyticsClock("America/Lima"));
    }

    @Test
    @DisplayName("getBranchSummary returns 403 with standard error body when user not authorized for branch")
    void getBranchSummaryUnauthorized() {
        var branchId = UUID.randomUUID();
        when(securityService.isAuthorizedForBranch(branchId)).thenReturn(false);

        ResponseEntity<?> response = controller.getBranchSummary(branchId);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
        assertThat(response.getBody()).isInstanceOf(ErrorResource.class);
        assertThat(((ErrorResource) response.getBody()).code()).isEqualTo("ACCESS_DENIED");
        Mockito.verify(queryService, Mockito.never()).handle(any(GetBranchAnalyticsSummaryQuery.class));
    }

    @Test
    @DisplayName("getBranchSummary returns 404 with standard error body when the branch does not exist")
    void getBranchSummaryBranchNotFound() {
        var branchId = UUID.randomUUID();
        when(securityService.isAuthorizedForBranch(branchId)).thenReturn(true);
        when(securityService.branchExists(branchId)).thenReturn(false);

        ResponseEntity<?> response = controller.getBranchSummary(branchId);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(response.getBody()).isInstanceOf(ErrorResource.class);
        assertThat(((ErrorResource) response.getBody()).code()).isEqualTo("BRANCH_NOT_FOUND");
        Mockito.verify(queryService, Mockito.never()).handle(any(GetBranchAnalyticsSummaryQuery.class));
    }

    @Test
    @DisplayName("getBranchSummary returns 500 with standard error body when the query fails")
    void getBranchSummaryInternalFailure() {
        var branchId = UUID.randomUUID();
        when(securityService.isAuthorizedForBranch(branchId)).thenReturn(true);
        when(securityService.branchExists(branchId)).thenReturn(true);
        when(queryService.handle(any(GetBranchAnalyticsSummaryQuery.class)))
                .thenReturn(Result.failure("analytics.error.retrievalFailed"));

        ResponseEntity<?> response = controller.getBranchSummary(branchId);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
        assertThat(response.getBody()).isInstanceOf(ErrorResource.class);
        assertThat(((ErrorResource) response.getBody()).code()).isEqualTo("UNEXPECTED_ERROR");
    }

    @Test
    @DisplayName("getBranchSummary returns 200 OK with summary resource")
    void getBranchSummarySuccess() {
        var branchId = UUID.randomUUID();
        when(securityService.isAuthorizedForBranch(branchId)).thenReturn(true);
        when(securityService.branchExists(branchId)).thenReturn(true);

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
    @DisplayName("getBranchFinancialRange returns 200 OK with list of snapshot resources")
    void getBranchFinancialRangeSuccess() {
        var branchId = UUID.randomUUID();
        when(securityService.isAuthorizedForBranch(branchId)).thenReturn(true);
        when(securityService.branchExists(branchId)).thenReturn(true);

        var snapshot = new DailyAnalyticsSnapshot(
                UUID.randomUUID(), branchId, LocalDate.now(),
                new BigDecimal("100.00"), 1, 2, 0, 0);
        when(queryService.handle(any(GetBranchAnalyticsByDateRangeQuery.class)))
                .thenReturn(Result.success(List.of(snapshot)));

        ResponseEntity<?> response = controller.getBranchFinancialRange(branchId, null, null);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isInstanceOf(List.class);
        var body = (java.util.List<?>) response.getBody();
        assertThat(body).hasSize(1);
        assertThat(body.get(0)).isInstanceOf(BranchAnalyticsSnapshotResource.class);
        var resource = (BranchAnalyticsSnapshotResource) body.get(0);
        assertThat(resource.branchId()).isEqualTo(branchId);
        assertThat(resource.totalRevenue()).isEqualTo(new BigDecimal("100.00"));
    }

    @Test
    @DisplayName("getBranchFinancialRange rejects an inverted date range with 400")
    void getBranchFinancialRangeRejectsInvertedRange() {
        var branchId = UUID.randomUUID();
        when(securityService.isAuthorizedForBranch(branchId)).thenReturn(true);
        when(securityService.branchExists(branchId)).thenReturn(true);

        assertThat(org.junit.jupiter.api.Assertions.assertThrows(IllegalArgumentException.class,
                        () -> controller.getBranchFinancialRange(
                                branchId,
                                LocalDate.now(),
                                LocalDate.now().minusDays(1))))
                .hasMessage("analytics.error.invalidDateRange");
        Mockito.verify(queryService, Mockito.never()).handle(any(GetBranchAnalyticsByDateRangeQuery.class));
    }

    @Test
    @DisplayName("getBranchFinancialRange returns 403 with standard error body when user not authorized")
    void getBranchFinancialRangeUnauthorized() {
        var branchId = UUID.randomUUID();
        when(securityService.isAuthorizedForBranch(branchId)).thenReturn(false);

        ResponseEntity<?> response = controller.getBranchFinancialRange(branchId, null, null);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
        assertThat(response.getBody()).isInstanceOf(ErrorResource.class);
        assertThat(((ErrorResource) response.getBody()).code()).isEqualTo("ACCESS_DENIED");
        Mockito.verify(queryService, Mockito.never()).handle(any(GetBranchAnalyticsByDateRangeQuery.class));
    }

    @Test
    @DisplayName("getNetworkOverview is restricted to admin and owner roles at method level")
    void getNetworkOverviewRequiresAdminOrOwnerRole() throws NoSuchMethodException {
        var method = AnalyticsController.class.getMethod("getNetworkOverview");
        var preAuthorize = method.getAnnotation(org.springframework.security.access.prepost.PreAuthorize.class);

        assertThat(preAuthorize).isNotNull();
        assertThat(preAuthorize.value()).isEqualTo("hasRole('ADMIN') or hasRole('OWNER')");
    }

    @Test
    @DisplayName("getNetworkOverview returns 200 OK with overview resource scoped to the caller branches")
    void getNetworkOverviewSuccess() {
        var scope = Set.of(UUID.randomUUID(), UUID.randomUUID());
        when(securityService.resolveNetworkAccessibleBranchIds()).thenReturn(scope);
        var overview = new NetworkAnalyticsOverview(2, new BigDecimal("5000.00"), 15, 20, 2);
        when(queryService.handle(any(GetNetworkAnalyticsOverviewQuery.class)))
                .thenReturn(Result.success(overview));

        ResponseEntity<?> response = controller.getNetworkOverview();

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isInstanceOf(NetworkAnalyticsOverviewResource.class);
        Mockito.verify(queryService).handle(eq(new GetNetworkAnalyticsOverviewQuery(scope)));
    }

    @Test
    @DisplayName("getNetworkOverview passes the full branch scope for platform admins")
    void getNetworkOverviewFullScopeForAdmins() {
        var allBranches = Set.of(UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID());
        when(securityService.resolveNetworkAccessibleBranchIds()).thenReturn(allBranches);
        var overview = new NetworkAnalyticsOverview(3, new BigDecimal("5000.00"), 15, 20, 2);
        when(queryService.handle(any(GetNetworkAnalyticsOverviewQuery.class)))
                .thenReturn(Result.success(overview));

        ResponseEntity<?> response = controller.getNetworkOverview();

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        Mockito.verify(queryService).handle(eq(new GetNetworkAnalyticsOverviewQuery(allBranches)));
    }

    @Test
    @DisplayName("getNetworkOverview returns 500 with standard error body when the query fails")
    void getNetworkOverviewInternalFailure() {
        when(securityService.resolveNetworkAccessibleBranchIds()).thenReturn(Set.of(UUID.randomUUID()));
        when(queryService.handle(any(GetNetworkAnalyticsOverviewQuery.class)))
                .thenReturn(Result.failure("analytics.error.retrievalFailed"));

        ResponseEntity<?> response = controller.getNetworkOverview();

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
        assertThat(response.getBody()).isInstanceOf(ErrorResource.class);
        assertThat(((ErrorResource) response.getBody()).code()).isEqualTo("UNEXPECTED_ERROR");
    }
}
