package com.tuxlogic.shiftiq.platform.iam.infrastructure.persistence.jpa.repositories;

import com.tuxlogic.shiftiq.platform.iam.infrastructure.persistence.jpa.entities.RefreshTokenPersistenceEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface RefreshTokenPersistenceRepository extends JpaRepository<RefreshTokenPersistenceEntity, UUID> {
    Optional<RefreshTokenPersistenceEntity> findByTokenHash(String tokenHash);
}
