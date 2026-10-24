package com.tuxlogic.shiftiq.platform.billing.domain.model.valueobjects;

public enum VoucherType {
    RECEIPT,
    INVOICE;

    public static VoucherType fromString(String value) {
        if (value == null || value.isBlank()) {
            return RECEIPT;
        }
        String upper = value.trim().toUpperCase();
        return switch (upper) {
            case "FACTURA", "INVOICE" -> INVOICE;
            case "BOLETA", "RECEIPT" -> RECEIPT;
            default -> valueOf(upper);
        };
    }
}
