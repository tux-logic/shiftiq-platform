package com.tuxlogic.shiftiq.platform.analytics.application.internal.services;

import com.tuxlogic.shiftiq.platform.analytics.application.internal.support.AnalyticsClock;
import com.tuxlogic.shiftiq.platform.analytics.domain.model.valueobjects.AnalyticsDelta;
import com.tuxlogic.shiftiq.platform.shared.domain.model.valueobjects.BranchId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * Applies analytics changes produced by domain events.
 *
 * <p>When the snapshot cannot be written (database unavailable, constraint conflict,
 * locking, ...) the change is queued as a {@code PendingAnalyticsDelta} instead of being
 * lost, and the scheduled replay job applies it later (finding M13). This class stays
 * non transactional on purpose: the failed write rolls back in its own transaction
 * before the queue entry is stored in a separate one.</p>
 */
@Component
public class AnalyticsDeltaDispatcher {

    private static final Logger log = LoggerFactory.getLogger(AnalyticsDeltaDispatcher.class);

    private final BranchAnalyticsSnapshotService snapshotService;
    private final AnalyticsClock clock;

    public AnalyticsDeltaDispatcher(BranchAnalyticsSnapshotService snapshotService, AnalyticsClock clock) {
        this.snapshotService = snapshotService;
        this.clock = clock;
    }

    public void dispatch(BranchId branchId, AnalyticsDelta delta) {
        if (branchId == null || delta == null) {
            return;
        }
        try {
            snapshotService.applyDelta(branchId, delta);
            log.info("Analytics delta applied for branch {}: {} revenue, {} work orders, {} appointments, {} dtc",
                    branchId.value(), delta.revenue(), delta.workOrders(), delta.appointments(), delta.dtcAlerts());
        } catch (Exception ex) {
            log.error("Could not apply analytics delta for branch {}, queuing it for replay", branchId.value(), ex);
            queue(branchId, delta, false);
        }
    }

    public void dispatchLowStockRecompute(BranchId branchId) {
        if (branchId == null) {
            return;
        }
        try {
            snapshotService.applyLowStockRecompute(branchId);
            log.info("Low stock analytics recomputed for branch {}", branchId.value());
        } catch (Exception ex) {
            log.error("Could not recompute low stock analytics for branch {}, queuing it for replay", branchId.value(), ex);
            queue(branchId, AnalyticsDelta.zero(), true);
        }
    }

    private void queue(BranchId branchId, AnalyticsDelta delta, boolean lowStockRecompute) {
        try {
            snapshotService.enqueueDelta(branchId, clock.today(), delta, lowStockRecompute);
            log.info("Analytics change queued for replay on branch {} (low stock recompute: {})",
                    branchId.value(), lowStockRecompute);
        } catch (Exception ex) {
            log.error("Could not queue analytics change for branch {}, the KPI change is lost", branchId.value(), ex);
        }
    }
}
