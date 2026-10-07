package com.tuxlogic.shiftiq.platform.core.application.queryservices;

import com.tuxlogic.shiftiq.platform.core.domain.model.aggregates.WorkshopSpecialty;
import com.tuxlogic.shiftiq.platform.core.domain.model.queries.GetWorkshopSpecialtiesByWorkshopIdQuery;
import com.tuxlogic.shiftiq.platform.core.domain.model.queries.GetWorkshopSpecialtyByIdQuery;

import java.util.List;
import java.util.Optional;

public interface WorkshopSpecialtyQueryService {
    List<WorkshopSpecialty> handle(GetWorkshopSpecialtiesByWorkshopIdQuery query);
    Optional<WorkshopSpecialty> handle(GetWorkshopSpecialtyByIdQuery query);
}
