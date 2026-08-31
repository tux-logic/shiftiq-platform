package com.tuxlogic.shiftiq.platform.billing.infrastructure.persistence.jpa.adapters;

import com.tuxlogic.shiftiq.platform.billing.domain.model.aggregates.Quote;
import com.tuxlogic.shiftiq.platform.billing.domain.repositories.QuoteRepository;
import com.tuxlogic.shiftiq.platform.billing.infrastructure.persistence.jpa.assemblers.QuotePersistenceAssembler;
import com.tuxlogic.shiftiq.platform.billing.infrastructure.persistence.jpa.repositories.QuotePersistenceRepository;
import com.tuxlogic.shiftiq.platform.shared.domain.model.valueobjects.BranchId;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Implementation of the QuoteRepository domain interface using Spring Data JPA.
 * Responsible for translating between Domain Aggregates and JPA Persistence Entities.
 */
@Service
public class QuoteRepositoryImpl implements QuoteRepository {

    private final QuotePersistenceRepository persistenceRepository;

    public QuoteRepositoryImpl(QuotePersistenceRepository persistenceRepository) {
        this.persistenceRepository = persistenceRepository;
    }

    @Override
    public Quote save(Quote quote) {
        boolean exists = persistenceRepository.existsById(quote.getId());
        
        com.tuxlogic.shiftiq.platform.billing.infrastructure.persistence.jpa.entities.QuotePersistenceEntity entity;
        if (exists) {
            entity = persistenceRepository.findById(quote.getId()).orElseThrow();
        } else {
            entity = new com.tuxlogic.shiftiq.platform.billing.infrastructure.persistence.jpa.entities.QuotePersistenceEntity();
        }
        
        QuotePersistenceAssembler.updateEntityFromAggregate(entity, quote);
        
        var savedEntity = persistenceRepository.save(entity);
        return QuotePersistenceAssembler.toAggregate(savedEntity);
    }

    @Override
    public Optional<Quote> findById(UUID id) {
        return persistenceRepository.findById(id)
                .map(QuotePersistenceAssembler::toAggregate);
    }

    @Override
    public List<Quote> findAllByBranchId(BranchId branchId) {
        return persistenceRepository.findAllByBranchId(branchId)
                .stream()
                .map(QuotePersistenceAssembler::toAggregate)
                .collect(Collectors.toList());
    }

    @Override
    public boolean existsByWorkOrderId(UUID workOrderId) {
        return persistenceRepository.existsByWorkOrderId(workOrderId);
    }
}
