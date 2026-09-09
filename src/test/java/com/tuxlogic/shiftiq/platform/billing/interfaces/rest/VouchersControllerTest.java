package com.tuxlogic.shiftiq.platform.billing.interfaces.rest;

import com.tuxlogic.shiftiq.platform.billing.application.commandservices.VoucherCommandService;
import com.tuxlogic.shiftiq.platform.billing.application.queryservices.QuoteQueryService;
import com.tuxlogic.shiftiq.platform.billing.application.queryservices.VoucherQueryService;
import com.tuxlogic.shiftiq.platform.billing.domain.model.aggregates.Quote;
import com.tuxlogic.shiftiq.platform.billing.domain.model.aggregates.Voucher;
import com.tuxlogic.shiftiq.platform.billing.domain.model.commands.CreateQuoteCommand;
import com.tuxlogic.shiftiq.platform.billing.domain.model.queries.GetQuoteByIdQuery;
import com.tuxlogic.shiftiq.platform.billing.domain.model.queries.GetVoucherByIdQuery;
import com.tuxlogic.shiftiq.platform.billing.interfaces.rest.resources.GenerateVoucherResource;
import com.tuxlogic.shiftiq.platform.shared.application.result.Result;
import com.tuxlogic.shiftiq.platform.shared.domain.model.valueobjects.BranchId;
import com.tuxlogic.shiftiq.platform.shared.domain.model.valueobjects.Money;
import com.tuxlogic.shiftiq.platform.shared.infrastructure.security.MultiTenancySecurityService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.MessageSource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class VouchersControllerTest {

    @Mock
    private VoucherCommandService commandService;

    @Mock
    private VoucherQueryService queryService;

    @Mock
    private QuoteQueryService quoteQueryService;

    @Mock
    private MessageSource messageSource;

    @Mock
    private MultiTenancySecurityService multiTenancySecurityService;

    private VouchersController controller;

    @BeforeEach
    void setUp() {
        controller = new VouchersController(commandService, queryService, quoteQueryService, messageSource, multiTenancySecurityService);
    }

    @Test
    void generateVoucher_WhenQuoteBranchForbidden_ShouldThrowException() {
        UUID quoteId = UUID.randomUUID();
        UUID branchId = UUID.randomUUID();
        GenerateVoucherResource resource = new GenerateVoucherResource(quoteId, "BOLETA", "DNI", "12345678", "Juan Perez");

        Quote quote = new Quote(
                new CreateQuoteCommand(UUID.randomUUID(), new BranchId(branchId), 0.0),
                Money.of(100.0)
        );

        when(quoteQueryService.handle(any(GetQuoteByIdQuery.class))).thenReturn(Optional.of(quote));
        doThrow(new AccessDeniedException("Unauthorized branch access"))
                .when(multiTenancySecurityService).validateBranchAccess(branchId);

        assertThrows(AccessDeniedException.class, () -> controller.generateVoucher(resource));
    }

    @Test
    void removePayment_WhenSucceeds_ShouldReturnNoContent() {
        UUID voucherId = UUID.randomUUID();
        UUID paymentId = UUID.randomUUID();
        UUID quoteId = UUID.randomUUID();
        UUID branchId = UUID.randomUUID();

        Voucher voucher = mock(Voucher.class);
        when(voucher.getQuoteId()).thenReturn(quoteId);

        Quote quote = new Quote(
                new CreateQuoteCommand(UUID.randomUUID(), new BranchId(branchId), 0.0),
                Money.of(100.0)
        );

        when(queryService.handle(any(GetVoucherByIdQuery.class))).thenReturn(Optional.of(voucher));
        when(quoteQueryService.handle(any(GetQuoteByIdQuery.class))).thenReturn(Optional.of(quote));
        when(commandService.handle(any(com.tuxlogic.shiftiq.platform.billing.domain.model.commands.RemovePaymentCommand.class))).thenReturn(Result.success(voucher));

        ResponseEntity<?> response = controller.removePayment(voucherId, paymentId);

        assertNotNull(response);
        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
    }
}

