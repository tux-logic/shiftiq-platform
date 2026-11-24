package com.tuxlogic.shiftiq.platform.analytics.interfaces.events;

import com.tuxlogic.shiftiq.platform.analytics.application.internal.services.AnalyticsDeltaDispatcher;
import com.tuxlogic.shiftiq.platform.analytics.domain.model.valueobjects.AnalyticsDelta;
import com.tuxlogic.shiftiq.platform.billing.domain.model.events.VoucherPaidEvent;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import java.math.BigDecimal;

/**
 * Records branch revenue in analytics when a voucher is actually paid (via Mercado Pago or in-person checkout),
 * decoupled from optional external electronic invoicing.
 */
@Component
public class VoucherPaidAnalyticsListener {

    private final AnalyticsDeltaDispatcher dispatcher;

    public VoucherPaidAnalyticsListener(AnalyticsDeltaDispatcher dispatcher) {
        this.dispatcher = dispatcher;
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT, fallbackExecution = true)
    public void on(VoucherPaidEvent event) {
        if (event.branchId() == null || event.paidAmount() == null || event.paidAmount().compareTo(BigDecimal.ZERO) <= 0) {
            return;
        }
        dispatcher.dispatch(event.branchId(), new AnalyticsDelta(event.paidAmount(), 0, 0, 0));
    }
}
