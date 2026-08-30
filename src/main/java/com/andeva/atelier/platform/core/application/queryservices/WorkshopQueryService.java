package com.andeva.atelier.platform.core.application.queryservices;

import com.andeva.atelier.platform.core.domain.model.aggregates.Workshop;
import com.andeva.atelier.platform.core.domain.model.queries.GetAllWorkshopsByOwnerIdQuery;
import com.andeva.atelier.platform.core.domain.model.queries.GetWorkshopByIdQuery;

import java.util.List;
import java.util.Optional;

public interface WorkshopQueryService {
    Optional<Workshop> handle(GetWorkshopByIdQuery query);
    List<Workshop> handle(GetAllWorkshopsByOwnerIdQuery query);
}
