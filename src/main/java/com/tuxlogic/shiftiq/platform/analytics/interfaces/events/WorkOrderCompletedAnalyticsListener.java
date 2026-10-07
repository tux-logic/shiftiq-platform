package com.tuxlogic.shiftiq.platform.analytics.interfaces.events;

import com.tuxlogic.shiftiq.platform.analytics.application.internal.services.AnalyticsDeltaDispatcher;
import com.tuxlogic.shiftiq.platform.analytics.domain.model.valueobjects.AnalyticsDelta;
import com.tuxlogic.shiftiq.platform.operations.domain.model.events.WorkOrderCompletedEvent;
import java.math.BigDecimal;

import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
public class WorkOrderCompletedAnalyticsListener {

    private final AnalyticsDeltaDispatcher dispatcher;

    public WorkOrderCompletedAnalyticsListener(AnalyticsDeltaDispatcher dispatcher) {
        this.dispatcher = dispatcher;
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT, fallbackExecution = true)
    public void on(WorkOrderCompletedEvent event) {
        if (event.branchId() == null) {
            return;
        }
        var revenue = event.totalAmount() != null && event.totalAmount().amount() != null
                ? event.totalAmount().amount()
                : BigDecimal.ZERO;
        dispatcher.dispatch(event.branchId(), new AnalyticsDelta(revenue, 1, 0, 0));
    }
}
