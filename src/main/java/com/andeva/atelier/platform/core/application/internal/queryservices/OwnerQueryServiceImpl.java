package com.andeva.atelier.platform.core.application.internal.queryservices;

import com.andeva.atelier.platform.core.application.queryservices.OwnerQueryService;
import com.andeva.atelier.platform.core.domain.model.aggregates.Owner;
import com.andeva.atelier.platform.core.domain.model.queries.GetOwnerByIdQuery;
import com.andeva.atelier.platform.core.domain.model.queries.GetOwnerByUserIdQuery;
import com.andeva.atelier.platform.core.domain.repositories.OwnerRepository;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class OwnerQueryServiceImpl implements OwnerQueryService {
    private final OwnerRepository ownerRepository;

    public OwnerQueryServiceImpl(OwnerRepository ownerRepository) {
        this.ownerRepository = ownerRepository;
    }

    @Override
    public Optional<Owner> handle(GetOwnerByIdQuery query) {
        return ownerRepository.findById(query.id());
    }

    @Override
    public Optional<Owner> handle(GetOwnerByUserIdQuery query) {
        return ownerRepository.findByUserId(query.userId());
    }
}
