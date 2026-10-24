package com.tuxlogic.shiftiq.platform.iam.application.internal.outboundservices.email;

/**
 * Result of an email verification check.
 *
 * @param valid     whether the email is valid and deliverable
 * @param status    the status reported by the verification service (e.g. valid, invalid, catch-all, do-not-mail, unknown)
 * @param subStatus the detailed sub-status if provided
 * @param message   optional diagnostic or error description
 */
public record EmailVerificationResult(
        boolean valid,
        String status,
        String subStatus,
        String message
) {
    public static EmailVerificationResult ofValid(String status, String subStatus) {
        return new EmailVerificationResult(true, status, subStatus, null);
    }

    public static EmailVerificationResult ofInvalid(String status, String subStatus, String message) {
        return new EmailVerificationResult(false, status, subStatus, message);
    }
}
