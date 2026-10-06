package com.tuxlogic.shiftiq.platform.billing.interfaces.rest;

import com.tuxlogic.shiftiq.platform.billing.application.commandservices.MercadoPagoPaymentCommandService;
import com.tuxlogic.shiftiq.platform.billing.application.queryservices.QuoteQueryService;
import com.tuxlogic.shiftiq.platform.billing.domain.model.queries.GetQuoteByIdQuery;
import com.tuxlogic.shiftiq.platform.billing.domain.model.valueobjects.QuoteStatus;
import com.tuxlogic.shiftiq.platform.billing.interfaces.rest.resources.CreateMercadoPagoPreferenceResource;
import com.tuxlogic.shiftiq.platform.billing.interfaces.rest.resources.MercadoPagoPreferenceResource;
import com.tuxlogic.shiftiq.platform.shared.application.result.ApplicationError;
import com.tuxlogic.shiftiq.platform.shared.infrastructure.security.MultiTenancySecurityService;
import com.tuxlogic.shiftiq.platform.shared.interfaces.rest.transform.ErrorResponseAssembler;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

/**
 * REST Controller for processing payments via Mercado Pago.
 * Exposes endpoints to create Payment Preferences derived from server-validated Quotes.
 */
@RestController
@RequestMapping(value = "/api/v1/payments/mercadopago", produces = "application/json")
@Tag(name = "Mercado Pago Payments", description = "Endpoints for Mercado Pago preferences and payment processing")
@PreAuthorize("isAuthenticated()")
public class MercadoPagoPaymentsController {

    private final MercadoPagoPaymentCommandService paymentCommandService;
    private final QuoteQueryService quoteQueryService;
    private final MultiTenancySecurityService multiTenancySecurityService;
    private final MessageSource messageSource;

    public MercadoPagoPaymentsController(
            MercadoPagoPaymentCommandService paymentCommandService,
            QuoteQueryService quoteQueryService,
            MultiTenancySecurityService multiTenancySecurityService,
            MessageSource messageSource) {
        this.paymentCommandService = paymentCommandService;
        this.quoteQueryService = quoteQueryService;
        this.multiTenancySecurityService = multiTenancySecurityService;
        this.messageSource = messageSource;
    }

    @PostMapping("/preferences")
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

        var resultOpt = paymentCommandService.createPreference(
                quote.getTotalAmount().amount(),
                "PEN",
                "Cotizacion ShiftIQ #" + quote.getId().toString().substring(0, 8),
                quote.getId().toString()
        );

        if (resultOpt.isEmpty()) {
            return ErrorResponseAssembler.toErrorResponseFromApplicationError(
                    ApplicationError.unexpected("preference", "Failed to create Mercado Pago preference with external gateway"));
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
}
