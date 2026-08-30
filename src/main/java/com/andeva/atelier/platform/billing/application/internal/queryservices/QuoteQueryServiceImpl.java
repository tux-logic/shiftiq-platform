package com.andeva.atelier.platform.billing.application.internal.queryservices;

import com.andeva.atelier.platform.billing.application.queryservices.QuoteQueryService;
import com.andeva.atelier.platform.billing.domain.model.aggregates.Quote;
import com.andeva.atelier.platform.billing.domain.model.queries.GetQuoteByIdQuery;
import com.andeva.atelier.platform.billing.domain.repositories.QuoteRepository;
import org.springframework.stereotype.Service;

import java.util.Optional;

/**
 * Implementation of the QuoteQueryService interface.
 * Delegates query operations to the underlying Quote persistence repository.
 */
@Service
public class QuoteQueryServiceImpl implements QuoteQueryService {

    private final QuoteRepository quoteRepository;

    public QuoteQueryServiceImpl(QuoteRepository quoteRepository) {
        this.quoteRepository = quoteRepository;
    }

    /**
     * Executes the query by delegating the lookup to the repository.
     * 
     * @param query The query object containing the target quote ID.
     * @return An Optional containing the Quote if found, or empty if it does not exist.
     */
    @Override
    public Optional<Quote> handle(GetQuoteByIdQuery query) {
        return quoteRepository.findById(query.quoteId());
    }

    /**
     * Executes the query to find all quotes for a given branch.
     * 
     * @param query The query object containing the target branch ID.
     * @return A list of Quotes matching the branch ID.
     */
    @Override
    public java.util.List<Quote> handle(com.andeva.atelier.platform.billing.domain.model.queries.GetQuotesByBranchIdQuery query) {
        return quoteRepository.findAllByBranchId(query.branchId());
    }
}
