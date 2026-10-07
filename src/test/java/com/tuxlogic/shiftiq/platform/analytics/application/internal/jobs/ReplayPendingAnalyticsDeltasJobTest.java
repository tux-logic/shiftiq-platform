package com.tuxlogic.shiftiq.platform.analytics.application.internal.jobs;

import com.tuxlogic.shiftiq.platform.analytics.application.internal.services.BranchAnalyticsSnapshotService;
import com.tuxlogic.shiftiq.platform.analytics.domain.model.aggregates.PendingAnalyticsDelta;
import com.tuxlogic.shiftiq.platform.analytics.domain.model.valueobjects.AnalyticsDelta;
import com.tuxlogic.shiftiq.platform.analytics.domain.repositories.BranchAnalyticsPendingDeltaRepository;
import com.tuxlogic.shiftiq.platform.shared.domain.model.valueobjects.BranchId;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class ReplayPendingAnalyticsDeltasJobTest {

    private BranchAnalyticsSnapshotService snapshotService;
    private BranchAnalyticsPendingDeltaRepository pendingRepository;
    private ReplayPendingAnalyticsDeltasJob job;
    private BranchId branchId;

    @BeforeEach
    void setUp() {
        snapshotService = Mockito.mock(BranchAnalyticsSnapshotService.class);
        pendingRepository = Mockito.mock(BranchAnalyticsPendingDeltaRepository.class);
        job = new ReplayPendingAnalyticsDeltasJob(snapshotService, pendingRepository, 10);
        branchId = new BranchId(UUID.randomUUID());
    }

    @Test
    @DisplayName("replays every pending delta and leaves nothing when the queue is empty")
    void replaysPendingDeltas() {
        when(pendingRepository.findPending(Mockito.anyInt())).thenReturn(List.of());

        job.replayPendingDeltas();

        verify(snapshotService, never()).replay(any());

        var pending = new PendingAnalyticsDelta(branchId, LocalDate.now(), AnalyticsDelta.zero(), false);
        when(pendingRepository.findPending(Mockito.anyInt())).thenReturn(List.of(pending));

        job.replayPendingDeltas();

        verify(snapshotService).replay(pending);
        verify(snapshotService, never()).recordReplayFailure(any(), any());
    }

    @Test
    @DisplayName("records the failure when a replay cannot be applied")
    void recordsReplayFailure() {
        var pending = new PendingAnalyticsDelta(branchId, LocalDate.now(), AnalyticsDelta.zero(), true);
        when(pendingRepository.findPending(Mockito.anyInt())).thenReturn(List.of(pending));
        doThrow(new RuntimeException("still down")).when(snapshotService).replay(pending);

        job.replayPendingDeltas();

        verify(snapshotService).recordReplayFailure(eq(pending), eq("still down"));
    }

    @Test
    @DisplayName("skips deltas that already reached the maximum number of attempts")
    void skipsDeltaWithMaxAttempts() {
        var pending = new PendingAnalyticsDelta(branchId, LocalDate.now(), AnalyticsDelta.zero(), false);
        for (int i = 0; i < 10; i++) {
            pending.recordFailure("boom");
        }
        assertThat(pending.getAttempts()).isEqualTo(10);
        when(pendingRepository.findPending(Mockito.anyInt())).thenReturn(List.of(pending));

        job.replayPendingDeltas();

        verify(snapshotService, never()).replay(pending);
        verify(snapshotService, never()).recordReplayFailure(any(), any());
    }

    @Test
    @DisplayName("keeps going when the queue itself cannot be read")
    void survivesQueueReadFailure() {
        when(pendingRepository.findPending(Mockito.anyInt())).thenThrow(new RuntimeException("db down"));

        job.replayPendingDeltas();

        verify(snapshotService, never()).replay(any());
    }

    @Test
    @DisplayName("does not mark a failure when recording it also fails")
    void survivesRecordFailureFailure() {
        var pending = new PendingAnalyticsDelta(branchId, LocalDate.now(), AnalyticsDelta.zero(), false);
        when(pendingRepository.findPending(Mockito.anyInt())).thenReturn(List.of(pending));
        doThrow(new RuntimeException("still down")).when(snapshotService).replay(pending);
        doThrow(new RuntimeException("still down")).when(snapshotService).recordReplayFailure(any(), any());

        job.replayPendingDeltas();

        verify(snapshotService).recordReplayFailure(eq(pending), eq("still down"));
    }
}
