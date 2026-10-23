-- V15: Create email_otp_verifications table for OTP code storage and verification lifecycle

CREATE TABLE IF NOT EXISTS email_otp_verifications
(
    id           uuid         NOT NULL UNIQUE,
    email        varchar(255) NOT NULL,
    otp_hash     varchar(255) NOT NULL,
    created_at   timestamp    NOT NULL,
    expires_at   timestamp    NOT NULL,
    attempts     integer      NOT NULL DEFAULT 0,
    max_attempts integer      NOT NULL DEFAULT 5,
    is_verified  boolean      NOT NULL DEFAULT false,
    is_consumed  boolean      NOT NULL DEFAULT false,
    PRIMARY KEY (id)
);

CREATE INDEX IF NOT EXISTS idx_email_otp_verifications_email
    ON email_otp_verifications (email);
