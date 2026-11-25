package com.tuxlogic.shiftiq.platform.billing.infrastructure.outbound.mercadopago;

import com.tuxlogic.shiftiq.platform.billing.application.outboundservices.MercadoPagoGateway;
import com.tuxlogic.shiftiq.platform.billing.application.outboundservices.PaymentCheckoutSessionResult;
import com.tuxlogic.shiftiq.platform.billing.application.outboundservices.PaymentGatewayStrategy;
import com.tuxlogic.shiftiq.platform.billing.application.outboundservices.PaymentResult;
import com.tuxlogic.shiftiq.platform.billing.domain.model.valueobjects.PaymentProvider;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.Optional;

/**
 * {@link PaymentGatewayStrategy} implementation backed by the Mercado Pago Java SDK adapter.
 */
@Component
public class MercadoPagoGatewayStrategy implements PaymentGatewayStrategy {

    private final MercadoPagoGateway mercadoPagoGateway;

    public MercadoPagoGatewayStrategy(MercadoPagoGateway mercadoPagoGateway) {
        this.mercadoPagoGateway = mercadoPagoGateway;
    }

    @Override
    public PaymentProvider provider() {
        return PaymentProvider.MERCADO_PAGO;
    }

    @Override
    public Optional<PaymentCheckoutSessionResult> createCheckoutSession(
            BigDecimal amount,
            String currency,
            String title,
            String externalReference) {
        return mercadoPagoGateway.createPreference(amount, currency, title, externalReference)
                .map(pref -> new PaymentCheckoutSessionResult(
                        PaymentProvider.MERCADO_PAGO,
                        pref.preferenceId(),
                        pref.initPoint(),
                        pref.sandboxInitPoint(),
                        pref.amount(),
                        pref.currency(),
                        pref.externalReference()
                ));
    }

    @Override
    public Optional<PaymentResult> getPaymentStatusByExternalId(String externalPaymentId) {
        return mercadoPagoGateway.getPaymentStatusByExternalId(externalPaymentId);
    }
}
