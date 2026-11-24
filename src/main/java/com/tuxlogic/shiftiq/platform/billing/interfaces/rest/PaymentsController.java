package com.tuxlogic.shiftiq.platform.billing.interfaces.rest;

import com.tuxlogic.shiftiq.platform.billing.application.commandservices.PaymentSessionService;
import com.tuxlogic.shiftiq.platform.billing.domain.model.valueobjects.PaymentProvider;
import com.tuxlogic.shiftiq.platform.billing.interfaces.rest.resources.CreatePaymentSessionResource;
import com.tuxlogic.shiftiq.platform.billing.interfaces.rest.resources.PaymentSessionResource;
import com.tuxlogic.shiftiq.platform.shared.interfaces.rest.transform.ErrorResponseAssembler;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Provider-agnostic REST controller for initiating virtual payment checkout sessions.
 */
@RestController
@RequestMapping(value = "/api/v1/payments", produces = "application/json")
@Tag(name = "Payments", description = "Provider-agnostic virtual payment checkout endpoints")
public class PaymentsController {

    private final PaymentSessionService paymentSessionService;

    public PaymentsController(PaymentSessionService paymentSessionService) {
        this.paymentSessionService = paymentSessionService;
    }

    @PostMapping("/sessions")
    @PreAuthorize("isAuthenticated()")
    @Operation(
            summary = "Create a virtual payment checkout session",
            description = "Creates a hosted checkout session for an APPROVED quote using the selected PaymentProvider (defaults to MERCADO_PAGO). Accessible by branch staff or the owning customer once the work order is COMPLETED."
    )
    public ResponseEntity<?> createPaymentSession(
            @Valid @RequestBody CreatePaymentSessionResource resource,
            Authentication authentication) {
        PaymentProvider provider = resource.provider() != null ? resource.provider() : PaymentProvider.MERCADO_PAGO;
        var result = paymentSessionService.createCheckoutSession(
                resource.quoteId(),
                provider,
                resource.type(),
                resource.customerDocumentType(),
                resource.customerDocumentNumber(),
                resource.customerName(),
                authentication
        );

        return result.fold(
                session -> new ResponseEntity<>(
                        new PaymentSessionResource(
                                session.provider(),
                                session.sessionId(),
                                session.checkoutUrl(),
                                session.sandboxCheckoutUrl(),
                                session.amount(),
                                session.currency(),
                                session.externalReference()
                        ),
                        HttpStatus.CREATED
                ),
                ErrorResponseAssembler::toErrorResponseFromApplicationError
        );
    }
}
