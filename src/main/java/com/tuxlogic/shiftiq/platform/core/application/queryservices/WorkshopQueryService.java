package com.tuxlogic.shiftiq.platform.core.application.queryservices;

import com.tuxlogic.shiftiq.platform.core.domain.model.aggregates.Workshop;
import com.tuxlogic.shiftiq.platform.core.domain.model.queries.GetAllWorkshopsByOwnerIdQuery;
import com.tuxlogic.shiftiq.platform.core.domain.model.queries.GetWorkshopByIdQuery;

import java.util.List;
import java.util.Optional;

public interface WorkshopQueryService {
    Optional<Workshop> handle(GetWorkshopByIdQuery query);
    List<Workshop> handle(GetAllWorkshopsByOwnerIdQuery query);
}
