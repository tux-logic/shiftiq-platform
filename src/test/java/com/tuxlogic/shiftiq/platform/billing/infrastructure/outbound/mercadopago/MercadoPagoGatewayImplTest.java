package com.tuxlogic.shiftiq.platform.billing.infrastructure.outbound.mercadopago;

import com.tuxlogic.shiftiq.platform.billing.application.outboundservices.MercadoPagoPaymentResult;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class MercadoPagoGatewayImplTest {

    private MercadoPagoGatewayImpl gateway;

    @BeforeEach
    void setUp() {
        gateway = new MercadoPagoGatewayImpl("TEST-dummy-access-token-for-unit-tests");
    }


    @Test
    void getPaymentStatus_WhenPaymentIdNull_ShouldReturnEmpty() {
        Optional<MercadoPagoPaymentResult> result = gateway.getPaymentStatus((Long) null);
        assertTrue(result.isEmpty());
    }

    @Test
    void getPaymentStatusByExternalId_WhenInvalidFormat_ShouldReturnEmpty() {
        var result = gateway.getPaymentStatusByExternalId("invalid_numeric_id");
        assertTrue(result.isEmpty());
    }

    @Test
    void getPaymentStatusByExternalId_WhenNull_ShouldReturnEmpty() {
        var result = gateway.getPaymentStatusByExternalId(null);
        assertTrue(result.isEmpty());
    }
}
