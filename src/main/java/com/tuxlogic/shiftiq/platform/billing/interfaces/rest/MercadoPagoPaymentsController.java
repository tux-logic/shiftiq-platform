package com.tuxlogic.shiftiq.platform.billing.interfaces.rest;

import com.tuxlogic.shiftiq.platform.billing.application.commandservices.MercadoPagoPaymentCommandService;
import com.tuxlogic.shiftiq.platform.billing.application.queryservices.QuoteQueryService;
import com.tuxlogic.shiftiq.platform.billing.application.queryservices.VoucherQueryService;
import com.tuxlogic.shiftiq.platform.billing.domain.model.queries.GetQuoteByIdQuery;
import com.tuxlogic.shiftiq.platform.billing.domain.model.queries.GetVoucherByQuoteIdQuery;
import com.tuxlogic.shiftiq.platform.billing.domain.model.valueobjects.QuoteStatus;
import com.tuxlogic.shiftiq.platform.billing.interfaces.rest.resources.CreateMercadoPagoPreferenceResource;
import com.tuxlogic.shiftiq.platform.billing.interfaces.rest.resources.MercadoPagoPreferenceResource;
import com.tuxlogic.shiftiq.platform.billing.interfaces.rest.resources.MercadoPagoWebhookResource;
import com.tuxlogic.shiftiq.platform.shared.application.result.ApplicationError;
import com.tuxlogic.shiftiq.platform.shared.infrastructure.security.MultiTenancySecurityService;
import com.tuxlogic.shiftiq.platform.shared.interfaces.rest.transform.ErrorResponseAssembler;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.nio.charset.StandardCharsets;

/**
 * REST Controller for processing payments via Mercado Pago.
 * Exposes endpoints to create Payment Preferences derived from server-validated Quotes,
 * as well as webhook notifications for asynchronous payment state reconciliations.
 */
@RestController
@RequestMapping(value = "/api/v1/payments/mercadopago", produces = "application/json")
@Tag(name = "Mercado Pago Payments", description = "Endpoints for Mercado Pago preferences, webhooks, and payment processing")
public class MercadoPagoPaymentsController {

    private static final Logger LOGGER = LoggerFactory.getLogger(MercadoPagoPaymentsController.class);

    private final MercadoPagoPaymentCommandService paymentCommandService;
    private final QuoteQueryService quoteQueryService;
    private final VoucherQueryService voucherQueryService;
    private final MultiTenancySecurityService multiTenancySecurityService;
    private final MessageSource messageSource;
    private final String webhookSecret;

    public MercadoPagoPaymentsController(
            MercadoPagoPaymentCommandService paymentCommandService,
            QuoteQueryService quoteQueryService,
            VoucherQueryService voucherQueryService,
            MultiTenancySecurityService multiTenancySecurityService,
            MessageSource messageSource,
            @Value("${mercadopago.webhook.secret:}") String webhookSecret) {
        this.paymentCommandService = paymentCommandService;
        this.quoteQueryService = quoteQueryService;
        this.voucherQueryService = voucherQueryService;
        this.multiTenancySecurityService = multiTenancySecurityService;
        this.messageSource = messageSource;
        this.webhookSecret = webhookSecret;
    }

    @PostMapping("/preferences")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Create a Mercado Pago Preference", description = "Generates a Mercado Pago checkout preference derived directly from an approved Quote")
    public ResponseEntity<?> createPreference(@Valid @RequestBody CreateMercadoPagoPreferenceResource resource) {
        var quoteOpt = quoteQueryService.handle(new GetQuoteByIdQuery(resource.quoteId()));
        if (quoteOpt.isEmpty()) {
            String message = messageSource.getMessage("billing.error.quote.notFound", null, LocaleContextHolder.getLocale());
            return ErrorResponseAssembler.toErrorResponseFromApplicationError(ApplicationError.notFound("preference", message));
        }

        var quote = quoteOpt.get();
        multiTenancySecurityService.validateBranchAccess(quote.getBranchId().value());

        if (quote.getStatus() != QuoteStatus.APPROVED) {
            String message = messageSource.getMessage("billing.error.voucher.notApproved", null, LocaleContextHolder.getLocale());
            return ErrorResponseAssembler.toErrorResponseFromApplicationError(ApplicationError.conflict("preference", message));
        }

        if (voucherQueryService.handle(new GetVoucherByQuoteIdQuery(quote.getId())).isPresent()) {
            String message = messageSource.getMessage("billing.error.quote.alreadyInvoiced", null, LocaleContextHolder.getLocale());
            return ErrorResponseAssembler.toErrorResponseFromApplicationError(ApplicationError.conflict("preference", message));
        }

        var resultOpt = paymentCommandService.createPreference(
                quote.getTotalAmount().amount(),
                "PEN",
                "Cotizacion ShiftIQ #" + quote.getId().toString().substring(0, 8),
                quote.getId().toString()
        );

        if (resultOpt.isEmpty()) {
            String message = messageSource.getMessage("error.unexpected.message", null, LocaleContextHolder.getLocale());
            return ErrorResponseAssembler.toErrorResponseFromApplicationError(
                    ApplicationError.unexpected("preference", message));
        }

        var result = resultOpt.get();
        var responseResource = new MercadoPagoPreferenceResource(
                result.preferenceId(),
                result.initPoint(),
                result.sandboxInitPoint(),
                result.amount(),
                result.currency(),
                result.externalReference()
        );

        return new ResponseEntity<>(responseResource, HttpStatus.CREATED);
    }

    @PostMapping("/webhooks")
    @Operation(summary = "Handle Mercado Pago Webhook / IPN notifications", description = "Receives asynchronous payment status updates from Mercado Pago")
    public ResponseEntity<Void> handleWebhook(
            @RequestHeader(value = "x-signature", required = false) String xSignature,
            @RequestHeader(value = "x-request-id", required = false) String xRequestId,
            @RequestBody(required = false) MercadoPagoWebhookResource body,
            @RequestParam(name = "type", required = false) String type,
            @RequestParam(name = "topic", required = false) String topic,
            @RequestParam(name = "id", required = false) String id,
            @RequestParam(name = "data.id", required = false) String dataId) {

        String paymentIdStr = null;
        String notificationType = null;

        if (body != null) {
            if (body.data() != null && body.data().id() != null) {
                paymentIdStr = body.data().id();
            } else if (body.id() != null) {
                paymentIdStr = body.id().toString();
            }
            notificationType = body.type() != null ? body.type() : body.action();
        }

        if (paymentIdStr == null) {
            paymentIdStr = dataId != null ? dataId : id;
        }
        if (notificationType == null) {
            notificationType = type != null ? type : topic;
        }

        if (webhookSecret == null || webhookSecret.isBlank()) {
            LOGGER.error("Mercado Pago webhook received but mercadopago.webhook.secret is not configured. Rejecting request (fail-closed).");
            return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).build();
        }

        if (!isValidSignature(xSignature, xRequestId, paymentIdStr)) {
            LOGGER.warn("Rejecting Mercado Pago webhook due to invalid x-signature");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        if (paymentIdStr != null && (notificationType == null || notificationType.contains("payment"))) {
            try {
                Long paymentId = Long.parseLong(paymentIdStr);
                var paymentStatusOpt = paymentCommandService.getPaymentStatus(paymentId);
                if (paymentStatusOpt.isPresent()) {
                    var status = paymentStatusOpt.get();
                    LOGGER.info("Processed Mercado Pago webhook for payment ID '{}' with status '{}' and detail '{}'",
                            paymentId, status.status(), status.statusDetail());
                    if ("refunded".equalsIgnoreCase(status.status()) || "charged_back".equalsIgnoreCase(status.status())) {
                        LOGGER.warn("Payment ID '{}' has been refunded or charged back. Immediate manual/system reconciliation required.", paymentId);
                    }
                }
            } catch (NumberFormatException e) {
                LOGGER.warn("Invalid payment ID format in webhook: {}", paymentIdStr);
            }
        }

        return ResponseEntity.ok().build();
    }

    private boolean isValidSignature(String xSignature, String xRequestId, String dataId) {
        if (xSignature == null || xSignature.isBlank() || webhookSecret == null || webhookSecret.isBlank()) {
            return false;
        }
        try {
            String ts = null;
            String hash = null;
            String[] parts = xSignature.split(",");
            for (String part : parts) {
                String[] kv = part.split("=", 2);
                if (kv.length == 2) {
                    if ("ts".trim().equalsIgnoreCase(kv[0].trim())) {
                        ts = kv[1].trim();
                    } else if ("v1".trim().equalsIgnoreCase(kv[0].trim())) {
                        hash = kv[1].trim();
                    }
                }
            }
            if (ts == null || hash == null) {
                return false;
            }
            String manifest = String.format("id:%s;request-id:%s;ts:%s;", dataId != null ? dataId : "", xRequestId != null ? xRequestId : "", ts);
            javax.crypto.Mac mac = javax.crypto.Mac.getInstance("HmacSHA256");
            mac.init(new javax.crypto.spec.SecretKeySpec(webhookSecret.getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
            byte[] rawHmac = mac.doFinal(manifest.getBytes(StandardCharsets.UTF_8));
            StringBuilder hex = new StringBuilder();
            for (byte b : rawHmac) {
                hex.append(String.format("%02x", b));
            }
            byte[] expectedHashBytes = hex.toString().toLowerCase().getBytes(StandardCharsets.UTF_8);
            byte[] actualHashBytes = hash.toLowerCase().getBytes(StandardCharsets.UTF_8);
            return java.security.MessageDigest.isEqual(expectedHashBytes, actualHashBytes);
        } catch (Exception e) {
            LOGGER.warn("Error calculating webhook HMAC signature: {}", e.getMessage());
            return false;
        }
    }
}
