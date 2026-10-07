package com.tuxlogic.shiftiq.platform.analytics.interfaces.events;

import com.tuxlogic.shiftiq.platform.analytics.application.internal.services.AnalyticsDeltaDispatcher;
import com.tuxlogic.shiftiq.platform.inventory.domain.model.events.LowStockAlertClearedEvent;
import com.tuxlogic.shiftiq.platform.inventory.domain.model.events.LowStockAlertTriggeredEvent;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

/**
 * Recomputes the low stock alert count from inventory on every stock alert change
 * instead of incrementing/decrementing a daily counter, so the KPI always reflects the
 * real number of products below their minimum stock (finding M14).
 */
@Component
public class InventoryStockAnalyticsListener {

    private final AnalyticsDeltaDispatcher dispatcher;

    public InventoryStockAnalyticsListener(AnalyticsDeltaDispatcher dispatcher) {
        this.dispatcher = dispatcher;
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT, fallbackExecution = true)
    public void on(LowStockAlertTriggeredEvent event) {
        if (event.branchId() == null) {
            return;
        }
        dispatcher.dispatchLowStockRecompute(event.branchId());
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT, fallbackExecution = true)
    public void on(LowStockAlertClearedEvent event) {
        if (event.branchId() == null) {
            return;
        }
        dispatcher.dispatchLowStockRecompute(event.branchId());
    }
}
