package com.tuxlogic.shiftiq.platform.billing.interfaces.rest;

import com.tuxlogic.shiftiq.platform.billing.application.commandservices.MercadoPagoPaymentCommandService;
import com.tuxlogic.shiftiq.platform.billing.application.outboundservices.MercadoPagoPreferenceResult;
import com.tuxlogic.shiftiq.platform.billing.application.queryservices.QuoteQueryService;
import com.tuxlogic.shiftiq.platform.billing.domain.model.aggregates.Quote;
import com.tuxlogic.shiftiq.platform.billing.domain.model.aggregates.Voucher;
import com.tuxlogic.shiftiq.platform.billing.domain.model.queries.GetQuoteByIdQuery;
import com.tuxlogic.shiftiq.platform.billing.domain.model.valueobjects.QuoteStatus;
import com.tuxlogic.shiftiq.platform.billing.domain.model.valueobjects.VoucherType;
import com.tuxlogic.shiftiq.platform.billing.domain.repositories.VoucherRepository;
import com.tuxlogic.shiftiq.platform.billing.interfaces.rest.resources.CreateMercadoPagoPreferenceResource;
import com.tuxlogic.shiftiq.platform.billing.interfaces.rest.resources.MercadoPagoPreferenceResource;
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

import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MercadoPagoPaymentsControllerTest {

    @Mock
    private MercadoPagoPaymentCommandService paymentCommandService;

    @Mock
    private QuoteQueryService quoteQueryService;

    @Mock
    private VoucherRepository voucherRepository;

    @Mock
    private MultiTenancySecurityService multiTenancySecurityService;

    @Mock
    private MessageSource messageSource;

    private MercadoPagoPaymentsController controller;

    @BeforeEach
    void setUp() {
        controller = new MercadoPagoPaymentsController(paymentCommandService, quoteQueryService, voucherRepository, multiTenancySecurityService, messageSource);
    }

    @Test
    void createPreference_WhenQuoteApproved_ShouldReturnCreated() {
        UUID quoteId = UUID.randomUUID();
        UUID branchId = UUID.randomUUID();
        BigDecimal amount = new BigDecimal("150.00");

        Quote quote = new Quote(quoteId, UUID.randomUUID(), new BranchId(branchId), new Money(amount), 0.0, new Money(amount), QuoteStatus.APPROVED);

        when(quoteQueryService.handle(any(GetQuoteByIdQuery.class))).thenReturn(Optional.of(quote));
        when(voucherRepository.findByQuoteId(eq(quoteId))).thenReturn(Optional.empty());

        MercadoPagoPreferenceResult mockResponse = new MercadoPagoPreferenceResult(
                "pref_123456",
                "https://www.mercadopago.com.pe/checkout/v1/redirect?pref_id=pref_123456",
                "https://sandbox.mercadopago.com.pe/checkout/v1/redirect?pref_id=pref_123456",
                amount,
                "PEN",
                quoteId.toString()
        );

        when(paymentCommandService.createPreference(eq(amount), eq("PEN"), anyString(), eq(quoteId.toString())))
                .thenReturn(Optional.of(mockResponse));

        CreateMercadoPagoPreferenceResource resource = new CreateMercadoPagoPreferenceResource(quoteId);

        ResponseEntity<?> response = controller.createPreference(resource);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody() instanceof MercadoPagoPreferenceResource);
        MercadoPagoPreferenceResource resBody = (MercadoPagoPreferenceResource) response.getBody();
        assertEquals("pref_123456", resBody.preferenceId());
        verify(multiTenancySecurityService).validateBranchAccess(branchId);
    }

    @Test
    void createPreference_WhenQuoteAlreadyInvoiced_ShouldReturnConflict() {
        UUID quoteId = UUID.randomUUID();
        UUID branchId = UUID.randomUUID();
        BigDecimal amount = new BigDecimal("150.00");

        Quote quote = new Quote(quoteId, UUID.randomUUID(), new BranchId(branchId), new Money(amount), 0.0, new Money(amount), QuoteStatus.APPROVED);
        Voucher existingVoucher = new Voucher(quoteId, VoucherType.RECEIPT, "DNI", "12345678", "Juan Perez", quote.getTotalAmount(), UUID.randomUUID(), "http://pdf");

        when(quoteQueryService.handle(any(GetQuoteByIdQuery.class))).thenReturn(Optional.of(quote));
        when(voucherRepository.findByQuoteId(eq(quoteId))).thenReturn(Optional.of(existingVoucher));
        when(messageSource.getMessage(anyString(), any(), any())).thenReturn("Quote already invoiced");

        CreateMercadoPagoPreferenceResource resource = new CreateMercadoPagoPreferenceResource(quoteId);

        ResponseEntity<?> response = controller.createPreference(resource);

        assertEquals(HttpStatus.CONFLICT, response.getStatusCode());
        verify(paymentCommandService, never()).createPreference(any(), any(), any(), any());
    }

    @Test
    void createPreference_WhenBranchAccessDenied_ShouldThrowException() {
        UUID quoteId = UUID.randomUUID();
        UUID branchId = UUID.randomUUID();

        Quote quote = new Quote(quoteId, UUID.randomUUID(), new BranchId(branchId), new Money(new BigDecimal("100.00")), 0.0, new Money(new BigDecimal("100.00")), QuoteStatus.APPROVED);

        when(quoteQueryService.handle(any(GetQuoteByIdQuery.class))).thenReturn(Optional.of(quote));
        doThrow(new AccessDeniedException("Access denied to branch"))
                .when(multiTenancySecurityService).validateBranchAccess(branchId);

        CreateMercadoPagoPreferenceResource resource = new CreateMercadoPagoPreferenceResource(quoteId);

        assertThrows(AccessDeniedException.class, () -> controller.createPreference(resource));
    }

    @Test
    void createPreference_WhenQuoteNotFound_ShouldReturnNotFound() {
        UUID quoteId = UUID.randomUUID();
        when(quoteQueryService.handle(any(GetQuoteByIdQuery.class))).thenReturn(Optional.empty());
        when(messageSource.getMessage(anyString(), any(), any())).thenReturn("Quote not found");

        CreateMercadoPagoPreferenceResource resource = new CreateMercadoPagoPreferenceResource(quoteId);

        ResponseEntity<?> response = controller.createPreference(resource);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }

    @Test
    void handleWebhook_WhenValidPaymentEvent_ShouldReturnOk() {
        ResponseEntity<Void> response = controller.handleWebhook("payment", null, null, "123456789");
        assertEquals(HttpStatus.OK, response.getStatusCode());
        verify(paymentCommandService).getPaymentStatus(123456789L);
    }
}
