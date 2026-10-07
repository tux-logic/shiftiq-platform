package com.tuxlogic.shiftiq.platform.core.domain.repositories;

import com.tuxlogic.shiftiq.platform.core.domain.model.aggregates.WorkshopSpecialty;
import com.tuxlogic.shiftiq.platform.core.domain.model.valueobjects.WorkshopId;
import com.tuxlogic.shiftiq.platform.core.domain.model.valueobjects.WorkshopSpecialtyId;

import java.util.List;
import java.util.Optional;

public interface WorkshopSpecialtyRepository {
    WorkshopSpecialty save(WorkshopSpecialty specialty);
    Optional<WorkshopSpecialty> findById(WorkshopSpecialtyId id);
    List<WorkshopSpecialty> findAllByWorkshopId(WorkshopId workshopId);
    List<WorkshopSpecialty> findAllActiveByWorkshopId(WorkshopId workshopId);
    Optional<WorkshopSpecialty> findByWorkshopIdAndCode(WorkshopId workshopId, String code);
    boolean existsByWorkshopIdAndCode(WorkshopId workshopId, String code);
    void delete(WorkshopSpecialty specialty);
}
