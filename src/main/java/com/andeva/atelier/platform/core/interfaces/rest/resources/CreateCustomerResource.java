package com.andeva.atelier.platform.core.interfaces.rest.resources;

import java.util.UUID;

public record CreateCustomerResource(
        UUID userId,
        boolean isCorporate,
        String firstName,
        String lastName,
        String businessName,
        String documentType,
        String documentNumber,
        String phone
) {
}
