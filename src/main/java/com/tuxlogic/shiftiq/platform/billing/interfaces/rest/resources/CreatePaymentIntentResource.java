package com.tuxlogic.shiftiq.platform.billing.interfaces.rest.resources;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;

public record CreatePaymentIntentResource(
        @NotNull(message = "billing.error.payment.amountRequired")
        @DecimalMin(value = "0.01", message = "billing.error.payment.amountMin")
        BigDecimal amount,

        String currency,
        String description
) {}
