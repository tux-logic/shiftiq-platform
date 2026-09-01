package com.tuxlogic.shiftiq.platform.billing.infrastructure.outbound.factos;

import java.math.BigDecimal;
import java.util.List;

public record FactosIssueInvoiceResponse(
        String series,
        String correlative,
        String cpeType,
        String issueDate,
        String issuerRuc,
        String acquirerDocument,
        String acquirerName,
        String status,
        BigDecimal totalTaxable,
        BigDecimal totalIgv,
        BigDecimal totalAmount,
        String currency,
        String pdfUrl,
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
