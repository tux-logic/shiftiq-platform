package com.tuxlogic.shiftiq.platform.billing.interfaces.rest;

import com.tuxlogic.shiftiq.platform.billing.application.commandservices.MercadoPagoPaymentCommandService;
import com.tuxlogic.shiftiq.platform.billing.application.commandservices.VoucherCommandService;
import com.tuxlogic.shiftiq.platform.billing.application.outboundservices.MercadoPagoPaymentResult;
import com.tuxlogic.shiftiq.platform.billing.application.outboundservices.MercadoPagoPreferenceResult;
import com.tuxlogic.shiftiq.platform.billing.application.queryservices.QuoteQueryService;
import com.tuxlogic.shiftiq.platform.billing.application.queryservices.VoucherQueryService;
import com.tuxlogic.shiftiq.platform.billing.domain.model.aggregates.Quote;
import com.tuxlogic.shiftiq.platform.billing.domain.model.aggregates.Voucher;
import com.tuxlogic.shiftiq.platform.billing.domain.model.commands.ProcessMercadoPagoCheckoutCommand;
import com.tuxlogic.shiftiq.platform.billing.domain.model.queries.GetQuoteByIdQuery;
import com.tuxlogic.shiftiq.platform.billing.domain.model.queries.GetVoucherByQuoteIdQuery;
import com.tuxlogic.shiftiq.platform.billing.domain.model.valueobjects.QuoteStatus;
import com.tuxlogic.shiftiq.platform.billing.domain.model.valueobjects.VoucherType;
import com.tuxlogic.shiftiq.platform.billing.interfaces.rest.resources.CreateMercadoPagoPreferenceResource;
import com.tuxlogic.shiftiq.platform.billing.interfaces.rest.resources.MercadoPagoPreferenceResource;
import com.tuxlogic.shiftiq.platform.billing.interfaces.rest.resources.MercadoPagoWebhookResource;
import com.tuxlogic.shiftiq.platform.shared.domain.model.valueobjects.BranchId;
import com.tuxlogic.shiftiq.platform.shared.application.result.Result;
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

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MercadoPagoPaymentsControllerTest {

    private static final String TEST_SECRET = "test_webhook_secret_key_12345";

    @Mock
    private MercadoPagoPaymentCommandService paymentCommandService;

    @Mock
    private VoucherCommandService voucherCommandService;

    @Mock
    private QuoteQueryService quoteQueryService;

    @Mock
    private VoucherQueryService voucherQueryService;

    @Mock
    private MultiTenancySecurityService multiTenancySecurityService;

    @Mock
    private MessageSource messageSource;

    private MercadoPagoPaymentsController controller;

    @BeforeEach
    void setUp() {
        controller = new MercadoPagoPaymentsController(
                paymentCommandService,
                voucherCommandService,
                quoteQueryService,
                voucherQueryService,
                multiTenancySecurityService,
                messageSource,
                TEST_SECRET
        );
    }

    private String generateValidSignature(String secret, String dataId, String requestId, String ts) throws Exception {
        String manifest = String.format("id:%s;request-id:%s;ts:%s;", dataId != null ? dataId : "", requestId != null ? requestId : "", ts);
        Mac mac = Mac.getInstance("HmacSHA256");
        mac.init(new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
        byte[] rawHmac = mac.doFinal(manifest.getBytes(StandardCharsets.UTF_8));
        StringBuilder hex = new StringBuilder();
        for (byte b : rawHmac) {
            hex.append(String.format("%02x", b));
        }
        return "ts=" + ts + ",v1=" + hex.toString();
    }

    @Test
    void createPreference_WhenQuoteApproved_ShouldReturnCreated() {
        UUID quoteId = UUID.randomUUID();
        UUID branchId = UUID.randomUUID();
        BigDecimal amount = new BigDecimal("150.00");

        Quote quote = new Quote(quoteId, UUID.randomUUID(), new BranchId(branchId), new Money(amount), 0.0, new Money(amount), QuoteStatus.APPROVED);

        when(quoteQueryService.handle(any(GetQuoteByIdQuery.class))).thenReturn(Optional.of(quote));
        when(voucherQueryService.handle(any(GetVoucherByQuoteIdQuery.class))).thenReturn(Optional.empty());

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
        when(voucherQueryService.handle(any(GetVoucherByQuoteIdQuery.class))).thenReturn(Optional.of(existingVoucher));
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
    void createPreference_WhenExternalGatewayFails_ShouldReturnUnexpectedError() {
        UUID quoteId = UUID.randomUUID();
        UUID branchId = UUID.randomUUID();
        BigDecimal amount = new BigDecimal("150.00");

        Quote quote = new Quote(quoteId, UUID.randomUUID(), new BranchId(branchId), new Money(amount), 0.0, new Money(amount), QuoteStatus.APPROVED);

        when(quoteQueryService.handle(any(GetQuoteByIdQuery.class))).thenReturn(Optional.of(quote));
        when(voucherQueryService.handle(any(GetVoucherByQuoteIdQuery.class))).thenReturn(Optional.empty());
        when(paymentCommandService.createPreference(any(), any(), any(), any())).thenReturn(Optional.empty());
        when(messageSource.getMessage(eq("error.unexpected.message"), any(), any())).thenReturn("An unexpected error occurred");

        CreateMercadoPagoPreferenceResource resource = new CreateMercadoPagoPreferenceResource(quoteId);

        ResponseEntity<?> response = controller.createPreference(resource);

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
    }

    @Test
    void handleWebhook_WhenValidSignatureAndBody_ShouldReturnOk() throws Exception {
        String dataId = "123456789";
        String requestId = "req-123";
        String ts = String.valueOf(Instant.now().getEpochSecond());
        String validSignature = generateValidSignature(TEST_SECRET, dataId, requestId, ts);

        MercadoPagoWebhookResource body = new MercadoPagoWebhookResource(
                "payment.created",
                "payment",
                new MercadoPagoWebhookResource.MercadoPagoWebhookData(dataId),
                null
        );

        when(paymentCommandService.getPaymentStatus(123456789L)).thenReturn(Optional.of(
                new MercadoPagoPaymentResult(
                        123456789L, "approved", "accredited", new BigDecimal("150.00"), "PEN", "quote-1"
                )
        ));

        ResponseEntity<Void> response = controller.handleWebhook(validSignature, requestId, null, null, null, null, body);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        verify(paymentCommandService).getPaymentStatus(123456789L);
    }

    @Test
    void handleWebhook_WhenValidSignatureAndQueryParams_ShouldReturnOk() throws Exception {
        String dataId = "987654321";
        String requestId = "req-456";
        String ts = String.valueOf(Instant.now().getEpochSecond());
        String validSignature = generateValidSignature(TEST_SECRET, dataId, requestId, ts);

        when(paymentCommandService.getPaymentStatus(987654321L)).thenReturn(Optional.of(
                new MercadoPagoPaymentResult(
                        987654321L, "approved", "accredited", new BigDecimal("200.00"), "PEN", "quote-2"
                )
        ));

        ResponseEntity<Void> response = controller.handleWebhook(validSignature, requestId, null, dataId, "payment", null, null);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        verify(paymentCommandService).getPaymentStatus(987654321L);
    }

    @Test
    void handleWebhook_WhenApprovedPaymentAndIntentExists_ShouldAutonomouslyIssueVoucher() throws Exception {
        UUID quoteId = UUID.randomUUID();
        String dataId = "123456789";
        String requestId = "req-123";
        String ts = String.valueOf(Instant.now().getEpochSecond());
        String validSignature = generateValidSignature(TEST_SECRET, dataId, requestId, ts);

        MercadoPagoWebhookResource body = new MercadoPagoWebhookResource(
                "payment.created",
                "payment",
                new MercadoPagoWebhookResource.MercadoPagoWebhookData(dataId),
                null
        );

        when(paymentCommandService.getPaymentStatus(123456789L)).thenReturn(Optional.of(
                new MercadoPagoPaymentResult(
                        123456789L, "approved", "accredited", new BigDecimal("150.00"), "PEN", quoteId.toString()
                )
        ));
        when(voucherQueryService.handle(any(GetVoucherByQuoteIdQuery.class))).thenReturn(Optional.empty());

        com.tuxlogic.shiftiq.platform.billing.infrastructure.persistence.jpa.entities.PaymentIntentPersistenceEntity mockIntent =
                new com.tuxlogic.shiftiq.platform.billing.infrastructure.persistence.jpa.entities.PaymentIntentPersistenceEntity(
                        quoteId,
                        new BigDecimal("150.00"),
                        "RECEIPT",
                        "DNI",
                        "12345678",
                        "Carlos Perez"
                );
        when(paymentCommandService.getPaymentIntent(quoteId)).thenReturn(Optional.of(mockIntent));

        Voucher mockVoucher = mock(Voucher.class);
        when(voucherCommandService.handle(any(ProcessMercadoPagoCheckoutCommand.class)))
                .thenReturn(Result.success(mockVoucher));

        ResponseEntity<Void> response = controller.handleWebhook(validSignature, requestId, null, null, null, null, body);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        verify(voucherCommandService).handle(argThat((ProcessMercadoPagoCheckoutCommand cmd) ->
                cmd.quoteId().equals(quoteId) &&
                cmd.type() == VoucherType.RECEIPT &&
                "12345678".equals(cmd.customerDocumentNumber()) &&
                "123456789".equals(cmd.paymentId())
        ));
    }

    @Test
    void createPreference_WhenInvoiceWithoutRuc_ShouldReturnBadRequest() {
        UUID quoteId = UUID.randomUUID();
        UUID branchId = UUID.randomUUID();
        BigDecimal amount = new BigDecimal("150.00");

        Quote quote = new Quote(quoteId, UUID.randomUUID(), new BranchId(branchId), new Money(amount), 0.0, new Money(amount), QuoteStatus.APPROVED);
        when(quoteQueryService.handle(any(GetQuoteByIdQuery.class))).thenReturn(Optional.of(quote));
        when(voucherQueryService.handle(any(GetVoucherByQuoteIdQuery.class))).thenReturn(Optional.empty());

        // INVOICE with invalid DNI (not RUC, not 11 digits)
        CreateMercadoPagoPreferenceResource resource = new CreateMercadoPagoPreferenceResource(
                quoteId, "INVOICE", "DNI", "12345678", "Empresa SAC"
        );

        ResponseEntity<?> response = controller.createPreference(resource);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        verify(paymentCommandService, never()).createPreference(any(), any(), any(), any());
    }

    @Test
    void handleWebhook_WhenInvalidSignature_ShouldReturnUnauthorized() {
        MercadoPagoWebhookResource body = new MercadoPagoWebhookResource(
                "payment.created",
                "payment",
                new MercadoPagoWebhookResource.MercadoPagoWebhookData("123456789"),
                null
        );

        ResponseEntity<Void> response = controller.handleWebhook(
                "ts=123456,v1=invalid_hash",
                "req-1",
                null,
                null,
                null,
                null,
                body
        );

        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
        verify(paymentCommandService, never()).getPaymentStatus(any());
    }

    @Test
    void handleWebhook_WhenStaleTimestamp_ShouldReturnUnauthorized() throws Exception {
        String dataId = "123456789";
        String requestId = "req-123";
        // 10 minutes in the past (> 300s)
        String staleTs = String.valueOf(Instant.now().getEpochSecond() - 600);
        String signature = generateValidSignature(TEST_SECRET, dataId, requestId, staleTs);

        MercadoPagoWebhookResource body = new MercadoPagoWebhookResource(
                "payment.created",
                "payment",
                new MercadoPagoWebhookResource.MercadoPagoWebhookData(dataId),
                null
        );

        ResponseEntity<Void> response = controller.handleWebhook(signature, requestId, null, null, null, null, body);

        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
        verify(paymentCommandService, never()).getPaymentStatus(any());
    }

    @Test
    void handleWebhook_WhenFutureTimestamp_ShouldReturnUnauthorized() throws Exception {
        String dataId = "123456789";
        String requestId = "req-123";
        // 10 minutes in the future (> 300s)
        String futureTs = String.valueOf(Instant.now().getEpochSecond() + 600);
        String signature = generateValidSignature(TEST_SECRET, dataId, requestId, futureTs);

        MercadoPagoWebhookResource body = new MercadoPagoWebhookResource(
                "payment.created",
                "payment",
                new MercadoPagoWebhookResource.MercadoPagoWebhookData(dataId),
                null
        );

        ResponseEntity<Void> response = controller.handleWebhook(signature, requestId, null, null, null, null, body);

        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
        verify(paymentCommandService, never()).getPaymentStatus(any());
    }

    @Test
    void handleWebhook_WhenAutonomousIssuanceFails_ShouldReturnServiceUnavailable() throws Exception {
        UUID quoteId = UUID.randomUUID();
        String dataId = "123456789";
        String requestId = "req-123";
        String ts = String.valueOf(Instant.now().getEpochSecond());
        String validSignature = generateValidSignature(TEST_SECRET, dataId, requestId, ts);

        MercadoPagoWebhookResource body = new MercadoPagoWebhookResource(
                "payment.created",
                "payment",
                new MercadoPagoWebhookResource.MercadoPagoWebhookData(dataId),
                null
        );

        when(paymentCommandService.getPaymentStatus(123456789L)).thenReturn(Optional.of(
                new MercadoPagoPaymentResult(
                        123456789L, "approved", "accredited", new BigDecimal("150.00"), "PEN", quoteId.toString()
                )
        ));
        when(voucherQueryService.handle(any(GetVoucherByQuoteIdQuery.class))).thenReturn(Optional.empty());

        com.tuxlogic.shiftiq.platform.billing.infrastructure.persistence.jpa.entities.PaymentIntentPersistenceEntity mockIntent =
                new com.tuxlogic.shiftiq.platform.billing.infrastructure.persistence.jpa.entities.PaymentIntentPersistenceEntity(
                        quoteId,
                        new BigDecimal("150.00"),
                        "RECEIPT",
                        "DNI",
                        "12345678",
                        "Carlos Perez"
                );
        when(paymentCommandService.getPaymentIntent(quoteId)).thenReturn(Optional.of(mockIntent));

        when(voucherCommandService.handle(any(ProcessMercadoPagoCheckoutCommand.class)))
                .thenReturn(Result.failure(com.tuxlogic.shiftiq.platform.billing.domain.model.valueobjects.VoucherCommandFailure.FACTOS_ISSUANCE_FAILED));

        ResponseEntity<Void> response = controller.handleWebhook(validSignature, requestId, null, null, null, null, body);

        assertEquals(HttpStatus.SERVICE_UNAVAILABLE, response.getStatusCode());
    }

    @Test
    void handleWebhook_WhenSecretUnconfigured_ShouldReturnServiceUnavailable() {
        MercadoPagoPaymentsController controllerWithoutSecret = new MercadoPagoPaymentsController(
                paymentCommandService,
                voucherCommandService,
                quoteQueryService,
                voucherQueryService,
                multiTenancySecurityService,
                messageSource,
                ""
        );

        MercadoPagoWebhookResource body = new MercadoPagoWebhookResource(
                "payment.created",
                "payment",
                new MercadoPagoWebhookResource.MercadoPagoWebhookData("123456789"),
                null
        );

        ResponseEntity<Void> response = controllerWithoutSecret.handleWebhook("ts=1,v1=abc", "req-1", null, null, null, null, body);

        assertEquals(HttpStatus.SERVICE_UNAVAILABLE, response.getStatusCode());
        verify(paymentCommandService, never()).getPaymentStatus(any());
    }

    @Test
    void handleWebhook_WhenExistingVoucherPendingAndActive_ShouldReturnServiceUnavailable() throws Exception {
        UUID quoteId = UUID.randomUUID();
        String requestId = "req-pending-active";
        String paymentId = "123456789";
        String ts = String.valueOf(Instant.now().getEpochSecond());
        String validSignature = generateValidSignature(TEST_SECRET, paymentId, requestId, ts);

        MercadoPagoWebhookResource body = new MercadoPagoWebhookResource(
                "payment.created",
                "payment",
                new MercadoPagoWebhookResource.MercadoPagoWebhookData(paymentId),
                null
        );

        MercadoPagoPaymentResult approvedPayment = new MercadoPagoPaymentResult(
                123456789L,
                "approved",
                "accredited",
                new BigDecimal("150.00"),
                "PEN",
                quoteId.toString()
        );
        when(paymentCommandService.getPaymentStatus(123456789L)).thenReturn(Optional.of(approvedPayment));

        // Active voucher created now
        Voucher activeVoucher = new Voucher(quoteId, VoucherType.RECEIPT, "DNI", "12345678", "Juan Perez",
                new Money(new BigDecimal("150.00")), null, null);
        when(voucherQueryService.handle(any(GetVoucherByQuoteIdQuery.class))).thenReturn(Optional.of(activeVoucher));

        ResponseEntity<Void> response = controller.handleWebhook(validSignature, requestId, null, null, null, null, body);

        assertEquals(HttpStatus.SERVICE_UNAVAILABLE, response.getStatusCode());
        verify(voucherCommandService, never()).handle(any(ProcessMercadoPagoCheckoutCommand.class));
    }

    @Test
    void handleWebhook_WhenExistingVoucherPendingAndStale_ShouldRetryAndReturnOk() throws Exception {
        UUID quoteId = UUID.randomUUID();
        String requestId = "req-pending-stale";
        String paymentId = "123456789";
        String ts = String.valueOf(Instant.now().getEpochSecond());
        String validSignature = generateValidSignature(TEST_SECRET, paymentId, requestId, ts);

        MercadoPagoWebhookResource body = new MercadoPagoWebhookResource(
                "payment.created",
                "payment",
                new MercadoPagoWebhookResource.MercadoPagoWebhookData(paymentId),
                null
        );

        MercadoPagoPaymentResult approvedPayment = new MercadoPagoPaymentResult(
                123456789L,
                "approved",
                "accredited",
                new BigDecimal("150.00"),
                "PEN",
                quoteId.toString()
        );
        when(paymentCommandService.getPaymentStatus(123456789L)).thenReturn(Optional.of(approvedPayment));

        // Stale voucher created 100 seconds ago
        Voucher staleVoucher = new Voucher(quoteId, VoucherType.RECEIPT, "DNI", "12345678", "Juan Perez",
                new Money(new BigDecimal("150.00")), null, null);
        staleVoucher.setUpdatedAt(Instant.now().minusSeconds(100));
        when(voucherQueryService.handle(any(GetVoucherByQuoteIdQuery.class))).thenReturn(Optional.of(staleVoucher));

        com.tuxlogic.shiftiq.platform.billing.infrastructure.persistence.jpa.entities.PaymentIntentPersistenceEntity mockIntent =
                new com.tuxlogic.shiftiq.platform.billing.infrastructure.persistence.jpa.entities.PaymentIntentPersistenceEntity(
                        quoteId,
                        new BigDecimal("150.00"),
                        "RECEIPT",
                        "DNI",
                        "12345678",
                        "Carlos Perez"
                );
        when(paymentCommandService.getPaymentIntent(quoteId)).thenReturn(Optional.of(mockIntent));

        Voucher emittedVoucher = new Voucher(quoteId, VoucherType.RECEIPT, "DNI", "12345678", "Juan Perez",
                new Money(new BigDecimal("150.00")), UUID.randomUUID(), "http://pdf");
        when(voucherCommandService.handle(any(ProcessMercadoPagoCheckoutCommand.class)))
                .thenReturn(Result.success(emittedVoucher));

        ResponseEntity<Void> response = controller.handleWebhook(validSignature, requestId, null, null, null, null, body);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        verify(voucherCommandService).handle(any(ProcessMercadoPagoCheckoutCommand.class));
    }
}
