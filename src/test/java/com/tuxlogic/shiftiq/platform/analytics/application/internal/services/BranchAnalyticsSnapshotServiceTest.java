package com.tuxlogic.shiftiq.platform.analytics.application.internal.services;

import com.tuxlogic.shiftiq.platform.analytics.application.internal.support.AnalyticsClock;
import com.tuxlogic.shiftiq.platform.analytics.domain.model.aggregates.PendingAnalyticsDelta;
import com.tuxlogic.shiftiq.platform.analytics.domain.model.valueobjects.AnalyticsDelta;
import com.tuxlogic.shiftiq.platform.analytics.domain.repositories.BranchAnalyticsPendingDeltaRepository;
import com.tuxlogic.shiftiq.platform.analytics.domain.repositories.BranchAnalyticsRepository;
import com.tuxlogic.shiftiq.platform.inventory.application.queryservices.ProductQueryService;
import com.tuxlogic.shiftiq.platform.inventory.domain.model.aggregates.Product;
import com.tuxlogic.shiftiq.platform.inventory.domain.model.queries.GetProductsByBranchIdQuery;
import com.tuxlogic.shiftiq.platform.shared.domain.model.valueobjects.BranchId;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

class BranchAnalyticsSnapshotServiceTest {

    private BranchAnalyticsRepository repository;
    private BranchAnalyticsPendingDeltaRepository pendingRepository;
    private ProductQueryService productQueryService;
    private BranchAnalyticsSnapshotService service;
    private BranchId branchId;

    @BeforeEach
    void setUp() {
        repository = Mockito.mock(BranchAnalyticsRepository.class);
        pendingRepository = Mockito.mock(BranchAnalyticsPendingDeltaRepository.class);
        productQueryService = Mockito.mock(ProductQueryService.class);
        service = new BranchAnalyticsSnapshotService(
                repository, pendingRepository, new AnalyticsClock("America/Lima"), productQueryService);
        branchId = new BranchId(UUID.randomUUID());
    }

    @Test
    @DisplayName("applyDelta delegates an atomic update for the business day")
    void applyDeltaDelegatesToRepository() {
        var delta = new AnalyticsDelta(new BigDecimal("100.00"), 1, 0, 0);

        service.applyDelta(branchId, delta);

        Mockito.verify(repository).applyDelta(Mockito.eq(branchId), any(LocalDate.class), Mockito.eq(delta));
    }

    @Test
    @DisplayName("applyDelta fails closed on a null branch")
    void applyDeltaRejectsNullBranch() {
        assertThatThrownBy(() -> service.applyDelta(null, AnalyticsDelta.zero()))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("analytics.error.branchId.required");
        Mockito.verifyNoInteractions(repository);
    }

    @Test
    @DisplayName("applyLowStockRecompute stores the real number of low stock products")
    void applyLowStockRecomputeCountsInventory() {
        Product lowStock = Mockito.mock(Product.class);
        when(productQueryService.handle(any(GetProductsByBranchIdQuery.class))).thenReturn(List.of(lowStock, lowStock));

        service.applyLowStockRecompute(branchId);

        Mockito.verify(repository).setLowStockAlertsCount(Mockito.eq(branchId), any(LocalDate.class), Mockito.eq(2));
    }

    @Test
    @DisplayName("enqueueDelta stores the change waiting for replay")
    void enqueueDeltaStoresPendingRow() {
        var delta = new AnalyticsDelta(BigDecimal.TEN, 0, 2, 0);
        var today = LocalDate.now();

        service.enqueueDelta(branchId, today, delta, false);

        var captor = ArgumentCaptor.forClass(PendingAnalyticsDelta.class);
        Mockito.verify(pendingRepository).save(captor.capture());
        assertThat(captor.getValue().getBranchId()).isEqualTo(branchId);
        assertThat(captor.getValue().getDeltaDate()).isEqualTo(today);
        assertThat(captor.getValue().getDelta()).isEqualTo(delta);
        assertThat(captor.getValue().isLowStockRecompute()).isFalse();
        assertThat(captor.getValue().getAttempts()).isZero();
    }

    @Test
    @DisplayName("replay applies the delta and removes the pending row in the same transaction")
    void replayAppliesAndDeletes() {
        var pending = new PendingAnalyticsDelta(branchId, LocalDate.now(), new AnalyticsDelta(BigDecimal.ONE, 1, 0, 0), false);

        service.replay(pending);

        Mockito.verify(repository).applyDelta(branchId, pending.getDeltaDate(), pending.getDelta());
        Mockito.verify(pendingRepository).delete(pending);
        Mockito.verify(repository, Mockito.never()).setLowStockAlertsCount(any(), any(), Mockito.anyInt());
    }

    @Test
    @DisplayName("replay of a low stock row recomputes from inventory before deleting")
    void replayRecomputesLowStock() {
        when(productQueryService.handle(any(GetProductsByBranchIdQuery.class))).thenReturn(List.of());
        var pending = new PendingAnalyticsDelta(branchId, LocalDate.now(), AnalyticsDelta.zero(), true);

        service.replay(pending);

        Mockito.verify(repository).applyDelta(branchId, pending.getDeltaDate(), AnalyticsDelta.zero());
        Mockito.verify(repository).setLowStockAlertsCount(branchId, pending.getDeltaDate(), 0);
        Mockito.verify(pendingRepository).delete(pending);
    }

    @Test
    @DisplayName("recordReplayFailure increments the attempts of the pending row")
    void recordReplayFailureIncrementsAttempts() {
        var pending = new PendingAnalyticsDelta(branchId, LocalDate.now(), AnalyticsDelta.zero(), false);

        service.recordReplayFailure(pending, "boom");

        assertThat(pending.getAttempts()).isEqualTo(1);
        assertThat(pending.getLastError()).isEqualTo("boom");
        Mockito.verify(pendingRepository).save(pending);
    }
}
