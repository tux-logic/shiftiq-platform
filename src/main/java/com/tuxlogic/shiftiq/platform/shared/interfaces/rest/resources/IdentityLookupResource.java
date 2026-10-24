package com.tuxlogic.shiftiq.platform.shared.interfaces.rest.resources;

/**
 * REST resource representing the verified identity and auto-fill details from RENIEC, SUNAT, or Migraciones.
 */
public record IdentityLookupResource(
        String documentType,
        String documentNumber,
        String firstName,
        String lastName,
        String fullName,
        String businessName,
        String status,
        String condition,
        String address,
        String department,
        String province,
        String district,
        String ubigeo,
        boolean valid,
        Boolean active,
        Boolean habido
) {
}
