package com.tuxlogic.shiftiq.platform.billing.interfaces.rest;

import com.tuxlogic.shiftiq.platform.billing.application.commandservices.QuoteCommandService;
import com.tuxlogic.shiftiq.platform.billing.application.queryservices.QuoteQueryService;
import com.tuxlogic.shiftiq.platform.billing.domain.model.aggregates.Quote;
import com.tuxlogic.shiftiq.platform.billing.domain.model.commands.CreateQuoteCommand;
import com.tuxlogic.shiftiq.platform.billing.interfaces.rest.resources.CreateQuoteResource;
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

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class QuotesControllerTest {

    @Mock
    private QuoteCommandService commandService;

    @Mock
    private QuoteQueryService queryService;

    @Mock
    private MessageSource messageSource;

    @Mock
    private MultiTenancySecurityService multiTenancySecurityService;

    private QuotesController controller;

    @BeforeEach
    void setUp() {
        controller = new QuotesController(commandService, queryService, messageSource, multiTenancySecurityService);
    }

    @Test
    void createQuote_WhenBranchAccessDenied_ShouldThrowException() {
        UUID workOrderId = UUID.randomUUID();
        UUID branchId = UUID.randomUUID();
        CreateQuoteResource resource = new CreateQuoteResource(workOrderId, branchId, 10.0);

        doThrow(new AccessDeniedException("Unauthorized branch access"))
                .when(multiTenancySecurityService).validateBranchAccess(branchId);

        assertThrows(AccessDeniedException.class, () -> controller.createQuote(resource));
    }

    @Test
    void createQuote_WhenAuthorized_ShouldReturnCreated() {
        UUID workOrderId = UUID.randomUUID();
        UUID branchId = UUID.randomUUID();
        CreateQuoteResource resource = new CreateQuoteResource(workOrderId, branchId, 10.0);

        Quote quote = new Quote(
                new CreateQuoteCommand(workOrderId, new BranchId(branchId), 10.0),
                Money.of(100.0)
        );

        when(commandService.handle(any(CreateQuoteCommand.class))).thenReturn(Result.success(quote));

        ResponseEntity<?> response = controller.createQuote(resource);

        assertNotNull(response);
        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        verify(multiTenancySecurityService).validateBranchAccess(branchId);
    }
}

