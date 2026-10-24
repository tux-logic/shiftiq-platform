package com.tuxlogic.shiftiq.platform.shared.application.outboundservices.identity;

/**
 * Immutable representation of a natural person identity validation result (RENIEC / Migraciones).
 */
public record PersonIdentityResult(
        String documentType,
        String documentNumber,
        String firstName,
        String lastName,
        String fullName,
        String address,
        String department,
        String province,
        String district,
        String verificationDigit,
        boolean valid
) {
    public static PersonIdentityResult invalid(String documentType, String documentNumber) {
        return new PersonIdentityResult(
                documentType,
                documentNumber,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                false
        );
    }
}
