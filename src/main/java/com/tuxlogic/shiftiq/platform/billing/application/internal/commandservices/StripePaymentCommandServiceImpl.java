package com.tuxlogic.shiftiq.platform.billing.application.internal.commandservices;

import com.tuxlogic.shiftiq.platform.billing.application.commandservices.StripePaymentCommandService;
import com.tuxlogic.shiftiq.platform.billing.application.outboundservices.StripeGateway;
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

    private final StripeGateway stripeGateway;

    public StripePaymentCommandServiceImpl(StripeGateway stripeGateway) {
        this.stripeGateway = stripeGateway;
    }

    @Override
    @Transactional
    public Optional<StripePaymentIntentResult> createPaymentIntent(BigDecimal amount, String currency, String description) {
        return stripeGateway.createStripePaymentIntent(amount, currency, description);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<StripePaymentIntentResult> getPaymentIntent(String paymentIntentId) {
        return stripeGateway.getStripePaymentIntent(paymentIntentId);
    }
}
