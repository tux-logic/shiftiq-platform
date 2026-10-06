package com.tuxlogic.shiftiq.platform.iam.domain.repositories;

import com.tuxlogic.shiftiq.platform.iam.domain.model.entities.RefreshToken;

import java.util.Optional;

/**
 * RefreshTokenRepository interface.
 * Defines the contract for refresh token persistence operations.
 */
public interface RefreshTokenRepository {
    void save(RefreshToken token);
    Optional<RefreshToken> findByTokenHash(String tokenHash);
}
