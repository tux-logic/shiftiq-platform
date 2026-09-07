package com.tuxlogic.shiftiq.platform.billing.application.internal.commandservices;

import com.tuxlogic.shiftiq.platform.billing.application.commandservices.StripePaymentCommandService;
import com.tuxlogic.shiftiq.platform.billing.application.outboundservices.PaymentGateway;
import com.tuxlogic.shiftiq.platform.billing.application.outboundservices.StripePaymentIntentResult;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Optional;

/**
 * Implementation of StripePaymentCommandService orchestrating domain payment operations and gateway interactions.
 */
@Service
public class StripePaymentCommandServiceImpl implements StripePaymentCommandService {

    private final PaymentGateway paymentGateway;

    public StripePaymentCommandServiceImpl(PaymentGateway paymentGateway) {
        this.paymentGateway = paymentGateway;
    }

    @Override
    @Transactional
    public Optional<StripePaymentIntentResult> createPaymentIntent(BigDecimal amount, String currency, String description) {
        return paymentGateway.createPaymentIntent(amount, currency, description)
                .map(res -> new StripePaymentIntentResult(res.paymentIntentId(), res.clientSecret(), res.amount(), res.currency(), res.status()));
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<StripePaymentIntentResult> getPaymentIntent(String paymentIntentId) {
        return paymentGateway.getPaymentIntent(paymentIntentId)
                .map(res -> new StripePaymentIntentResult(res.paymentIntentId(), res.clientSecret(), res.amount(), res.currency(), res.status()));
    }
}
