package com.andeva.atelier.platform.core.application.queryservices;

import com.andeva.atelier.platform.core.domain.model.aggregates.Branch;
import com.andeva.atelier.platform.core.domain.model.queries.GetAllBranchesByWorkshopIdQuery;
import com.andeva.atelier.platform.core.domain.model.queries.GetBranchByIdQuery;

import java.util.List;
import java.util.Optional;

public interface BranchQueryService {
    Optional<Branch> handle(GetBranchByIdQuery query);
    List<Branch> handle(GetAllBranchesByWorkshopIdQuery query);
}
