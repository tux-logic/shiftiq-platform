package com.tuxlogic.shiftiq.platform.iam.infrastructure.persistence.jpa.repositories;

import com.tuxlogic.shiftiq.platform.iam.infrastructure.persistence.jpa.entities.WorkshopRolePersistenceEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface WorkshopRolePersistenceRepository extends JpaRepository<WorkshopRolePersistenceEntity, UUID> {

    @Query("SELECT r FROM WorkshopRolePersistenceEntity r WHERE (r.workshopId IS NULL OR r.workshopId = :workshopId) AND r.active = true ORDER BY r.system DESC, r.name ASC")
    List<WorkshopRolePersistenceEntity> findAvailableForWorkshop(@Param("workshopId") UUID workshopId);

    Optional<WorkshopRolePersistenceEntity> findByWorkshopIdAndNameIgnoreCase(UUID workshopId, String name);
}
