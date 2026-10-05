package com.tuxlogic.shiftiq.platform.iam.domain.model.entities;

import lombok.Getter;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.UUID;

/**
 * Refresh token issued together with an access token.
 *
 * <p>The raw token never reaches the database: only its SHA-256 hash is stored,
 * so a leaked database cannot be replayed against the API. A token is single
 * use: it is marked as used the moment it is exchanged for a new session, which
 * gives rotation and makes replay of a stolen token detectable.</p>
 */
@Getter
public class RefreshToken {

    private UUID id;
    private UUID userId;
    private String tokenHash;
    private Instant createdAt;
    private Instant expiresAt;
    private Instant usedAt;
    private Instant revokedAt;

    public RefreshToken() {
    }

    public RefreshToken(String tokenHash, UUID userId, long expirationDays) {
        this.id = UUID.randomUUID();
        this.tokenHash = tokenHash;
        this.userId = userId;
        this.createdAt = Instant.now();
        this.expiresAt = this.createdAt.plus(expirationDays, ChronoUnit.DAYS);
    }

    public RefreshToken(UUID id, UUID userId, String tokenHash, Instant createdAt, Instant expiresAt,
                        Instant usedAt, Instant revokedAt) {
        this.id = id;
        this.userId = userId;
        this.tokenHash = tokenHash;
        this.createdAt = createdAt;
        this.expiresAt = expiresAt;
        this.usedAt = usedAt;
        this.revokedAt = revokedAt;
    }

    /**
     * A token can be exchanged only while it has not been consumed, has not
     * been revoked and has not expired.
     */
    public boolean isValid() {
        return usedAt == null && revokedAt == null && Instant.now().isBefore(expiresAt);
    }

    /**
     * Consumes the token as part of a rotation.
     */
    public void markAsUsed() {
        this.usedAt = Instant.now();
    }

    /**
     * Revokes the token so it can never be exchanged again.
     */
    public void revoke() {
        this.revokedAt = Instant.now();
    }
}
