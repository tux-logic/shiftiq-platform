package com.tuxlogic.shiftiq.platform.billing.interfaces.rest.resources;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.util.UUID;

public record CreatePaymentIntentResource(
        @NotNull(message = "billing.error.payment.amountRequired")
        @DecimalMin(value = "0.01", message = "billing.error.payment.amountMin")
        BigDecimal amount,

        String currency,
        String description,

        UUID branchId
) {
    public CreatePaymentIntentResource(BigDecimal amount, String currency, String description) {
        this(amount, currency, description, null);
    }
}

