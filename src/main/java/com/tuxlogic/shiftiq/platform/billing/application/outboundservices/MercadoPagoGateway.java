package com.tuxlogic.shiftiq.platform.billing.application.outboundservices;

import java.math.BigDecimal;
import java.util.Optional;

/**
 * Outbound service interface for Mercado Pago payment operations.
 */
public interface MercadoPagoGateway extends PaymentGateway {
    Optional<MercadoPagoPreferenceResult> createPreference(BigDecimal amount, String currency, String title, String externalReference);
    Optional<MercadoPagoPaymentResult> getPaymentStatus(Long paymentId);
}
