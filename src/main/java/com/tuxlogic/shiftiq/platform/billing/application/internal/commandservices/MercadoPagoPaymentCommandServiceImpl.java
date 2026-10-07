package com.tuxlogic.shiftiq.platform.billing.application.internal.commandservices;

import com.tuxlogic.shiftiq.platform.billing.application.commandservices.MercadoPagoPaymentCommandService;
import com.tuxlogic.shiftiq.platform.billing.application.outboundservices.MercadoPagoGateway;
import com.tuxlogic.shiftiq.platform.billing.application.outboundservices.MercadoPagoPaymentResult;
import com.tuxlogic.shiftiq.platform.billing.application.outboundservices.MercadoPagoPreferenceResult;
import com.tuxlogic.shiftiq.platform.billing.infrastructure.persistence.jpa.entities.PaymentIntentPersistenceEntity;
import com.tuxlogic.shiftiq.platform.billing.infrastructure.persistence.jpa.repositories.PaymentIntentJpaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

/**
 * Implementation of MercadoPagoPaymentCommandService orchestrating payment operations and Mercado Pago gateway interactions.
 */
@Service
public class MercadoPagoPaymentCommandServiceImpl implements MercadoPagoPaymentCommandService {

    private final MercadoPagoGateway mercadoPagoGateway;
    private final PaymentIntentJpaRepository paymentIntentJpaRepository;

    public MercadoPagoPaymentCommandServiceImpl(
            MercadoPagoGateway mercadoPagoGateway,
            PaymentIntentJpaRepository paymentIntentJpaRepository) {
        this.mercadoPagoGateway = mercadoPagoGateway;
        this.paymentIntentJpaRepository = paymentIntentJpaRepository;
    }

    @Override
    public Optional<MercadoPagoPreferenceResult> createPreference(BigDecimal amount, String currency, String title, String externalReference) {
        return mercadoPagoGateway.createPreference(amount, currency, title, externalReference);
    }

    @Override
    public Optional<MercadoPagoPaymentResult> getPaymentStatus(Long paymentId) {
        return mercadoPagoGateway.getPaymentStatus(paymentId);
    }

    @Override
    @Transactional
    public void registerPaymentIntent(UUID quoteId, String voucherType, String customerDocumentType,
                                      String customerDocumentNumber, String customerName, BigDecimal amount) {
        var existing = paymentIntentJpaRepository.findByQuoteId(quoteId);
        PaymentIntentPersistenceEntity entity = existing.orElseGet(() -> {
            var newEntity = new PaymentIntentPersistenceEntity();
            newEntity.setId(UUID.randomUUID());
            newEntity.setQuoteId(quoteId);
            newEntity.setCreatedAt(LocalDateTime.now());
            return newEntity;
        });

        entity.setVoucherType(voucherType != null ? voucherType.toUpperCase() : "RECEIPT");
        entity.setCustomerDocumentType(customerDocumentType != null ? customerDocumentType : "DNI");
        entity.setCustomerDocumentNumber(customerDocumentNumber != null ? customerDocumentNumber : "00000000");
        entity.setCustomerName(customerName != null ? customerName : "Cliente General");
        entity.setAmount(amount);
        entity.setCurrency("PEN");
        entity.setStatus("CREATED");
        entity.setUpdatedAt(LocalDateTime.now());

        paymentIntentJpaRepository.save(entity);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<PaymentIntentPersistenceEntity> getPaymentIntent(UUID quoteId) {
        return paymentIntentJpaRepository.findByQuoteId(quoteId);
    }
}
