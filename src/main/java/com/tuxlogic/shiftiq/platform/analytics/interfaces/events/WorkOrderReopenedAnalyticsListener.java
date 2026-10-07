package com.tuxlogic.shiftiq.platform.analytics.interfaces.events;

import com.tuxlogic.shiftiq.platform.analytics.application.internal.services.AnalyticsDeltaDispatcher;
import com.tuxlogic.shiftiq.platform.analytics.domain.model.valueobjects.AnalyticsDelta;
import com.tuxlogic.shiftiq.platform.operations.domain.model.events.WorkOrderReopenedEvent;
import java.math.BigDecimal;

import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

/**
 * Reverts the analytics effect of a work order completion when the order is reopened,
 * so reopening and completing again never double counts (finding M7). The snapshot
 * columns are clamped at zero by the upsert, so the counter cannot go negative.
 */
@Component
public class WorkOrderReopenedAnalyticsListener {

    private final AnalyticsDeltaDispatcher dispatcher;

    public WorkOrderReopenedAnalyticsListener(AnalyticsDeltaDispatcher dispatcher) {
        this.dispatcher = dispatcher;
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT, fallbackExecution = true)
    public void on(WorkOrderReopenedEvent event) {
        if (event.branchId() == null) {
            return;
        }
        var revenue = event.totalAmount() != null && event.totalAmount().amount() != null
                ? event.totalAmount().amount()
                : BigDecimal.ZERO;
        dispatcher.dispatch(event.branchId(), new AnalyticsDelta(revenue.negate(), -1, 0, 0));
    }
}
