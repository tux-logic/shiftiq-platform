package com.tuxlogic.shiftiq.platform.billing.application.internal.commandservices;

import com.tuxlogic.shiftiq.platform.billing.domain.model.aggregates.Quote;
import com.tuxlogic.shiftiq.platform.billing.domain.model.commands.CreateQuoteCommand;
import com.tuxlogic.shiftiq.platform.billing.domain.model.valueobjects.QuoteCommandFailure;
import com.tuxlogic.shiftiq.platform.billing.domain.repositories.QuoteRepository;
import com.tuxlogic.shiftiq.platform.operations.application.queryservices.WorkOrderQueryService;
import com.tuxlogic.shiftiq.platform.operations.domain.model.aggregates.WorkOrder;
import com.tuxlogic.shiftiq.platform.operations.domain.model.queries.GetWorkOrderByIdQuery;
import com.tuxlogic.shiftiq.platform.shared.application.result.Result;
import com.tuxlogic.shiftiq.platform.shared.domain.model.valueobjects.BranchId;
import com.tuxlogic.shiftiq.platform.shared.domain.model.valueobjects.Money;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
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

    @InjectMocks
    private QuoteCommandServiceImpl quoteCommandService;

    private UUID workOrderId;
    private BranchId branchId;
    private CreateQuoteCommand createQuoteCommand;
    private WorkOrder workOrder;

    @BeforeEach
    void setUp() {
        workOrderId = UUID.randomUUID();
        branchId = new BranchId(UUID.randomUUID());
        createQuoteCommand = new CreateQuoteCommand(workOrderId, branchId, 10.0);

        workOrder = mock(WorkOrder.class);
        lenient().when(workOrder.getTotalAmount()).thenReturn(new Money(new BigDecimal("100.00")));
    }

    @Test
    void createQuote_WhenQuoteDoesNotExist_ShouldCreateSuccessfully() {
        // Arrange
        when(workOrderQueryService.handle(any(GetWorkOrderByIdQuery.class))).thenReturn(Optional.of(workOrder));
        when(quoteRepository.existsByWorkOrderId(workOrderId)).thenReturn(false);
        when(quoteRepository.save(any(Quote.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        Result<Quote, QuoteCommandFailure> result = quoteCommandService.handle(createQuoteCommand);

        // Assert
        assertTrue(result.isSuccess());
        assertNotNull(result.success().get());
        assertEquals(workOrderId, result.success().get().getWorkOrderId());
        assertEquals(branchId, result.success().get().getBranchId());
        assertEquals(10.0, result.success().get().getDiscountPercentage());
        // Subtotal = 100.00, Discount = 10% -> Total = 90.00
        assertEquals(0, new BigDecimal("90.00").compareTo(result.success().get().getTotalAmount().amount()));
        verify(quoteRepository, times(1)).save(any(Quote.class));
    }

    @Test
    void createQuote_WhenQuoteAlreadyExists_ShouldReturnFailure() {
        // Arrange
        when(workOrderQueryService.handle(any(GetWorkOrderByIdQuery.class))).thenReturn(Optional.of(workOrder));
        when(quoteRepository.existsByWorkOrderId(workOrderId)).thenReturn(true);

        // Act
        Result<Quote, QuoteCommandFailure> result = quoteCommandService.handle(createQuoteCommand);

        // Assert
        assertTrue(result.isFailure());
        assertEquals(QuoteCommandFailure.QUOTE_ALREADY_EXISTS_FOR_WORK_ORDER, result.failure().get());
        verify(quoteRepository, never()).save(any(Quote.class));
    }
}
