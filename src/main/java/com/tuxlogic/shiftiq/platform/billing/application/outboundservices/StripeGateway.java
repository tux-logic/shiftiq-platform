package com.tuxlogic.shiftiq.platform.billing.application.outboundservices;

import com.tuxlogic.shiftiq.platform.billing.infrastructure.outbound.stripe.StripePaymentIntentResponse;

import java.math.BigDecimal;
import java.util.Optional;

public interface StripeGateway {
    Optional<StripePaymentIntentResponse> createPaymentIntent(BigDecimal amount, String currency, String description);
}
