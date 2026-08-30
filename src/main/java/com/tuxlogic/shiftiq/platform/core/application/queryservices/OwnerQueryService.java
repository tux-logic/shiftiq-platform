package com.tuxlogic.shiftiq.platform.core.application.queryservices;

import com.tuxlogic.shiftiq.platform.core.domain.model.aggregates.Owner;
import com.tuxlogic.shiftiq.platform.core.domain.model.queries.GetOwnerByIdQuery;

import com.tuxlogic.shiftiq.platform.core.domain.model.queries.GetOwnerByUserIdQuery;

import java.util.Optional;

public interface OwnerQueryService {
    Optional<Owner> handle(GetOwnerByIdQuery query);
    Optional<Owner> handle(GetOwnerByUserIdQuery query);
}
