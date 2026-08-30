package com.tuxlogic.shiftiq.platform.operations.application.queryservices;

import com.tuxlogic.shiftiq.platform.operations.domain.model.aggregates.Service;
import com.tuxlogic.shiftiq.platform.operations.domain.model.queries.GetAllServicesByBranchIdQuery;
import com.tuxlogic.shiftiq.platform.operations.domain.model.queries.GetServiceByIdQuery;

import java.util.List;
import java.util.Optional;

public interface ServiceQueryService {
    Optional<Service> handle(GetServiceByIdQuery query);
    List<Service> handle(GetAllServicesByBranchIdQuery query);
}
