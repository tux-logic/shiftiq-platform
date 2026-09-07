package com.tuxlogic.shiftiq.platform.billing.infrastructure.outbound.stripe;

import com.stripe.Stripe;
import com.stripe.exception.StripeException;
import com.stripe.model.PaymentIntent;
import com.stripe.param.PaymentIntentCreateParams;
import com.tuxlogic.shiftiq.platform.billing.application.outboundservices.PaymentGateway;
import com.tuxlogic.shiftiq.platform.billing.application.outboundservices.PaymentIntentResult;
import com.tuxlogic.shiftiq.platform.billing.application.outboundservices.StripeGateway;
import com.tuxlogic.shiftiq.platform.billing.application.outboundservices.StripePaymentIntentResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Optional;

/**
 * Implementation of PaymentGateway outbound service interface.
 * Adapter connecting to the official Stripe API via the stripe-java SDK.
 */
@Service
public class StripeGatewayImpl implements StripeGateway, PaymentGateway {

    private static final Logger LOGGER = LoggerFactory.getLogger(StripeGatewayImpl.class);

    public StripeGatewayImpl(@Value("${stripe.secret.key}") String secretKey) {
        Stripe.apiKey = secretKey;
    }

    @Override
    public Optional<PaymentIntentResult> createPaymentIntent(BigDecimal amount, String currency, String description) {
        return createStripePaymentIntent(amount, currency, description).map(res -> new PaymentIntentResult(
                res.paymentIntentId(), res.clientSecret(), res.amount(), res.currency(), res.status()
        ));
    }

    @Override
    public Optional<PaymentIntentResult> getPaymentIntent(String paymentIntentId) {
        return getStripePaymentIntent(paymentIntentId).map(res -> new PaymentIntentResult(
                res.paymentIntentId(), res.clientSecret(), res.amount(), res.currency(), res.status()
        ));
    }

    @Override
    public Optional<StripePaymentIntentResult> createStripePaymentIntent(BigDecimal amount, String currency, String description) {
        try {
            // Stripe amounts are represented in cents (e.g. 10.00 -> 1000)
            long amountInCents = amount.multiply(new BigDecimal("100")).longValue();

            PaymentIntentCreateParams params = PaymentIntentCreateParams.builder()
                    .setAmount(amountInCents)
                    .setCurrency(currency != null ? currency.toLowerCase() : "pen")
                    .setDescription(description != null ? description : "ShiftIQ Billing Payment")
                    .setAutomaticPaymentMethods(
                            PaymentIntentCreateParams.AutomaticPaymentMethods.builder()
                                    .setEnabled(true)
                                    .build()
                    )
                    .build();

            PaymentIntent paymentIntent = PaymentIntent.create(params);

            return Optional.of(new StripePaymentIntentResult(
                    paymentIntent.getId(),
                    paymentIntent.getClientSecret(),
                    amount,
                    paymentIntent.getCurrency().toUpperCase(),
                    paymentIntent.getStatus()
            ));
        } catch (StripeException e) {
            LOGGER.error("Error creating payment intent: {}", e.getMessage(), e);
            return Optional.empty();
        }
    }

    @Override
    public Optional<StripePaymentIntentResult> getStripePaymentIntent(String paymentIntentId) {
        if (paymentIntentId == null || paymentIntentId.isBlank()) {
            return Optional.empty();
        }
        try {
            PaymentIntent paymentIntent = PaymentIntent.retrieve(paymentIntentId);
            BigDecimal amount = BigDecimal.valueOf(paymentIntent.getAmount()).divide(new BigDecimal("100"));
            return Optional.of(new StripePaymentIntentResult(
                    paymentIntent.getId(),
                    paymentIntent.getClientSecret(),
                    amount,
                    paymentIntent.getCurrency() != null ? paymentIntent.getCurrency().toUpperCase() : "PEN",
                    paymentIntent.getStatus()
            ));
        } catch (StripeException e) {
            LOGGER.error("Error retrieving payment intent {}: {}", paymentIntentId, e.getMessage(), e);
            return Optional.empty();
        }
    }
}
