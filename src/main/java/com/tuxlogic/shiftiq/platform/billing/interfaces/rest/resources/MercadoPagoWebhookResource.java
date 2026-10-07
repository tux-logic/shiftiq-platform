package com.tuxlogic.shiftiq.platform.billing.interfaces.rest.resources;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * Resource DTO representing incoming webhook payloads from Mercado Pago.
 * Supports both event notifications (v2) and legacy notifications.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record MercadoPagoWebhookResource(
        String action,
        String type,
        MercadoPagoWebhookData data,
        Long id
) {
    @JsonIgnoreProperties(ignoreUnknown = true)
    public record MercadoPagoWebhookData(String id) {}
}
