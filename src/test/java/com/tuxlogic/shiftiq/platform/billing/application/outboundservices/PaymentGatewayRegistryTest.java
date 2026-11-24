package com.tuxlogic.shiftiq.platform.billing.application.outboundservices;

import com.tuxlogic.shiftiq.platform.billing.domain.model.valueobjects.PaymentProvider;
import com.tuxlogic.shiftiq.platform.billing.infrastructure.outbound.mercadopago.MercadoPagoGatewayStrategy;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PaymentGatewayRegistryTest {

    @Mock
    private MercadoPagoGateway mercadoPagoGateway;

    @Test
    void resolve_WhenMercadoPagoRegistered_ShouldDelegateToMercadoPagoStrategy() {
        var strategy = new MercadoPagoGatewayStrategy(mercadoPagoGateway);
        var registry = new PaymentGatewayRegistry(List.of(strategy));

        when(mercadoPagoGateway.createPreference(new BigDecimal("150.00"), "PEN", "Quote #1", "ref-1"))
                .thenReturn(Optional.of(new MercadoPagoPreferenceResult(
                        "pref-1", "https://mp/init", "https://mp/sandbox",
                        new BigDecimal("150.00"), "PEN", "ref-1"
                )));

        var resolved = registry.resolve(PaymentProvider.MERCADO_PAGO);
        var session = resolved.createCheckoutSession(new BigDecimal("150.00"), "PEN", "Quote #1", "ref-1");

        assertTrue(session.isPresent());
        assertEquals(PaymentProvider.MERCADO_PAGO, session.get().provider());
        assertEquals("pref-1", session.get().sessionId());
        assertEquals("https://mp/init", session.get().checkoutUrl());
    }

    @Test
    void resolve_WhenProviderNullOrUnregistered_ShouldThrowIllegalArgumentException() {
        var registry = new PaymentGatewayRegistry(List.of());
        assertThrows(IllegalArgumentException.class, () -> registry.resolve(null));
        assertThrows(IllegalArgumentException.class, () -> registry.resolve(PaymentProvider.MERCADO_PAGO));
    }
}
