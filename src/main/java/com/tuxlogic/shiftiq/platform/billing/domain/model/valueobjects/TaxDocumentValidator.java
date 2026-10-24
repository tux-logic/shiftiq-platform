package com.tuxlogic.shiftiq.platform.billing.domain.model.valueobjects;

/**
 * Domain utility for validating customer tax documents according to Peruvian electronic billing standards.
 */
public final class TaxDocumentValidator {

    private TaxDocumentValidator() {}

    /**
     * Validates that the customer document type and number match the required format for the given voucher type.
     *
     * @param type the voucher type (INVOICE or RECEIPT)
     * @param customerDocumentType the document type code (RUC, DNI, CE, PASSPORT, OTHER)
     * @param customerDocumentNumber the document number
     * @throws IllegalArgumentException if any validation rule is violated
     */
    public static void validate(VoucherType type, String customerDocumentType, String customerDocumentNumber) {
        if (type == null) {
            throw new IllegalArgumentException("billing.error.command.voucherTypeRequired");
        }
        if (customerDocumentType == null || customerDocumentType.isBlank()) {
            throw new IllegalArgumentException("billing.error.command.customerDocumentTypeRequired");
        }
        if (customerDocumentNumber == null || customerDocumentNumber.isBlank()) {
            throw new IllegalArgumentException("billing.error.command.customerDocumentNumberRequired");
        }

        String docType = customerDocumentType.trim().toUpperCase();
        String docNumber = customerDocumentNumber.trim();

        if (type == VoucherType.INVOICE) {
            if (!"RUC".equals(docType)) {
                throw new IllegalArgumentException("billing.error.invoice.ruc.documentType");
            }
            if (!docNumber.matches("^\\d{11}$")) {
                throw new IllegalArgumentException("billing.error.invoice.ruc.number");
            }
        } else if (type == VoucherType.RECEIPT) {
            if ("DNI".equals(docType)) {
                if (!docNumber.matches("^\\d{8}$")) {
                    throw new IllegalArgumentException("billing.error.receipt.dni.number");
                }
            } else if (!"CE".equals(docType) && !"PASSPORT".equals(docType) && !"OTHER".equals(docType)) {
                throw new IllegalArgumentException("billing.error.receipt.invalidDocumentType");
            }
        }
    }
}
