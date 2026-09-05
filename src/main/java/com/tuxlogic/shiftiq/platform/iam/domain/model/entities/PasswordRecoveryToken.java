package com.tuxlogic.shiftiq.platform.iam.domain.model.entities;

import lombok.Getter;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.UUID;

/**
 * PasswordRecoveryToken entity.
 * Represents a token generated for recovering a user's password.
 */
@Getter
public class PasswordRecoveryToken {

    private UUID id;
    private String tokenHash;
    private Instant createdAt;
    private Instant expiresAt;
    private boolean isUsed;
    private UUID userId;

    public PasswordRecoveryToken() {
    }

    public PasswordRecoveryToken(String tokenHash, UUID userId, long expirationMinutes) {
        this.id = UUID.randomUUID();
        this.tokenHash = tokenHash;
        this.userId = userId;
        this.createdAt = Instant.now();
        this.expiresAt = this.createdAt.plus(expirationMinutes, ChronoUnit.MINUTES);
        this.isUsed = false;
    }

    public PasswordRecoveryToken(UUID id, String tokenHash, UUID userId, Instant createdAt, Instant expiresAt, boolean isUsed) {
        this.id = id;
        this.tokenHash = tokenHash;
        this.userId = userId;
        this.createdAt = createdAt;
        this.expiresAt = expiresAt;
        this.isUsed = isUsed;
    }

    public boolean isValid() {
        return !isUsed && Instant.now().isBefore(expiresAt);
    }

    public void markAsUsed() {
        this.isUsed = true;
    }
}
