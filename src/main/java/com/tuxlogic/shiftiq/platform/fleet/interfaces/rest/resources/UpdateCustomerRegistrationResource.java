package com.tuxlogic.shiftiq.platform.fleet.interfaces.rest.resources;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

public record UpdateCustomerRegistrationResource(
        @NotNull(message = "fleet.error.resource.status.required")
        @Pattern(regexp = "ACTIVE|INACTIVE", message = "Status must be ACTIVE or INACTIVE")
        String status
) {
}

