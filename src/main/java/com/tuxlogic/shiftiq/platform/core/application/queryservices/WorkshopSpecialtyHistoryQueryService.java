package com.tuxlogic.shiftiq.platform.core.application.queryservices;

import com.tuxlogic.shiftiq.platform.core.domain.model.entities.WorkshopSpecialtyRevision;
import com.tuxlogic.shiftiq.platform.core.domain.model.queries.GetWorkshopSpecialtyHistoryQuery;

import java.util.List;

public interface WorkshopSpecialtyHistoryQueryService {
    List<WorkshopSpecialtyRevision> handle(GetWorkshopSpecialtyHistoryQuery query);
}
