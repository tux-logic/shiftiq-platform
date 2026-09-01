package com.tuxlogic.shiftiq.platform.billing.infrastructure.outbound.factos;

import java.math.BigDecimal;
import java.util.List;

public record FactosIssueInvoiceRequest(
        String series,
        String correlative,
        String cpeType,
        String issueDate,
        String issuerRuc,
        String acquirerDocument,
        String acquirerName,
        String currency,
        List<Item> items
) {
    public record Item(
            String code,
            String description,
            BigDecimal quantity,
            BigDecimal unitPrice,
            String affectationType
    ) {}
}
