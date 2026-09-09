package com.tuxlogic.shiftiq.platform.billing.interfaces.rest;

import com.tuxlogic.shiftiq.platform.billing.application.commandservices.StripePaymentCommandService;
import com.tuxlogic.shiftiq.platform.billing.interfaces.rest.resources.CreatePaymentIntentResource;
import com.tuxlogic.shiftiq.platform.billing.interfaces.rest.resources.PaymentIntentResource;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.RestController;

/**
 * REST Controller for processing credit/debit card payments via Stripe.
 * Exposes endpoints to create PaymentIntents for the mobile/web application client.
 */
@RestController
@RequestMapping(value = "/api/v1/payments/stripe", produces = "application/json")
@Tag(name = "Stripe Payments", description = "Endpoints for creating Stripe PaymentIntents to process card payments")
@PreAuthorize("isAuthenticated()")
public class StripePaymentsController {

    private final StripePaymentCommandService paymentCommandService;
    private final com.tuxlogic.shiftiq.platform.shared.infrastructure.security.MultiTenancySecurityService multiTenancySecurityService;

    public StripePaymentsController(StripePaymentCommandService paymentCommandService, com.tuxlogic.shiftiq.platform.shared.infrastructure.security.MultiTenancySecurityService multiTenancySecurityService) {
        this.paymentCommandService = paymentCommandService;
        this.multiTenancySecurityService = multiTenancySecurityService;
    }

    @PostMapping("/payment-intents")
    @Operation(summary = "Create a Stripe PaymentIntent", description = "Generates a Stripe PaymentIntent and clientSecret for card payment processing")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<PaymentIntentResource> createPaymentIntent(@Valid @RequestBody CreatePaymentIntentResource resource) {
        if (resource.branchId() != null) {
            multiTenancySecurityService.validateBranchAccess(resource.branchId());
        }

        var resultOpt = paymentCommandService.createPaymentIntent(
                resource.amount(),
                resource.currency(),
                resource.description()
        );

        if (resultOpt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }

        var result = resultOpt.get();
        var responseResource = new PaymentIntentResource(
                result.paymentIntentId(),
                result.clientSecret(),
                result.amount(),
                result.currency(),
                result.status()
        );

        return new ResponseEntity<>(responseResource, HttpStatus.CREATED);
    }

}
