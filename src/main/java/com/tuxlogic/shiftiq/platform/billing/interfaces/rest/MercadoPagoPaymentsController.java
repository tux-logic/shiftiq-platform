package com.tuxlogic.shiftiq.platform.billing.interfaces.rest;

import com.tuxlogic.shiftiq.platform.billing.application.commandservices.MercadoPagoPaymentCommandService;
import com.tuxlogic.shiftiq.platform.billing.application.commandservices.VoucherCommandService;
import com.tuxlogic.shiftiq.platform.billing.application.queryservices.QuoteQueryService;
import com.tuxlogic.shiftiq.platform.billing.application.queryservices.VoucherQueryService;
import com.tuxlogic.shiftiq.platform.billing.domain.model.commands.ProcessMercadoPagoCheckoutCommand;
import com.tuxlogic.shiftiq.platform.billing.domain.model.queries.GetQuoteByIdQuery;
import com.tuxlogic.shiftiq.platform.billing.domain.model.queries.GetVoucherByQuoteIdQuery;
import com.tuxlogic.shiftiq.platform.billing.domain.model.valueobjects.QuoteStatus;
import com.tuxlogic.shiftiq.platform.billing.domain.model.valueobjects.VoucherStatus;
import com.tuxlogic.shiftiq.platform.billing.domain.model.valueobjects.VoucherType;
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
import java.time.Instant;
import java.util.UUID;

/**
 * REST Controller for processing payments via Mercado Pago.
 * Exposes endpoints to create Payment Preferences derived from server-validated Quotes,
 * as well as webhook notifications for asynchronous payment state reconciliations and autonomous voucher issuance.
 */
@RestController
@RequestMapping(value = "/api/v1/payments/mercadopago", produces = "application/json")
@Tag(name = "Mercado Pago Payments", description = "Endpoints for Mercado Pago preferences, webhooks, and payment processing")
public class MercadoPagoPaymentsController {

    private static final Logger LOGGER = LoggerFactory.getLogger(MercadoPagoPaymentsController.class);

    private final MercadoPagoPaymentCommandService paymentCommandService;
    private final VoucherCommandService voucherCommandService;
    private final QuoteQueryService quoteQueryService;
    private final VoucherQueryService voucherQueryService;
    private final MultiTenancySecurityService multiTenancySecurityService;
    private final MessageSource messageSource;
    private final String webhookSecret;

    public MercadoPagoPaymentsController(
            MercadoPagoPaymentCommandService paymentCommandService,
            VoucherCommandService voucherCommandService,
            QuoteQueryService quoteQueryService,
            VoucherQueryService voucherQueryService,
            MultiTenancySecurityService multiTenancySecurityService,
            MessageSource messageSource,
            @Value("${mercadopago.webhook.secret:}") String webhookSecret) {
        this.paymentCommandService = paymentCommandService;
        this.voucherCommandService = voucherCommandService;
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

        if ("INVOICE".equalsIgnoreCase(resource.type())) {
            if (resource.customerDocumentType() == null || !"RUC".equalsIgnoreCase(resource.customerDocumentType())) {
                String msg = messageSource.getMessage("billing.error.invoice.ruc.documentType", null, LocaleContextHolder.getLocale());
                return ErrorResponseAssembler.toErrorResponseFromApplicationError(
                        ApplicationError.validationError("customerDocumentType", msg));
            }
            if (resource.customerDocumentNumber() == null || !resource.customerDocumentNumber().matches("^\\d{11}$")) {
                String msg = messageSource.getMessage("billing.error.invoice.ruc.number", null, LocaleContextHolder.getLocale());
                return ErrorResponseAssembler.toErrorResponseFromApplicationError(
                        ApplicationError.validationError("customerDocumentNumber", msg));
            }
            if (resource.customerName() == null || resource.customerName().isBlank()) {
                String msg = messageSource.getMessage("billing.error.invoice.customerName", null, LocaleContextHolder.getLocale());
                return ErrorResponseAssembler.toErrorResponseFromApplicationError(
                        ApplicationError.validationError("customerName", msg));
            }
        }

        // Persist payment intent with customer tax data so webhook can autonomously issue voucher if client leaves
        paymentCommandService.registerPaymentIntent(
                quote.getId(),
                resource.type() != null ? resource.type() : "RECEIPT",
                resource.customerDocumentType() != null ? resource.customerDocumentType() : "DNI",
                resource.customerDocumentNumber() != null ? resource.customerDocumentNumber() : "00000000",
                resource.customerName() != null ? resource.customerName() : "Cliente General",
                quote.getTotalAmount().amount()
        );

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
            @RequestParam(value = "id", required = false) String idParam,
            @RequestParam(value = "data.id", required = false) String dataIdParam,
            @RequestParam(value = "type", required = false) String type,
            @RequestParam(value = "topic", required = false) String topic,
            @RequestBody(required = false) MercadoPagoWebhookResource bodyResource
    ) {
        String paymentIdStr = null;
        String notificationType = null;

        if (bodyResource != null) {
            if (bodyResource.data() != null && bodyResource.data().id() != null) {
                paymentIdStr = bodyResource.data().id();
            } else if (bodyResource.id() != null) {
                paymentIdStr = String.valueOf(bodyResource.id());
            }
            if (bodyResource.type() != null) {
                notificationType = bodyResource.type();
            } else if (bodyResource.action() != null) {
                notificationType = bodyResource.action();
            }
        }

        if (paymentIdStr == null) {
            paymentIdStr = dataIdParam != null ? dataIdParam : idParam;
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

                    if ("approved".equalsIgnoreCase(status.status()) || "succeeded".equalsIgnoreCase(status.status())) {
                        String externalRef = status.externalReference();
                        if (externalRef != null && !externalRef.isBlank()) {
                            try {
                                UUID quoteId = UUID.fromString(externalRef);
                                var existingVoucherOpt = voucherQueryService.handle(new GetVoucherByQuoteIdQuery(quoteId));
                                
                                if (existingVoucherOpt.isPresent()) {
                                    var existingVoucher = existingVoucherOpt.get();
                                    if (existingVoucher.getStatus() == VoucherStatus.PAID) {
                                        LOGGER.info("Voucher already exists and is PAID for quote ID '{}'. Webhook confirmed.", quoteId);
                                        return ResponseEntity.ok().build();
                                    }
                                    if (existingVoucher.getStatus() == VoucherStatus.EMISSION_FAILED) {
                                        LOGGER.info("Existing voucher in EMISSION_FAILED for quote ID '{}'. Retrying emission via webhook...", quoteId);
                                        var intentOpt = paymentCommandService.getPaymentIntent(quoteId);
                                        if (intentOpt.isPresent()) {
                                            var intent = intentOpt.get();
                                            VoucherType voucherType = "INVOICE".equalsIgnoreCase(intent.getVoucherType())
                                                    ? VoucherType.INVOICE : VoucherType.RECEIPT;
                                            var checkoutCmd = new ProcessMercadoPagoCheckoutCommand(
                                                    quoteId,
                                                    voucherType,
                                                    intent.getCustomerDocumentType(),
                                                    intent.getCustomerDocumentNumber(),
                                                    intent.getCustomerName(),
                                                    paymentIdStr
                                            );
                                            var retryResult = voucherCommandService.handle(checkoutCmd);
                                            if (retryResult.isSuccess()) {
                                                LOGGER.info("Successfully recovered and issued voucher on retry for quote ID '{}'", quoteId);
                                                return ResponseEntity.ok().build();
                                            } else {
                                                LOGGER.warn("Webhook retry emission failed for quote ID '{}': {}. Returning 503 to trigger Mercado Pago retry.",
                                                        quoteId, retryResult.failure().get());
                                                return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).build();
                                            }
                                        } else {
                                            LOGGER.warn("No PaymentIntent found to retry EMISSION_FAILED voucher for quote ID '{}'. Returning 503 to keep Mercado Pago retrying.", quoteId);
                                            return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).build();
                                        }
                                    }
                                    if (existingVoucher.getStatus() == VoucherStatus.PENDING) {
                                        java.time.Instant lastUpdate = existingVoucher.getUpdatedAt() != null ? existingVoucher.getUpdatedAt() : existingVoucher.getCreatedAt();
                                        boolean isStale = lastUpdate != null && java.time.Duration.between(lastUpdate, java.time.Instant.now()).toSeconds() >= 60;
                                        if (isStale) {
                                            LOGGER.info("Existing voucher in PENDING is stale (>=60s) for quote ID '{}'. Retrying emission via webhook...", quoteId);
                                            var intentOpt = paymentCommandService.getPaymentIntent(quoteId);
                                            if (intentOpt.isPresent()) {
                                                var intent = intentOpt.get();
                                                VoucherType voucherType = "INVOICE".equalsIgnoreCase(intent.getVoucherType())
                                                        ? VoucherType.INVOICE : VoucherType.RECEIPT;
                                                var checkoutCmd = new ProcessMercadoPagoCheckoutCommand(
                                                        quoteId,
                                                        voucherType,
                                                        intent.getCustomerDocumentType(),
                                                        intent.getCustomerDocumentNumber(),
                                                        intent.getCustomerName(),
                                                        paymentIdStr
                                                );
                                                var retryResult = voucherCommandService.handle(checkoutCmd);
                                                if (retryResult.isSuccess()) {
                                                    LOGGER.info("Successfully recovered and issued voucher on stale PENDING retry for quote ID '{}'", quoteId);
                                                    return ResponseEntity.ok().build();
                                                } else {
                                                    LOGGER.warn("Webhook retry emission failed for stale PENDING quote ID '{}': {}. Returning 503 to trigger MP retry.",
                                                            quoteId, retryResult.failure().get());
                                                    return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).build();
                                                }
                                            } else {
                                                LOGGER.warn("No PaymentIntent found to recover stale PENDING voucher for quote ID '{}'. Returning 503 to keep Mercado Pago retrying.", quoteId);
                                                return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).build();
                                            }
                                        } else {
                                            LOGGER.info("Voucher for quote ID '{}' is currently PENDING (<60s in progress). Returning 503 so MP retries later.", quoteId);
                                            return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).build();
                                        }
                                    }
                                    LOGGER.info("Voucher for quote ID '{}' is in status '{}'.", quoteId, existingVoucher.getStatus());
                                    return ResponseEntity.ok().build();
                                } else {
                                    // Autonomous issuance via stored PaymentIntent
                                    var intentOpt = paymentCommandService.getPaymentIntent(quoteId);
                                    if (intentOpt.isPresent()) {
                                        var intent = intentOpt.get();
                                        if (status.amount() != null && intent.getAmount() != null
                                                && status.amount().compareTo(intent.getAmount()) != 0) {
                                            LOGGER.warn("Webhook payment amount '{}' does not match intent amount '{}' for quote ID '{}'",
                                                    status.amount(), intent.getAmount(), quoteId);
                                            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
                                        }

                                        VoucherType voucherType = "INVOICE".equalsIgnoreCase(intent.getVoucherType())
                                                ? VoucherType.INVOICE : VoucherType.RECEIPT;
                                        var checkoutCmd = new ProcessMercadoPagoCheckoutCommand(
                                                quoteId,
                                                voucherType,
                                                intent.getCustomerDocumentType(),
                                                intent.getCustomerDocumentNumber(),
                                                intent.getCustomerName(),
                                                paymentIdStr
                                        );
                                        var checkoutResult = voucherCommandService.handle(checkoutCmd);
                                        if (checkoutResult.isSuccess()) {
                                            LOGGER.info("Autonomously issued voucher via webhook for quote ID '{}' and payment ID '{}'",
                                                    quoteId, paymentIdStr);
                                            return ResponseEntity.ok().build();
                                        } else {
                                            LOGGER.warn("Webhook autonomous checkout failed for quote ID '{}': {}. Returning 503 to trigger Mercado Pago retry.",
                                                    quoteId, checkoutResult.failure().get());
                                            return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).build();
                                        }
                                    } else {
                                        LOGGER.warn("Payment intent not found for quote ID '{}'. Awaiting client checkout.", quoteId);
                                    }
                                }
                            } catch (IllegalArgumentException e) {
                                LOGGER.warn("Invalid externalReference UUID in payment: {}", externalRef);
                            }
                        }
                    } else if ("refunded".equalsIgnoreCase(status.status()) || "charged_back".equalsIgnoreCase(status.status())) {
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

            // Freshness verification: Reject timestamps older than 5 minutes or in future
            try {
                long tsSeconds = Long.parseLong(ts);
                long currentSeconds = Instant.now().getEpochSecond();
                if (Math.abs(currentSeconds - tsSeconds) > 300) {
                    LOGGER.warn("Rejecting Mercado Pago webhook: timestamp is stale or in the future (ts={}, now={})", tsSeconds, currentSeconds);
                    return false;
                }
            } catch (NumberFormatException e) {
                LOGGER.warn("Rejecting Mercado Pago webhook: invalid ts timestamp format '{}'", ts);
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
