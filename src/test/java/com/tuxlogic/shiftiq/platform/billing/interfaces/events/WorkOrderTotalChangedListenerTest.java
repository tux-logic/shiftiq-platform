package com.tuxlogic.shiftiq.platform.billing.interfaces.events;

import com.tuxlogic.shiftiq.platform.billing.domain.model.aggregates.Quote;
import com.tuxlogic.shiftiq.platform.billing.domain.model.commands.CreateQuoteCommand;
import com.tuxlogic.shiftiq.platform.billing.domain.repositories.QuoteRepository;
import com.tuxlogic.shiftiq.platform.operations.domain.model.events.WorkOrderTotalChangedEvent;
import com.tuxlogic.shiftiq.platform.operations.domain.model.valueobjects.WorkOrderId;
import com.tuxlogic.shiftiq.platform.shared.domain.model.valueobjects.BranchId;
import com.tuxlogic.shiftiq.platform.shared.domain.model.valueobjects.Money;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class WorkOrderTotalChangedListenerTest {

    @Mock
    private QuoteRepository quoteRepository;

    private WorkOrderTotalChangedListener listener;

    @BeforeEach
    void setUp() {
        listener = new WorkOrderTotalChangedListener(quoteRepository);
    }

    @Test
    void onWorkOrderTotalChanged_WhenDraftQuoteExists_ShouldSyncAndSaveQuote() {
        UUID workOrderId = UUID.randomUUID();
        Quote draftQuote = new Quote(
                new CreateQuoteCommand(workOrderId, new BranchId(UUID.randomUUID()), 10.0),
                Money.of(100.0)
        );

        when(quoteRepository.findDraftByWorkOrderId(workOrderId)).thenReturn(Optional.of(draftQuote));

        listener.on(new WorkOrderTotalChangedEvent(this, new WorkOrderId(workOrderId), Money.of(250.0)));

        assertEquals(0, new BigDecimal("250.00").compareTo(draftQuote.getSubtotalAmount().amount()));
        assertEquals(0, new BigDecimal("225.00").compareTo(draftQuote.getTotalAmount().amount()));
        verify(quoteRepository).save(draftQuote);
    }

    @Test
    void onWorkOrderTotalChanged_WhenNoDraftQuoteExists_ShouldDoNothing() {
        UUID workOrderId = UUID.randomUUID();
        when(quoteRepository.findDraftByWorkOrderId(workOrderId)).thenReturn(Optional.empty());

        listener.on(new WorkOrderTotalChangedEvent(this, new WorkOrderId(workOrderId), Money.of(250.0)));

        verify(quoteRepository, never()).save(any());
    }
}
