package com.tuxlogic.shiftiq.platform.billing.interfaces.rest;

import com.tuxlogic.shiftiq.platform.billing.application.commandservices.QuoteCommandService;
import com.tuxlogic.shiftiq.platform.billing.application.queryservices.QuoteQueryService;
import com.tuxlogic.shiftiq.platform.billing.domain.model.aggregates.Quote;
import com.tuxlogic.shiftiq.platform.billing.domain.model.commands.CreateQuoteCommand;
import com.tuxlogic.shiftiq.platform.billing.interfaces.rest.resources.CreateQuoteResource;
import com.tuxlogic.shiftiq.platform.iam.domain.model.valueobjects.Permission;
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

    @Mock
    private com.tuxlogic.shiftiq.platform.billing.application.outboundservices.ExternalBillingContextService externalBillingContextService;

    private QuotesController controller;

    @BeforeEach
    void setUp() {
        controller = new QuotesController(commandService, queryService, messageSource, multiTenancySecurityService, externalBillingContextService);
    }

    @Test
    void createQuote_WhenBranchAccessDenied_ShouldThrowException() {
        UUID workOrderId = UUID.randomUUID();
        UUID branchId = UUID.randomUUID();
        CreateQuoteResource resource = new CreateQuoteResource(workOrderId, branchId, 10.0);

        doThrow(new AccessDeniedException("Unauthorized branch access"))
                .when(multiTenancySecurityService).validateBranchPermission(branchId, Permission.QUOTE_CREATE);

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
        verify(multiTenancySecurityService).validateBranchPermission(branchId, Permission.QUOTE_CREATE);
    }

    @Test
    void getMyQuotes_WhenAuthenticatedCustomer_ShouldReturnCustomerQuotes() {
        UUID workOrderId = UUID.randomUUID();
        UUID branchId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();

        Quote quote = new Quote(new CreateQuoteCommand(workOrderId, new BranchId(branchId), 0.0), Money.of(150.0));
        var principal = mock(com.tuxlogic.shiftiq.platform.shared.infrastructure.security.AuthenticatedPrincipal.class);
        when(principal.getId()).thenReturn(userId);
        var auth = new org.springframework.security.authentication.UsernamePasswordAuthenticationToken(principal, null);

        when(externalBillingContextService.findWorkOrderIdsByUserId(userId)).thenReturn(java.util.List.of(workOrderId));
        when(queryService.findAllByWorkOrderIds(java.util.List.of(workOrderId))).thenReturn(java.util.List.of(quote));

        var response = controller.getMyQuotes(auth);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(1, response.getBody().size());
    }

    @Test
    void getQuoteById_WhenOwningCustomer_ShouldAllowReadOnlyAccess() {
        UUID workOrderId = UUID.randomUUID();
        UUID branchId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();

        Quote quote = new Quote(new CreateQuoteCommand(workOrderId, new BranchId(branchId), 0.0), Money.of(150.0));
        var principal = mock(com.tuxlogic.shiftiq.platform.shared.infrastructure.security.AuthenticatedPrincipal.class);
        when(principal.getId()).thenReturn(userId);
        var auth = new org.springframework.security.authentication.UsernamePasswordAuthenticationToken(principal, null);

        when(queryService.handle(any(com.tuxlogic.shiftiq.platform.billing.domain.model.queries.GetQuoteByIdQuery.class)))
                .thenReturn(java.util.Optional.of(quote));
        when(externalBillingContextService.isCustomerOwnerOfWorkOrder(userId, workOrderId)).thenReturn(true);

        var response = controller.getQuoteById(quote.getId(), auth);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        verify(multiTenancySecurityService, never()).validateBranchPermission(any(), any());
    }

    @Test
    void approveQuote_WhenOwningCustomer_ShouldApproveWithoutStaffPermission() {
        UUID workOrderId = UUID.randomUUID();
        UUID branchId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();

        Quote quote = new Quote(new CreateQuoteCommand(workOrderId, new BranchId(branchId), 0.0), Money.of(150.0));
        var principal = mock(com.tuxlogic.shiftiq.platform.shared.infrastructure.security.AuthenticatedPrincipal.class);
        when(principal.getId()).thenReturn(userId);
        var auth = new org.springframework.security.authentication.UsernamePasswordAuthenticationToken(principal, null);

        when(queryService.handle(any(com.tuxlogic.shiftiq.platform.billing.domain.model.queries.GetQuoteByIdQuery.class)))
                .thenReturn(java.util.Optional.of(quote));
        when(externalBillingContextService.isCustomerOwnerOfWorkOrder(userId, workOrderId)).thenReturn(true);
        when(commandService.handle(any(com.tuxlogic.shiftiq.platform.billing.domain.model.commands.ApproveQuoteCommand.class)))
                .thenReturn(Result.success(quote));

        org.springframework.security.core.context.SecurityContextHolder.getContext().setAuthentication(auth);
        try {
            var response = controller.approveQuote(quote.getId());

            assertEquals(HttpStatus.OK, response.getStatusCode());
            verify(multiTenancySecurityService, never()).validateBranchPermission(any(), any());
        } finally {
            org.springframework.security.core.context.SecurityContextHolder.clearContext();
        }
    }

    @Test
    void cancelQuote_WhenOwningCustomer_ShouldCancelWithoutStaffPermission() {
        UUID workOrderId = UUID.randomUUID();
        UUID branchId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();

        Quote quote = new Quote(new CreateQuoteCommand(workOrderId, new BranchId(branchId), 0.0), Money.of(150.0));
        var principal = mock(com.tuxlogic.shiftiq.platform.shared.infrastructure.security.AuthenticatedPrincipal.class);
        when(principal.getId()).thenReturn(userId);
        var auth = new org.springframework.security.authentication.UsernamePasswordAuthenticationToken(principal, null);

        when(queryService.handle(any(com.tuxlogic.shiftiq.platform.billing.domain.model.queries.GetQuoteByIdQuery.class)))
                .thenReturn(java.util.Optional.of(quote));
        when(externalBillingContextService.isCustomerOwnerOfWorkOrder(userId, workOrderId)).thenReturn(true);
        when(commandService.handle(any(com.tuxlogic.shiftiq.platform.billing.domain.model.commands.CancelQuoteCommand.class)))
                .thenReturn(Result.success(quote));

        org.springframework.security.core.context.SecurityContextHolder.getContext().setAuthentication(auth);
        try {
            var response = controller.cancelQuote(quote.getId());

            assertEquals(HttpStatus.OK, response.getStatusCode());
            verify(multiTenancySecurityService, never()).validateBranchPermission(any(), any());
        } finally {
            org.springframework.security.core.context.SecurityContextHolder.clearContext();
        }
    }
}

