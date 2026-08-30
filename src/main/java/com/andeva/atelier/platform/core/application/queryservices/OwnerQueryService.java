package com.andeva.atelier.platform.core.application.queryservices;

import com.andeva.atelier.platform.core.domain.model.aggregates.Owner;
import com.andeva.atelier.platform.core.domain.model.queries.GetOwnerByIdQuery;

import com.andeva.atelier.platform.core.domain.model.queries.GetOwnerByUserIdQuery;

import java.util.Optional;

public interface OwnerQueryService {
    Optional<Owner> handle(GetOwnerByIdQuery query);
    Optional<Owner> handle(GetOwnerByUserIdQuery query);
}
