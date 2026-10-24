package com.tuxlogic.shiftiq.platform.core.infrastructure.persistence.jpa.repositories;

import com.tuxlogic.shiftiq.platform.core.infrastructure.persistence.jpa.entities.WorkshopSpecialtyHistoryPersistenceEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface WorkshopSpecialtyHistoryPersistenceRepository extends JpaRepository<WorkshopSpecialtyHistoryPersistenceEntity, UUID> {
    List<WorkshopSpecialtyHistoryPersistenceEntity> findByWorkshopIdAndSpecialtyIdOrderByRecordedAtDesc(UUID workshopId, UUID specialtyId);
    List<WorkshopSpecialtyHistoryPersistenceEntity> findByWorkshopIdOrderByRecordedAtDesc(UUID workshopId);
}
