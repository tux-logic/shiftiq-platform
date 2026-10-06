package com.tuxlogic.shiftiq.platform.billing.application.commandservices;

import com.tuxlogic.shiftiq.platform.billing.application.outboundservices.MercadoPagoPaymentResult;
import com.tuxlogic.shiftiq.platform.billing.application.outboundservices.MercadoPagoPreferenceResult;

import java.math.BigDecimal;
import java.util.Optional;

/**
 * Application command service interface for Mercado Pago payment domain operations.
 */
public interface MercadoPagoPaymentCommandService {
    Optional<MercadoPagoPreferenceResult> createPreference(BigDecimal amount, String currency, String title, String externalReference);
    Optional<MercadoPagoPaymentResult> getPaymentStatus(Long paymentId);
}
