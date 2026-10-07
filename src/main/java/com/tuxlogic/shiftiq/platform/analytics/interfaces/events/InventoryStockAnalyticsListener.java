package com.tuxlogic.shiftiq.platform.analytics.interfaces.events;

import com.tuxlogic.shiftiq.platform.analytics.application.internal.services.BranchAnalyticsSnapshotService;
import com.tuxlogic.shiftiq.platform.analytics.domain.repositories.BranchAnalyticsRepository;
import com.tuxlogic.shiftiq.platform.inventory.domain.model.events.LowStockAlertClearedEvent;
import com.tuxlogic.shiftiq.platform.inventory.domain.model.events.LowStockAlertTriggeredEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
public class InventoryStockAnalyticsListener {

    private static final Logger log = LoggerFactory.getLogger(InventoryStockAnalyticsListener.class);
    private final BranchAnalyticsSnapshotService snapshotService;
    private final BranchAnalyticsRepository repository;

    public InventoryStockAnalyticsListener(BranchAnalyticsSnapshotService snapshotService, BranchAnalyticsRepository repository) {
        this.snapshotService = snapshotService;
        this.repository = repository;
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT, fallbackExecution = true)
    public void on(LowStockAlertTriggeredEvent event) {
        try {
            if (event.branchId() == null) return;
            var snapshot = snapshotService.getOrCreateTodaySnapshot(event.branchId());
            snapshot.updateLowStockAlertsCount(snapshot.getLowStockAlertsCount() + 1);
            repository.save(snapshot);
            log.info("Analytics updated for LowStockAlertTriggeredEvent on branch {}", event.branchId().value());
        } catch (Exception e) {
            log.error("Failed to process LowStockAlertTriggeredEvent in analytics", e);
        }
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT, fallbackExecution = true)
    public void on(LowStockAlertClearedEvent event) {
        try {
            if (event.branchId() == null) return;
            var snapshot = snapshotService.getOrCreateTodaySnapshot(event.branchId());
            snapshot.updateLowStockAlertsCount(Math.max(0, snapshot.getLowStockAlertsCount() - 1));
            repository.save(snapshot);
            log.info("Analytics updated for LowStockAlertClearedEvent on branch {}", event.branchId().value());
        } catch (Exception e) {
            log.error("Failed to process LowStockAlertClearedEvent in analytics", e);
        }
    }
}
