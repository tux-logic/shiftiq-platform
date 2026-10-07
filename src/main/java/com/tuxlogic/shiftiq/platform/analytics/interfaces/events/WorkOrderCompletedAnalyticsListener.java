package com.tuxlogic.shiftiq.platform.analytics.interfaces.events;

import com.tuxlogic.shiftiq.platform.analytics.application.internal.services.BranchAnalyticsSnapshotService;
import com.tuxlogic.shiftiq.platform.analytics.domain.repositories.BranchAnalyticsRepository;
import com.tuxlogic.shiftiq.platform.operations.domain.model.events.WorkOrderCompletedEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
public class WorkOrderCompletedAnalyticsListener {

    private static final Logger log = LoggerFactory.getLogger(WorkOrderCompletedAnalyticsListener.class);
    private final BranchAnalyticsSnapshotService snapshotService;
    private final BranchAnalyticsRepository repository;

    public WorkOrderCompletedAnalyticsListener(BranchAnalyticsSnapshotService snapshotService, BranchAnalyticsRepository repository) {
        this.snapshotService = snapshotService;
        this.repository = repository;
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT, fallbackExecution = true)
    public void on(WorkOrderCompletedEvent event) {
        try {
            if (event.branchId() == null) return;
            var snapshot = snapshotService.getOrCreateTodaySnapshot(event.branchId());
            snapshot.incrementCompletedWorkOrders();
            if (event.totalAmount() != null && event.totalAmount().amount() != null) {
                snapshot.addRevenue(event.totalAmount().amount());
            }
            repository.save(snapshot);
            log.info("Analytics updated for WorkOrderCompletedEvent on branch {}", event.branchId().value());
        } catch (Exception e) {
            log.error("Failed to process WorkOrderCompletedEvent in analytics", e);
        }
    }
}
