package com.andeva.atelier.platform.iam.infrastructure.persistence.jpa.repositories;

import com.andeva.atelier.platform.iam.infrastructure.persistence.jpa.entities.PasswordRecoveryTokenPersistenceEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface PasswordRecoveryTokenPersistenceRepository extends JpaRepository<PasswordRecoveryTokenPersistenceEntity, UUID> {
    Optional<PasswordRecoveryTokenPersistenceEntity> findByTokenHash(String tokenHash);
}
