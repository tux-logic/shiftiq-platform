package com.tuxlogic.shiftiq.platform.billing.application.internal.commandservices;

import com.tuxlogic.shiftiq.platform.billing.application.commandservices.MercadoPagoPaymentCommandService;
import com.tuxlogic.shiftiq.platform.billing.application.outboundservices.MercadoPagoGateway;
import com.tuxlogic.shiftiq.platform.billing.application.outboundservices.MercadoPagoPaymentResult;
import com.tuxlogic.shiftiq.platform.billing.application.outboundservices.MercadoPagoPreferenceResult;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Optional;

/**
 * Implementation of MercadoPagoPaymentCommandService orchestrating payment operations and Mercado Pago gateway interactions.
 */
@Service
public class MercadoPagoPaymentCommandServiceImpl implements MercadoPagoPaymentCommandService {

    private final MercadoPagoGateway mercadoPagoGateway;

    public MercadoPagoPaymentCommandServiceImpl(MercadoPagoGateway mercadoPagoGateway) {
        this.mercadoPagoGateway = mercadoPagoGateway;
    }

    @Override
    public Optional<MercadoPagoPreferenceResult> createPreference(BigDecimal amount, String currency, String title, String externalReference) {
        return mercadoPagoGateway.createPreference(amount, currency, title, externalReference);
    }

    @Override
    public Optional<MercadoPagoPaymentResult> getPaymentStatus(Long paymentId) {
        return mercadoPagoGateway.getPaymentStatus(paymentId);
    }
}

