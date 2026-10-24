package com.tuxlogic.shiftiq.platform.iam.domain.model.entities;

import lombok.Getter;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.UUID;

/**
 * Domain entity representing an email One-Time Passcode (OTP) verification record.
 */
@Getter
public class EmailOtpVerification {

    private UUID id;
    private String email;
    private String otpHash;
    private Instant createdAt;
    private Instant expiresAt;
    private int attempts;
    private int maxAttempts;
    private boolean isVerified;
    private boolean isConsumed;

    public EmailOtpVerification() {
    }

    public EmailOtpVerification(String email, String otpHash, long expirationMinutes, int maxAttempts) {
        this.id = UUID.randomUUID();
        this.email = email;
        this.otpHash = otpHash;
        this.createdAt = Instant.now();
        this.expiresAt = this.createdAt.plus(expirationMinutes, ChronoUnit.MINUTES);
        this.attempts = 0;
        this.maxAttempts = maxAttempts > 0 ? maxAttempts : 5;
        this.isVerified = false;
        this.isConsumed = false;
    }

    public EmailOtpVerification(
            UUID id,
            String email,
            String otpHash,
            Instant createdAt,
            Instant expiresAt,
            int attempts,
            int maxAttempts,
            boolean isVerified,
            boolean isConsumed
    ) {
        this.id = id;
        this.email = email;
        this.otpHash = otpHash;
        this.createdAt = createdAt;
        this.expiresAt = expiresAt;
        this.attempts = attempts;
        this.maxAttempts = maxAttempts;
        this.isVerified = isVerified;
        this.isConsumed = isConsumed;
    }

    public boolean isValid() {
        return !isConsumed && !isVerified && attempts < maxAttempts && Instant.now().isBefore(expiresAt);
    }

    public void incrementAttempts() {
        this.attempts++;
    }

    public void markVerified() {
        this.isVerified = true;
        this.isConsumed = true;
    }

    public void markConsumed() {
        this.isConsumed = true;
    }
}
