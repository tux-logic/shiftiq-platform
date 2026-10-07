package com.tuxlogic.shiftiq.platform.analytics.application.internal.jobs;

import com.tuxlogic.shiftiq.platform.analytics.application.internal.services.BranchAnalyticsSnapshotService;
import com.tuxlogic.shiftiq.platform.analytics.domain.model.aggregates.PendingAnalyticsDelta;
import com.tuxlogic.shiftiq.platform.analytics.domain.repositories.BranchAnalyticsPendingDeltaRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Replays analytics changes that failed to apply and were queued by
 * {@code AnalyticsDeltaDispatcher}. Each row is applied and deleted in a single
 * transaction, so a crash cannot double count a delta.
 */
@Component
public class ReplayPendingAnalyticsDeltasJob {

    private static final Logger log = LoggerFactory.getLogger(ReplayPendingAnalyticsDeltasJob.class);
    private static final int BATCH_SIZE = 100;

    private final BranchAnalyticsSnapshotService snapshotService;
    private final BranchAnalyticsPendingDeltaRepository pendingRepository;
    private final int maxAttempts;

    public ReplayPendingAnalyticsDeltasJob(BranchAnalyticsSnapshotService snapshotService,
                                           BranchAnalyticsPendingDeltaRepository pendingRepository,
                                           @Value("${analytics.replay-deltas.max-attempts:10}") int maxAttempts) {
        this.snapshotService = snapshotService;
        this.pendingRepository = pendingRepository;
        this.maxAttempts = maxAttempts;
    }

    @Scheduled(cron = "${analytics.replay-deltas.cron:0 */5 * * * *}")
    public void replayPendingDeltas() {
        List<PendingAnalyticsDelta> pending;
        try {
            pending = pendingRepository.findPending(BATCH_SIZE);
        } catch (Exception ex) {
            log.error("Failed to load pending analytics deltas", ex);
            return;
        }
        if (pending.isEmpty()) {
            return;
        }
        log.info("Replaying {} pending analytics deltas", pending.size());
        for (var delta : pending) {
            if (delta.getAttempts() >= maxAttempts) {
                log.error("Pending analytics delta {} for branch {} reached {} attempts, skipping until inspected",
                        delta.getId(), delta.getBranchId().value(), delta.getAttempts());
                continue;
            }
            try {
                snapshotService.replay(delta);
                log.info("Replayed analytics delta {} for branch {}", delta.getId(), delta.getBranchId().value());
            } catch (Exception ex) {
                log.error("Replay failed for analytics delta {} on branch {}", delta.getId(), delta.getBranchId().value(), ex);
                recordFailure(delta, ex);
            }
        }
    }

    private void recordFailure(PendingAnalyticsDelta delta, Exception ex) {
        try {
            snapshotService.recordReplayFailure(delta, ex.getMessage());
        } catch (Exception failureEx) {
            log.error("Could not record replay failure for analytics delta {}", delta.getId(), failureEx);
        }
    }
}
