package com.tuxlogic.shiftiq.platform.billing.application.outboundservices;

import com.tuxlogic.shiftiq.platform.billing.domain.model.valueobjects.VoucherType;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

public interface FactosGateway {
    
    public record FactosItem(String code, String description, BigDecimal quantity, BigDecimal unitPrice) {}

    public record FactosInvoiceResult(String series, String correlative, String pdfUrl, BigDecimal totalAmount) {}

    Optional<FactosInvoiceResult> issueVoucher(
            String issuerRuc,
            VoucherType documentType,
            String customerDocumentType,
            String customerDocumentNumber,
            String customerName,
            List<FactosItem> items
    );
}
