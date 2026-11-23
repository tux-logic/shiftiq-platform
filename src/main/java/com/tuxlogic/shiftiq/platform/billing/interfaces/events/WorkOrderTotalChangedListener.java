package com.tuxlogic.shiftiq.platform.billing.interfaces.events;

import com.tuxlogic.shiftiq.platform.billing.domain.repositories.QuoteRepository;
import com.tuxlogic.shiftiq.platform.operations.domain.model.events.WorkOrderTotalChangedEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * Listens for {@link WorkOrderTotalChangedEvent} and automatically recalculates
 * any active DRAFT quote associated with the modified Work Order.
 * APPROVED and CANCELED quotes remain untouched.
 */
@Component
public class WorkOrderTotalChangedListener {

    private static final Logger LOGGER = LoggerFactory.getLogger(WorkOrderTotalChangedListener.class);
    private final QuoteRepository quoteRepository;

    public WorkOrderTotalChangedListener(QuoteRepository quoteRepository) {
        this.quoteRepository = quoteRepository;
    }

    @EventListener
    @Transactional
    public void on(WorkOrderTotalChangedEvent event) {
        if (event == null || event.workOrderId() == null || event.totalAmount() == null) {
            return;
        }
        quoteRepository.findDraftByWorkOrderId(event.workOrderId().value())
                .ifPresent(draftQuote -> {
                    draftQuote.syncSubtotal(event.totalAmount());
                    quoteRepository.save(draftQuote);
                    LOGGER.debug("Auto-synced DRAFT quote '{}' to new Work Order total '{}'",
                            draftQuote.getId(), event.totalAmount().amount());
                });
    }
}
