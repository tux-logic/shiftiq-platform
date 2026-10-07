package com.tuxlogic.shiftiq.platform.analytics.application.internal.services;

import com.tuxlogic.shiftiq.platform.analytics.application.internal.support.AnalyticsClock;
import com.tuxlogic.shiftiq.platform.analytics.domain.model.aggregates.PendingAnalyticsDelta;
import com.tuxlogic.shiftiq.platform.analytics.domain.model.valueobjects.AnalyticsDelta;
import com.tuxlogic.shiftiq.platform.analytics.domain.repositories.BranchAnalyticsPendingDeltaRepository;
import com.tuxlogic.shiftiq.platform.analytics.domain.repositories.BranchAnalyticsRepository;
import com.tuxlogic.shiftiq.platform.inventory.application.queryservices.ProductQueryService;
import com.tuxlogic.shiftiq.platform.inventory.domain.model.queries.GetProductsByBranchIdQuery;
import com.tuxlogic.shiftiq.platform.shared.domain.model.valueobjects.BranchId;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

/**
 * Write side of the branch analytics snapshots.
 *
 * <p>All mutations are applied as atomic SQL upserts instead of a read-modify-write of
 * the aggregate, so concurrent events on the same branch and day never lose an update
 * and the unique (branch, day) constraint cannot race (findings M5/M6).</p>
 */
@Service
public class BranchAnalyticsSnapshotService {

    private final BranchAnalyticsRepository repository;
    private final BranchAnalyticsPendingDeltaRepository pendingRepository;
    private final AnalyticsClock clock;
    private final ProductQueryService productQueryService;

    public BranchAnalyticsSnapshotService(BranchAnalyticsRepository repository,
                                          BranchAnalyticsPendingDeltaRepository pendingRepository,
                                          AnalyticsClock clock,
                                          ProductQueryService productQueryService) {
        this.repository = repository;
        this.pendingRepository = pendingRepository;
        this.clock = clock;
        this.productQueryService = productQueryService;
    }

    @Transactional
    public void applyDelta(BranchId branchId, AnalyticsDelta delta) {
        if (branchId == null) {
            throw new IllegalArgumentException("analytics.error.branchId.required");
        }
        if (delta == null) {
            return;
        }
        repository.applyDelta(branchId, clock.today(), delta);
    }

    /**
     * Recomputes the low stock alert count from inventory and stores it as an absolute
     * value, so the KPI can never drift from increments/decrements (finding M14).
     */
    @Transactional
    public void applyLowStockRecompute(BranchId branchId) {
        if (branchId == null) {
            throw new IllegalArgumentException("analytics.error.branchId.required");
        }
        var lowStockProducts = productQueryService.handle(new GetProductsByBranchIdQuery(branchId, null, null, true));
        repository.setLowStockAlertsCount(branchId, clock.today(), lowStockProducts.size());
    }

    @Transactional
    public void enqueueDelta(BranchId branchId, LocalDate deltaDate, AnalyticsDelta delta, boolean lowStockRecompute) {
        pendingRepository.save(new PendingAnalyticsDelta(branchId, deltaDate, delta, lowStockRecompute));
    }

    /**
     * Applies a queued delta and removes it in the same transaction, so a row is either
     * fully replayed or retried again on the next run.
     */
    @Transactional
    public void replay(PendingAnalyticsDelta pending) {
        if (pending == null) {
            return;
        }
        repository.applyDelta(pending.getBranchId(), pending.getDeltaDate(), pending.getDelta());
        if (pending.isLowStockRecompute()) {
            var lowStockProducts = productQueryService.handle(
                    new GetProductsByBranchIdQuery(pending.getBranchId(), null, null, true));
            repository.setLowStockAlertsCount(pending.getBranchId(), pending.getDeltaDate(), lowStockProducts.size());
        }
        pendingRepository.delete(pending);
    }

    @Transactional
    public void recordReplayFailure(PendingAnalyticsDelta pending, String error) {
        pending.recordFailure(error);
        pendingRepository.save(pending);
    }
}
