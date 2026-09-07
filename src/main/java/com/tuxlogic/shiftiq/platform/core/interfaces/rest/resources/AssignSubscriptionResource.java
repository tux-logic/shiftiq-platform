package com.tuxlogic.shiftiq.platform.core.interfaces.rest.resources;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.UUID;

public record AssignSubscriptionResource(
        @NotNull(message = "planId is required") UUID planId,
        @NotBlank(message = "billingCycle is required") String billingCycle,
        @NotBlank(message = "cardNumber is required") String cardNumber,
        @NotBlank(message = "cardHolderName is required") String cardHolderName,
        @NotBlank(message = "expirationDate is required") String expirationDate,
        @NotBlank(message = "cvv is required") String cvv
) {
}
