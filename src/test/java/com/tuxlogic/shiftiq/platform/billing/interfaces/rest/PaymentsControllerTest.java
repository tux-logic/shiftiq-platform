package com.tuxlogic.shiftiq.platform.billing.interfaces.rest;

import com.tuxlogic.shiftiq.platform.billing.application.commandservices.MercadoPagoPaymentCommandService;
import com.tuxlogic.shiftiq.platform.billing.application.internal.commandservices.PaymentSessionServiceImpl;
import com.tuxlogic.shiftiq.platform.billing.application.outboundservices.ExternalBillingContextService;
import com.tuxlogic.shiftiq.platform.billing.application.outboundservices.MercadoPagoGateway;
import com.tuxlogic.shiftiq.platform.billing.application.outboundservices.MercadoPagoPreferenceResult;
import com.tuxlogic.shiftiq.platform.billing.application.outboundservices.PaymentGatewayRegistry;
import com.tuxlogic.shiftiq.platform.billing.application.queryservices.QuoteQueryService;
import com.tuxlogic.shiftiq.platform.billing.application.queryservices.VoucherQueryService;
import com.tuxlogic.shiftiq.platform.billing.domain.model.aggregates.Quote;
import com.tuxlogic.shiftiq.platform.billing.domain.model.commands.CreateQuoteCommand;
import com.tuxlogic.shiftiq.platform.billing.domain.model.queries.GetQuoteByIdQuery;
import com.tuxlogic.shiftiq.platform.billing.domain.model.queries.GetVoucherByQuoteIdQuery;
import com.tuxlogic.shiftiq.platform.billing.domain.model.valueobjects.PaymentProvider;
import com.tuxlogic.shiftiq.platform.billing.infrastructure.outbound.mercadopago.MercadoPagoGatewayStrategy;
import com.tuxlogic.shiftiq.platform.billing.interfaces.rest.resources.CreatePaymentSessionResource;
import com.tuxlogic.shiftiq.platform.billing.interfaces.rest.resources.PaymentSessionResource;
import com.tuxlogic.shiftiq.platform.shared.domain.model.valueobjects.BranchId;
import com.tuxlogic.shiftiq.platform.shared.domain.model.valueobjects.Money;
import com.tuxlogic.shiftiq.platform.shared.infrastructure.security.AuthenticatedPrincipal;
import com.tuxlogic.shiftiq.platform.shared.infrastructure.security.MultiTenancySecurityService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.MessageSource;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PaymentsControllerTest {

    @Mock
    private QuoteQueryService quoteQueryService;

    @Mock
    private VoucherQueryService voucherQueryService;

    @Mock
    private MercadoPagoPaymentCommandService paymentCommandService;

    @Mock
    private MultiTenancySecurityService multiTenancySecurityService;

    @Mock
    private ExternalBillingContextService externalBillingContextService;

    @Mock
    private MercadoPagoGateway mercadoPagoGateway;

    @Mock
    private MessageSource messageSource;

    private PaymentsController controller;

    @BeforeEach
    void setUp() {
        var registry = new PaymentGatewayRegistry(List.of(new MercadoPagoGatewayStrategy(mercadoPagoGateway)));
        var sessionService = new PaymentSessionServiceImpl(
                quoteQueryService,
                voucherQueryService,
                paymentCommandService,
                multiTenancySecurityService,
                externalBillingContextService,
                registry,
                messageSource
        );
        controller = new PaymentsController(sessionService);
    }

    @Test
    void createPaymentSession_WhenCustomerOwnsCompletedOrder_ShouldReturnCreatedSession() {
        UUID workOrderId = UUID.randomUUID();
        UUID branchId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();

        Quote quote = new Quote(new CreateQuoteCommand(workOrderId, new BranchId(branchId), 0.0), new Money(new BigDecimal("150.00")));
        quote.approve();

        var principal = mock(AuthenticatedPrincipal.class);
        when(principal.getId()).thenReturn(userId);
        var auth = new UsernamePasswordAuthenticationToken(principal, null);

        when(quoteQueryService.handle(any(GetQuoteByIdQuery.class))).thenReturn(Optional.of(quote));
        when(multiTenancySecurityService.isAuthorizedForBranch(branchId)).thenReturn(false);
        when(externalBillingContextService.isCustomerOwnerOfWorkOrder(userId, workOrderId)).thenReturn(true);
        when(externalBillingContextService.isWorkOrderCompleted(workOrderId)).thenReturn(true);
        when(voucherQueryService.handle(any(GetVoucherByQuoteIdQuery.class))).thenReturn(Optional.empty());
        when(mercadoPagoGateway.createPreference(any(), eq("PEN"), anyString(), anyString()))
                .thenReturn(Optional.of(new MercadoPagoPreferenceResult(
                        "pref-999", "https://mp/init", "https://mp/sandbox",
                        new BigDecimal("150.00"), "PEN", quote.getId().toString())));

        var resource = new CreatePaymentSessionResource(quote.getId(), PaymentProvider.MERCADO_PAGO, "RECEIPT", "DNI", "75981176", "ALAN MAMANI");
        var response = controller.createPaymentSession(resource, auth);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        var body = assertInstanceOf(PaymentSessionResource.class, response.getBody());
        assertEquals(PaymentProvider.MERCADO_PAGO, body.provider());
        assertEquals("pref-999", body.sessionId());
        assertEquals("https://mp/init", body.checkoutUrl());
    }

    @Test
    void createPaymentSession_WhenUnauthorizedCustomer_ShouldThrowAccessDenied() {
        UUID workOrderId = UUID.randomUUID();
        UUID branchId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();

        Quote quote = new Quote(new CreateQuoteCommand(workOrderId, new BranchId(branchId), 0.0), new Money(new BigDecimal("150.00")));

        var principal = mock(AuthenticatedPrincipal.class);
        when(principal.getId()).thenReturn(userId);
        var auth = new UsernamePasswordAuthenticationToken(principal, null);

        when(quoteQueryService.handle(any(GetQuoteByIdQuery.class))).thenReturn(Optional.of(quote));
        when(multiTenancySecurityService.isAuthorizedForBranch(branchId)).thenReturn(false);
        when(externalBillingContextService.isCustomerOwnerOfWorkOrder(userId, workOrderId)).thenReturn(false);

        var resource = new CreatePaymentSessionResource(quote.getId(), null, "RECEIPT", "DNI", "75981176", "ALAN MAMANI");
        assertThrows(AccessDeniedException.class, () -> controller.createPaymentSession(resource, auth));
    }
}
