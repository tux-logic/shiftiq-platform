package com.andeva.atelier.platform.core.interfaces.rest.resources;

import java.util.UUID;

public record CreateEmployeeResource(
        UUID userId,
        String firstName,
        String lastName,
        String documentType,
        String documentNumber,
        String phone
) {
}
