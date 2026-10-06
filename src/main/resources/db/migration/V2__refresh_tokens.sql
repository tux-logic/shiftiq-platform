-- =================================================================================================
-- SHIFTIQ PLATFORM - V2: REFRESH TOKENS
-- =================================================================================================
--
-- Stores refresh tokens so sessions can be rotated and revoked server-side.
-- Only the SHA-256 hash of the raw token is persisted, therefore a leaked
-- database cannot be replayed against the API.
--
-- used_at / revoked_at are NULL while the token is live:
--   * used_at   is set once the token is exchanged (single use / rotation);
--   * revoked_at is set by the logout endpoint.
-- =================================================================================================

-- -------------------------------------------------------------------------------------------------
-- 1. TABLAS
-- -------------------------------------------------------------------------------------------------

CREATE TABLE refresh_tokens
(
    id         uuid         NOT NULL UNIQUE,
    user_id    uuid         NOT NULL,
    token_hash varchar(64)  NOT NULL UNIQUE,
    created_at timestamp    NOT NULL,
    expires_at timestamp    NOT NULL,
    used_at    timestamp   ,
    revoked_at timestamp   ,
    PRIMARY KEY (id)
);

-- -------------------------------------------------------------------------------------------------
-- 2. FK
-- -------------------------------------------------------------------------------------------------

ALTER TABLE refresh_tokens ADD CONSTRAINT FK_users_TO_refresh_tokens FOREIGN KEY (user_id) REFERENCES users (id);

-- -------------------------------------------------------------------------------------------------
-- 3. ÍNDICES
-- -------------------------------------------------------------------------------------------------

CREATE INDEX idx_refresh_tokens_user_id ON refresh_tokens (user_id);
CREATE INDEX idx_refresh_tokens_expires_at ON refresh_tokens (expires_at);
