package com.tuxlogic.shiftiq.platform.shared.application.outboundservices.identity;

/**
 * Immutable representation of a legal entity fiscal validation result (SUNAT).
 */
public record CompanyIdentityResult(
        String ruc,
        String businessName,
        String status,
        String condition,
        String address,
        String department,
        String province,
        String district,
        String ubigeo,
        boolean active,
        boolean habido,
        boolean valid
) {
    public static CompanyIdentityResult invalid(String ruc) {
        return new CompanyIdentityResult(
                ruc,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                false,
                false,
                false
        );
    }
}
