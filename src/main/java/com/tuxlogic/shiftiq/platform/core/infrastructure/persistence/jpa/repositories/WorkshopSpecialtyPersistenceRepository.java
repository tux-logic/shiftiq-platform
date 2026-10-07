package com.tuxlogic.shiftiq.platform.core.infrastructure.persistence.jpa.repositories;

import com.tuxlogic.shiftiq.platform.core.infrastructure.persistence.jpa.entities.WorkshopSpecialtyPersistenceEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface WorkshopSpecialtyPersistenceRepository extends JpaRepository<WorkshopSpecialtyPersistenceEntity, UUID> {
    List<WorkshopSpecialtyPersistenceEntity> findAllByWorkshopId(UUID workshopId);
    List<WorkshopSpecialtyPersistenceEntity> findAllByWorkshopIdAndActiveTrue(UUID workshopId);
    Optional<WorkshopSpecialtyPersistenceEntity> findByWorkshopIdAndCode(UUID workshopId, String code);
    boolean existsByWorkshopIdAndCode(UUID workshopId, String code);
}
