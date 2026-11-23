package com.tuxlogic.shiftiq.platform.billing.application.internal.commandservices;

import com.tuxlogic.shiftiq.platform.billing.domain.model.aggregates.Quote;
import com.tuxlogic.shiftiq.platform.billing.domain.model.commands.CreateQuoteCommand;
import com.tuxlogic.shiftiq.platform.billing.domain.model.commands.SyncQuoteWithWorkOrderCommand;
import com.tuxlogic.shiftiq.platform.billing.domain.model.valueobjects.QuoteCommandFailure;
import com.tuxlogic.shiftiq.platform.billing.domain.repositories.QuoteRepository;
import com.tuxlogic.shiftiq.platform.operations.application.queryservices.WorkOrderQueryService;
import com.tuxlogic.shiftiq.platform.operations.domain.model.aggregates.WorkOrder;
import com.tuxlogic.shiftiq.platform.operations.domain.model.queries.GetWorkOrderByIdQuery;
import com.tuxlogic.shiftiq.platform.operations.domain.model.valueobjects.AppointmentId;
import com.tuxlogic.shiftiq.platform.operations.domain.model.valueobjects.DiagnosticSummary;
import com.tuxlogic.shiftiq.platform.shared.domain.model.valueobjects.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class QuoteCommandServiceImplTest {

    @Mock
    private QuoteRepository quoteRepository;

    @Mock
    private WorkOrderQueryService workOrderQueryService;

    private QuoteCommandServiceImpl service;

    @BeforeEach
    void setUp() {
        service = new QuoteCommandServiceImpl(quoteRepository, workOrderQueryService);
    }

    @Test
    void createQuote_WhenPreviousQuoteCanceled_ShouldAllowCreatingNewQuote() {
        UUID workOrderId = UUID.randomUUID();
        UUID branchId = UUID.randomUUID();

        WorkOrder wo = new WorkOrder(
                new AppointmentId(UUID.randomUUID()),
                new BranchId(branchId),
                new VehicleId(UUID.randomUUID()),
                new CustomerId(UUID.randomUUID()),
                1,
                new DiagnosticSummary("Diagnostico"),
                new Mileage(35000)
        );

        when(workOrderQueryService.handle(any(GetWorkOrderByIdQuery.class))).thenReturn(Optional.of(wo));
        when(quoteRepository.existsActiveByWorkOrderId(workOrderId)).thenReturn(false);
        when(quoteRepository.save(any(Quote.class))).thenAnswer(inv -> inv.getArgument(0));

        var result = service.handle(new CreateQuoteCommand(workOrderId, new BranchId(branchId), 10.0));

        assertTrue(result.isSuccess());
        verify(quoteRepository).existsActiveByWorkOrderId(workOrderId);
    }

    @Test
    void syncQuoteWithWorkOrder_WhenDraft_ShouldRecalculateSubtotalAndTotal() {
        UUID workOrderId = UUID.randomUUID();
        UUID branchId = UUID.randomUUID();

        Quote quote = new Quote(new CreateQuoteCommand(workOrderId, new BranchId(branchId), 10.0), Money.of(100.0));

        WorkOrder wo = mock(WorkOrder.class);
        when(wo.getTotalAmount()).thenReturn(new Money(new BigDecimal("200.00")));

        when(quoteRepository.findById(quote.getId())).thenReturn(Optional.of(quote));
        when(workOrderQueryService.handle(any(GetWorkOrderByIdQuery.class))).thenReturn(Optional.of(wo));
        when(quoteRepository.save(any(Quote.class))).thenAnswer(inv -> inv.getArgument(0));

        var result = service.handle(new SyncQuoteWithWorkOrderCommand(quote.getId()));

        assertTrue(result.isSuccess());
        assertEquals(0, new BigDecimal("200.00").compareTo(result.success().get().getSubtotalAmount().amount()));
        assertEquals(0, new BigDecimal("180.00").compareTo(result.success().get().getTotalAmount().amount()));
    }

    @Test
    void syncQuoteWithWorkOrder_WhenAlreadyApproved_ShouldReturnInvalidQuoteState() {
        UUID workOrderId = UUID.randomUUID();
        UUID branchId = UUID.randomUUID();

        Quote quote = new Quote(new CreateQuoteCommand(workOrderId, new BranchId(branchId), 0.0), Money.of(100.0));
        quote.approve();

        WorkOrder wo = mock(WorkOrder.class);
        when(wo.getTotalAmount()).thenReturn(new Money(new BigDecimal("200.00")));

        when(quoteRepository.findById(quote.getId())).thenReturn(Optional.of(quote));
        when(workOrderQueryService.handle(any(GetWorkOrderByIdQuery.class))).thenReturn(Optional.of(wo));

        var result = service.handle(new SyncQuoteWithWorkOrderCommand(quote.getId()));

        assertTrue(result.isFailure());
        assertEquals(QuoteCommandFailure.INVALID_QUOTE_STATE, result.failure().get());
    }
}
