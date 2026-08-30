package com.andeva.atelier.platform.core.infrastructure.persistence.jpa.repositories;

import com.andeva.atelier.platform.core.infrastructure.persistence.jpa.entities.EmployeePersistenceEntity;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface EmployeePersistenceRepository extends JpaRepository<EmployeePersistenceEntity, UUID> {
    Optional<EmployeePersistenceEntity> findByUserId(UUID userId);
    boolean existsByUserId(UUID userId);
    Optional<EmployeePersistenceEntity> findByDocumentNumber(String documentNumber);
}
