package com.tuxlogic.shiftiq.platform.analytics.interfaces.events;

import com.tuxlogic.shiftiq.platform.analytics.application.internal.services.AnalyticsDeltaDispatcher;
import com.tuxlogic.shiftiq.platform.analytics.domain.model.valueobjects.AnalyticsDelta;
import com.tuxlogic.shiftiq.platform.fleet.domain.model.events.AppointmentCreatedEvent;
import java.math.BigDecimal;

import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
public class AppointmentCreatedAnalyticsListener {

    private final AnalyticsDeltaDispatcher dispatcher;

    public AppointmentCreatedAnalyticsListener(AnalyticsDeltaDispatcher dispatcher) {
        this.dispatcher = dispatcher;
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT, fallbackExecution = true)
    public void on(AppointmentCreatedEvent event) {
        if (event.branchId() == null) {
            return;
        }
        dispatcher.dispatch(event.branchId(), new AnalyticsDelta(BigDecimal.ZERO, 0, 1, 0));
    }
}
