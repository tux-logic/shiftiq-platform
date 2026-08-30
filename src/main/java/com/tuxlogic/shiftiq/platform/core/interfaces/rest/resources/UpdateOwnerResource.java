package com.tuxlogic.shiftiq.platform.core.interfaces.rest.resources;

public record UpdateOwnerResource(
        String firstName,
        String lastName,
        String documentType,
        String documentNumber,
        String phone
) {
}
