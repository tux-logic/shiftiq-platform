package com.tuxlogic.shiftiq.platform.billing.application.commandservices;

import com.tuxlogic.shiftiq.platform.billing.application.outboundservices.MercadoPagoPaymentResult;
import com.tuxlogic.shiftiq.platform.billing.application.outboundservices.MercadoPagoPreferenceResult;
import com.tuxlogic.shiftiq.platform.billing.infrastructure.persistence.jpa.entities.PaymentIntentPersistenceEntity;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;

/**
 * Application command service interface for Mercado Pago payment domain operations.
 */
public interface MercadoPagoPaymentCommandService {
    Optional<MercadoPagoPreferenceResult> createPreference(BigDecimal amount, String currency, String title, String externalReference);
    Optional<MercadoPagoPaymentResult> getPaymentStatus(Long paymentId);
    void registerPaymentIntent(UUID quoteId, String voucherType, String customerDocumentType, String customerDocumentNumber, String customerName, BigDecimal amount);
    Optional<PaymentIntentPersistenceEntity> getPaymentIntent(UUID quoteId);
}
