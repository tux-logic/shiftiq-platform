package com.tuxlogic.shiftiq.platform.core.infrastructure.persistence.jpa.repositories;

import com.tuxlogic.shiftiq.platform.core.infrastructure.persistence.jpa.entities.CustomerPersistenceEntity;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface CustomerPersistenceRepository extends JpaRepository<CustomerPersistenceEntity, UUID> {
    Optional<CustomerPersistenceEntity> findByUserId(UUID userId);
    boolean existsByUserId(UUID userId);
    Optional<CustomerPersistenceEntity> findByDocumentNumber(String documentNumber);

    @Query(value = "SELECT 'CUSTOMER' FROM customers WHERE user_id = :userId AND deleted_at IS NULL " +
                   "UNION ALL " +
                   "SELECT 'OWNER' FROM owners WHERE user_id = :userId AND deleted_at IS NULL " +
                   "UNION ALL " +
                   "SELECT 'EMPLOYEE' FROM employees WHERE user_id = :userId AND deleted_at IS NULL", 
           nativeQuery = true)
    List<String> findProfileRolesByUserId(@Param("userId") UUID userId);
}
