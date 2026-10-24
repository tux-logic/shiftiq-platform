package com.tuxlogic.shiftiq.platform.core.domain.repositories;

import com.tuxlogic.shiftiq.platform.core.domain.model.entities.WorkshopSpecialtyRevision;
import com.tuxlogic.shiftiq.platform.core.domain.model.valueobjects.WorkshopId;
import com.tuxlogic.shiftiq.platform.core.domain.model.valueobjects.WorkshopSpecialtyId;

import java.util.List;

public interface WorkshopSpecialtyHistoryRepository {
    WorkshopSpecialtyRevision save(WorkshopSpecialtyRevision revision);
    List<WorkshopSpecialtyRevision> findByWorkshopIdAndSpecialtyId(WorkshopId workshopId, WorkshopSpecialtyId specialtyId);
    List<WorkshopSpecialtyRevision> findByWorkshopId(WorkshopId workshopId);
}
