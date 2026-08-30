package com.tuxlogic.shiftiq.platform.core.interfaces.rest.resources;

public record UpdateEmployeeResource(
        String firstName,
        String lastName,
        String documentType,
        String documentNumber,
        String phone
) {
}
