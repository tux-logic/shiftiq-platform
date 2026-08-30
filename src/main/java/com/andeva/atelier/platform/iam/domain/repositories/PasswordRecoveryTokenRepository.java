package com.andeva.atelier.platform.iam.domain.repositories;

import com.andeva.atelier.platform.iam.domain.model.entities.PasswordRecoveryToken;

import java.util.Optional;

/**
 * PasswordRecoveryTokenRepository interface.
 * Defines the contract for password recovery token persistence operations.
 */
public interface PasswordRecoveryTokenRepository {
    void save(PasswordRecoveryToken token);
    Optional<PasswordRecoveryToken> findByTokenHash(String tokenHash);
}
