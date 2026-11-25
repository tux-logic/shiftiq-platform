package com.tuxlogic.shiftiq.platform.billing.application.outboundservices;

import com.tuxlogic.shiftiq.platform.billing.domain.model.valueobjects.PaymentProvider;
import org.springframework.stereotype.Service;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;

/**
 * Registry/Factory resolving the active {@link PaymentGatewayStrategy} for a requested {@link PaymentProvider}.
 */
@Service
public class PaymentGatewayRegistry {

    private final Map<PaymentProvider, PaymentGatewayStrategy> strategies = new EnumMap<>(PaymentProvider.class);

    public PaymentGatewayRegistry(List<PaymentGatewayStrategy> strategyList) {
        for (PaymentGatewayStrategy strategy : strategyList) {
            strategies.put(strategy.provider(), strategy);
        }
    }

    /**
     * Resolves the strategy for the given provider.
     *
     * @param provider Target payment provider.
     * @return The matching {@link PaymentGatewayStrategy}.
     */
    public PaymentGatewayStrategy resolve(PaymentProvider provider) {
        if (provider == null || !strategies.containsKey(provider)) {
            throw new IllegalArgumentException("billing.error.payment.unsupportedProvider");
        }
        return strategies.get(provider);
    }
}
