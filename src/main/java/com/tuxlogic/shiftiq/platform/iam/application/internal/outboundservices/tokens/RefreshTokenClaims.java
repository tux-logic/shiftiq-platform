package com.tuxlogic.shiftiq.platform.iam.application.internal.outboundservices.tokens;

import java.time.Instant;

/**
 * Trusted content of a refresh token after its signature and expiry were verified.
 *
 * @param username  the email of the token subject
 * @param expiresAt the instant the token stops being valid
 */
public record RefreshTokenClaims(String username, Instant expiresAt) {
}
