package com.tuxlogic.shiftiq.platform.billing.interfaces.rest;

import com.tuxlogic.shiftiq.platform.billing.application.commandservices.VoucherCommandService;
import com.tuxlogic.shiftiq.platform.billing.application.queryservices.QuoteQueryService;
import com.tuxlogic.shiftiq.platform.billing.domain.model.aggregates.Quote;
import com.tuxlogic.shiftiq.platform.billing.domain.model.commands.CreateQuoteCommand;
import com.tuxlogic.shiftiq.platform.billing.domain.model.queries.GetQuoteByIdQuery;
import com.tuxlogic.shiftiq.platform.billing.interfaces.rest.resources.ProcessCheckoutResource;
import com.tuxlogic.shiftiq.platform.shared.domain.model.valueobjects.BranchId;
import com.tuxlogic.shiftiq.platform.shared.domain.model.valueobjects.Money;
import com.tuxlogic.shiftiq.platform.shared.infrastructure.security.MultiTenancySecurityService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.MessageSource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CheckoutsControllerTest {

    @Mock
    private VoucherCommandService commandService;

    @Mock
    private QuoteQueryService quoteQueryService;

    @Mock
    private MessageSource messageSource;

    @Mock
    private MultiTenancySecurityService multiTenancySecurityService;

    private CheckoutsController controller;

    @BeforeEach
    void setUp() {
        controller = new CheckoutsController(commandService, quoteQueryService, messageSource, multiTenancySecurityService);
    }

    @Test
    @DisplayName("checkout with invalid document for invoice returns 400 Bad Request")
    void checkoutWithInvalidDocumentReturnsBadRequest() {
        UUID quoteId = UUID.randomUUID();
        UUID branchId = UUID.randomUUID();
        ProcessCheckoutResource resource = new ProcessCheckoutResource(
                quoteId,
                "INVOICE",
                "DNI",
                "12345678",
                "Empresa SAC",
                "CASH"
        );

        Quote quote = new Quote(
                new CreateQuoteCommand(UUID.randomUUID(), new BranchId(branchId), 0.0),
                Money.of(100.0)
        );

        when(quoteQueryService.handle(any(GetQuoteByIdQuery.class))).thenReturn(Optional.of(quote));
        when(messageSource.getMessage(eq("billing.error.invoice.ruc.documentType"), any(), any(), any()))
                .thenReturn("For Invoices, the document type must be RUC.");

        ResponseEntity<?> response = controller.checkout(resource);

        assertNotNull(response);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }
}
