package com.tuxlogic.shiftiq.platform.iam.domain.model.entities;

import lombok.Getter;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * PasswordRecoveryToken entity.
 * Represents a token generated for recovering a user's password.
 */
@Getter
public class PasswordRecoveryToken {

    private UUID id;
    private String tokenHash;
    private LocalDateTime createdAt;
    private LocalDateTime expiresAt;
    private boolean isUsed;
    private UUID userId;

    public PasswordRecoveryToken() {
    }

    public PasswordRecoveryToken(String tokenHash, UUID userId, long expirationMinutes) {
        this.id = UUID.randomUUID();
        this.tokenHash = tokenHash;
        this.userId = userId;
        this.createdAt = LocalDateTime.now();
        this.expiresAt = this.createdAt.plusMinutes(expirationMinutes);
        this.isUsed = false;
    }

    public PasswordRecoveryToken(UUID id, String tokenHash, UUID userId, LocalDateTime createdAt, LocalDateTime expiresAt, boolean isUsed) {
        this.id = id;
        this.tokenHash = tokenHash;
        this.userId = userId;
        this.createdAt = createdAt;
        this.expiresAt = expiresAt;
        this.isUsed = isUsed;
    }

    public boolean isValid() {
        return !isUsed && LocalDateTime.now().isBefore(expiresAt);
    }

    public void markAsUsed() {
        this.isUsed = true;
    }
}
