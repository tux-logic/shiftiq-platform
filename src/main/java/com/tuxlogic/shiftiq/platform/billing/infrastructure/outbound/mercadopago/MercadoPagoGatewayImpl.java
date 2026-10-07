package com.tuxlogic.shiftiq.platform.billing.infrastructure.outbound.mercadopago;

import com.mercadopago.MercadoPagoConfig;
import com.mercadopago.client.payment.PaymentClient;
import com.mercadopago.client.preference.PreferenceClient;
import com.mercadopago.client.preference.PreferenceItemRequest;
import com.mercadopago.client.preference.PreferenceRequest;
import com.mercadopago.exceptions.MPApiException;
import com.mercadopago.exceptions.MPException;
import com.mercadopago.resources.payment.Payment;
import com.mercadopago.resources.preference.Preference;
import com.tuxlogic.shiftiq.platform.billing.application.outboundservices.MercadoPagoGateway;
import com.tuxlogic.shiftiq.platform.billing.application.outboundservices.MercadoPagoPaymentResult;
import com.tuxlogic.shiftiq.platform.billing.application.outboundservices.MercadoPagoPreferenceResult;
import com.tuxlogic.shiftiq.platform.billing.application.outboundservices.PaymentResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

/**
 * Adapter connecting to the official Mercado Pago API via the sdk-java SDK.
 */
@Service
public class MercadoPagoGatewayImpl implements MercadoPagoGateway {

    private static final Logger LOGGER = LoggerFactory.getLogger(MercadoPagoGatewayImpl.class);

    public MercadoPagoGatewayImpl(@Value("${mercadopago.access.token}") String accessToken) {
        if (accessToken != null && !accessToken.isBlank()) {
            MercadoPagoConfig.setAccessToken(accessToken);
        } else {
            LOGGER.warn("Mercado Pago access token is not configured.");
        }
    }

    @Override
    public Optional<PaymentResult> getPaymentStatusByExternalId(String externalPaymentId) {
        if (externalPaymentId == null) {
            return Optional.empty();
        }
        try {
            Long parsedPaymentId = Long.parseLong(externalPaymentId);
            return getPaymentStatus(parsedPaymentId).map(pay -> new PaymentResult(
                    pay.paymentId().toString(),
                    pay.externalReference(),
                    pay.amount(),
                    pay.currency(),
                    pay.status()
            ));
        } catch (NumberFormatException e) {
            LOGGER.error("Invalid Mercado Pago payment ID format: {}", externalPaymentId);
            return Optional.empty();
        }
    }

    @Override
    public Optional<MercadoPagoPreferenceResult> createPreference(BigDecimal amount, String currency, String title, String externalReference) {
        try {
            PreferenceClient client = new PreferenceClient();

            String curr = (currency != null && !currency.isBlank()) ? currency.toUpperCase() : "PEN";
            String itemTitle = (title != null && !title.isBlank()) ? title : "ShiftIQ Billing Payment";

            PreferenceItemRequest itemRequest = PreferenceItemRequest.builder()
                    .title(itemTitle)
                    .quantity(1)
                    .unitPrice(amount)
                    .currencyId(curr)
                    .build();

            PreferenceRequest preferenceRequest = PreferenceRequest.builder()
                    .items(List.of(itemRequest))
                    .externalReference(externalReference)
                    .build();

            Preference preference = client.create(preferenceRequest);

            LOGGER.info("Successfully created Mercado Pago preference ID '{}'", preference.getId());
            return Optional.of(new MercadoPagoPreferenceResult(
                    preference.getId(),
                    preference.getInitPoint(),
                    preference.getSandboxInitPoint(),
                    amount,
                    curr,
                    externalReference
            ));
        } catch (MPApiException e) {
            LOGGER.error("Mercado Pago API error creating preference: HTTP {}", e.getStatusCode());
            return Optional.empty();
        } catch (MPException e) {
            LOGGER.error("Mercado Pago SDK error creating preference: {}", e.getMessage());
            return Optional.empty();
        }
    }

    @Override
    public Optional<MercadoPagoPaymentResult> getPaymentStatus(Long paymentId) {
        if (paymentId == null) {
            return Optional.empty();
        }
        try {
            PaymentClient client = new PaymentClient();
            Payment payment = client.get(paymentId);

            LOGGER.info("Retrieved Mercado Pago payment ID '{}' with status '{}'", paymentId, payment.getStatus());
            return Optional.of(new MercadoPagoPaymentResult(
                    payment.getId(),
                    payment.getStatus(),
                    payment.getStatusDetail(),
                    payment.getTransactionAmount(),
                    payment.getCurrencyId(),
                    payment.getExternalReference()
            ));
        } catch (MPApiException e) {
            LOGGER.error("Mercado Pago API error retrieving payment '{}': HTTP {}", paymentId, e.getStatusCode());
            return Optional.empty();
        } catch (MPException e) {
            LOGGER.error("Mercado Pago SDK error retrieving payment '{}': {}", paymentId, e.getMessage());
            return Optional.empty();
        }
    }
}

