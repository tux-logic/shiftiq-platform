package com.tuxlogic.shiftiq.platform.iam.application.internal.outboundservices.email;

/**
 * Port for verifying email deliverability and authenticity against an external service.
 */
public interface EmailVerificationService {

    /**
     * Verifies whether the provided email address is valid, active, and deliverable.
     *
     * @param email the email address to verify
     * @return the result of the verification
     */
    EmailVerificationResult verifyEmail(String email);
}
