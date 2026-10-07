package com.tuxlogic.shiftiq.platform.analytics.application.internal.services;

import com.tuxlogic.shiftiq.platform.analytics.application.internal.support.AnalyticsClock;
import com.tuxlogic.shiftiq.platform.analytics.domain.model.valueobjects.AnalyticsDelta;
import com.tuxlogic.shiftiq.platform.shared.domain.model.valueobjects.BranchId;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

class AnalyticsDeltaDispatcherTest {

    private BranchAnalyticsSnapshotService snapshotService;
    private AnalyticsDeltaDispatcher dispatcher;
    private BranchId branchId;

    @BeforeEach
    void setUp() {
        snapshotService = Mockito.mock(BranchAnalyticsSnapshotService.class);
        dispatcher = new AnalyticsDeltaDispatcher(snapshotService, new AnalyticsClock("America/Lima"));
        branchId = new BranchId(UUID.randomUUID());
    }

    @Test
    @DisplayName("dispatch applies the delta when the snapshot write succeeds")
    void dispatchAppliesDelta() {
        var delta = new AnalyticsDelta(new BigDecimal("10.00"), 1, 0, 0);

        dispatcher.dispatch(branchId, delta);

        verify(snapshotService).applyDelta(branchId, delta);
        verify(snapshotService, Mockito.never()).enqueueDelta(any(), any(), any(), Mockito.anyBoolean());
    }

    @Test
    @DisplayName("dispatch queues the delta when the snapshot write fails")
    void dispatchQueuesDeltaOnFailure() {
        var delta = new AnalyticsDelta(new BigDecimal("10.00"), 1, 0, 0);
        doThrow(new RuntimeException("db down")).when(snapshotService).applyDelta(branchId, delta);

        dispatcher.dispatch(branchId, delta);

        verify(snapshotService).enqueueDelta(eq(branchId), any(LocalDate.class), eq(delta), eq(false));
    }

    @Test
    @DisplayName("dispatch reports the lost change when even the queue write fails")
    void dispatchSwallowsQueueFailure() {
        var delta = AnalyticsDelta.zero();
        doThrow(new RuntimeException("db down")).when(snapshotService).applyDelta(branchId, delta);
        doThrow(new RuntimeException("db down")).when(snapshotService).enqueueDelta(any(), any(), any(), Mockito.anyBoolean());

        dispatcher.dispatch(branchId, delta);

        verify(snapshotService).applyDelta(branchId, delta);
        verify(snapshotService).enqueueDelta(eq(branchId), any(LocalDate.class), eq(delta), eq(false));
    }

    @Test
    @DisplayName("dispatchLowStockRecompute applies the count or queues it for replay")
    void dispatchLowStockRecompute() {
        dispatcher.dispatchLowStockRecompute(branchId);
        verify(snapshotService).applyLowStockRecompute(branchId);

        doThrow(new RuntimeException("db down")).when(snapshotService).applyLowStockRecompute(branchId);
        dispatcher.dispatchLowStockRecompute(branchId);
        verify(snapshotService).enqueueDelta(eq(branchId), any(LocalDate.class), eq(AnalyticsDelta.zero()), eq(true));
    }

    @Test
    @DisplayName("dispatch ignores a null branch or delta")
    void dispatchIgnoresNulls() {
        dispatcher.dispatch(null, AnalyticsDelta.zero());
        dispatcher.dispatch(branchId, null);
        dispatcher.dispatchLowStockRecompute(null);

        verifyNoInteractions(snapshotService);
    }
}
