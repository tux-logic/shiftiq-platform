package com.tuxlogic.shiftiq.platform.billing.domain.model.valueobjects;

import com.tuxlogic.shiftiq.platform.billing.domain.model.commands.GenerateVoucherCommand;
import com.tuxlogic.shiftiq.platform.billing.domain.model.commands.ProcessCheckoutCommand;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class TaxDocumentValidatorTest {

    @Test
    @DisplayName("Invoice with valid 11-digit RUC passes validation")
    void invoiceWithValidRucPasses() {
        assertThatCode(() -> TaxDocumentValidator.validate(VoucherType.INVOICE, "RUC", "20601234567"))
                .doesNotThrowAnyException();

        UUID quoteId = UUID.randomUUID();
        assertThatCode(() -> new GenerateVoucherCommand(quoteId, VoucherType.INVOICE, "RUC", "20601234567", "Empresa SAC"))
                .doesNotThrowAnyException();
    }

    @Test
    @DisplayName("Invoice with non-RUC document type throws exception")
    void invoiceWithNonRucTypeThrows() {
        assertThatThrownBy(() -> TaxDocumentValidator.validate(VoucherType.INVOICE, "DNI", "20601234567"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("billing.error.invoice.ruc.documentType");
    }

    @Test
    @DisplayName("Invoice with RUC having less or more than 11 digits throws exception")
    void invoiceWithInvalidRucLengthThrows() {
        assertThatThrownBy(() -> TaxDocumentValidator.validate(VoucherType.INVOICE, "RUC", "12345678"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("billing.error.invoice.ruc.number");

        assertThatThrownBy(() -> TaxDocumentValidator.validate(VoucherType.INVOICE, "RUC", "2060123456789"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("billing.error.invoice.ruc.number");
    }

    @Test
    @DisplayName("Invoice with non-digit RUC throws exception")
    void invoiceWithNonDigitRucThrows() {
        assertThatThrownBy(() -> TaxDocumentValidator.validate(VoucherType.INVOICE, "RUC", "2060123456A"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("billing.error.invoice.ruc.number");
    }

    @Test
    @DisplayName("Receipt with valid 8-digit DNI passes validation")
    void receiptWithValidDniPasses() {
        assertThatCode(() -> TaxDocumentValidator.validate(VoucherType.RECEIPT, "DNI", "12345678"))
                .doesNotThrowAnyException();

        UUID quoteId = UUID.randomUUID();
        assertThatCode(() -> new ProcessCheckoutCommand(quoteId, VoucherType.RECEIPT, "DNI", "12345678", "Juan Perez", PaymentMethod.CASH))
                .doesNotThrowAnyException();
    }

    @Test
    @DisplayName("Receipt with DNI having invalid length throws exception")
    void receiptWithInvalidDniLengthThrows() {
        assertThatThrownBy(() -> TaxDocumentValidator.validate(VoucherType.RECEIPT, "DNI", "1234567"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("billing.error.receipt.dni.number");

        assertThatThrownBy(() -> TaxDocumentValidator.validate(VoucherType.RECEIPT, "DNI", "123456789"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("billing.error.receipt.dni.number");
    }

    @Test
    @DisplayName("Receipt with CE, PASSPORT, or OTHER passes validation")
    void receiptWithForeignDocumentsPasses() {
        assertThatCode(() -> TaxDocumentValidator.validate(VoucherType.RECEIPT, "CE", "001234567"))
                .doesNotThrowAnyException();

        assertThatCode(() -> TaxDocumentValidator.validate(VoucherType.RECEIPT, "PASSPORT", "P12345678"))
                .doesNotThrowAnyException();

        assertThatCode(() -> TaxDocumentValidator.validate(VoucherType.RECEIPT, "OTHER", "ABC-999"))
                .doesNotThrowAnyException();
    }

    @Test
    @DisplayName("Receipt with unsupported document type throws exception")
    void receiptWithUnsupportedTypeThrows() {
        assertThatThrownBy(() -> TaxDocumentValidator.validate(VoucherType.RECEIPT, "INVALID", "12345678"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("billing.error.receipt.invalidDocumentType");
    }
}
