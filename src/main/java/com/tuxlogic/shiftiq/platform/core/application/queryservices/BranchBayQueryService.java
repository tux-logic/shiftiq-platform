package com.tuxlogic.shiftiq.platform.core.application.queryservices;

import com.tuxlogic.shiftiq.platform.core.domain.model.aggregates.BranchBay;
import com.tuxlogic.shiftiq.platform.core.domain.model.queries.GetBranchBayByIdQuery;
import com.tuxlogic.shiftiq.platform.core.domain.model.queries.GetBranchBaysByBranchIdQuery;

import java.util.List;
import java.util.Optional;

public interface BranchBayQueryService {
    List<BranchBay> handle(GetBranchBaysByBranchIdQuery query);
    Optional<BranchBay> handle(GetBranchBayByIdQuery query);
}