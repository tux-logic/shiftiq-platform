package com.tuxlogic.shiftiq.platform.iam.domain.repositories;

import com.tuxlogic.shiftiq.platform.iam.domain.model.aggregates.WorkshopRole;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface WorkshopRoleRepository {
    WorkshopRole save(WorkshopRole role);
    Optional<WorkshopRole> findById(UUID id);
    List<WorkshopRole> findAvailableForWorkshop(UUID workshopId);
    Optional<WorkshopRole> findByWorkshopIdAndNameIgnoreCase(UUID workshopId, String name);
}
