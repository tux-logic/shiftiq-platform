package com.andeva.atelier.platform.billing.application.internal.commandservices;

import com.andeva.atelier.platform.billing.domain.model.valueobjects.QuoteCommandFailure;
import com.andeva.atelier.platform.billing.application.commandservices.QuoteCommandService;
import com.andeva.atelier.platform.billing.domain.model.aggregates.Quote;
import com.andeva.atelier.platform.billing.domain.model.commands.CreateQuoteCommand;
import com.andeva.atelier.platform.billing.domain.repositories.QuoteRepository;
import com.andeva.atelier.platform.operations.application.queryservices.WorkOrderQueryService;
import com.andeva.atelier.platform.operations.domain.model.queries.GetWorkOrderByIdQuery;
import com.andeva.atelier.platform.operations.domain.model.valueobjects.WorkOrderId;
import com.andeva.atelier.platform.shared.application.result.Result;
import org.springframework.stereotype.Service;

/**
 * Implementation of the QuoteCommandService interface.
 * Handles the business use cases for Quote operations, acting as an orchestrator 
 * between the domain aggregates, query services from other contexts (e.g., Operations), 
 * and persistence repositories.
 */
@Service
public class QuoteCommandServiceImpl implements QuoteCommandService {

    private final QuoteRepository quoteRepository;
    private final WorkOrderQueryService workOrderQueryService;

    public QuoteCommandServiceImpl(QuoteRepository quoteRepository, WorkOrderQueryService workOrderQueryService) {
        this.quoteRepository = quoteRepository;
        this.workOrderQueryService = workOrderQueryService;
    }

    @Override
    public Result<Quote, QuoteCommandFailure> handle(CreateQuoteCommand command) {
        var query = new GetWorkOrderByIdQuery(new WorkOrderId(command.workOrderId()));
        var workOrderOpt = workOrderQueryService.handle(query);

        if (workOrderOpt.isEmpty()) {
            return Result.failure(QuoteCommandFailure.WORK_ORDER_NOT_FOUND);
        }

        var workOrder = workOrderOpt.get();
        
        try {
            var quote = new Quote(command, workOrder.getTotalAmount());
            var savedQuote = quoteRepository.save(quote);
            return Result.success(savedQuote);
        } catch (IllegalArgumentException e) {
            return Result.failure(QuoteCommandFailure.INVALID_QUOTE_DATA);
        }
    }

    @Override
    public Result<Quote, QuoteCommandFailure> handle(com.andeva.atelier.platform.billing.domain.model.commands.UpdateQuoteDiscountCommand command) {
        var quoteOpt = quoteRepository.findById(command.quoteId());
        
        if (quoteOpt.isEmpty()) {
            return Result.failure(QuoteCommandFailure.QUOTE_NOT_FOUND);
        }

        var quote = quoteOpt.get();

        try {
            quote.updateDiscount(command.discountPercentage());
            var savedQuote = quoteRepository.save(quote);
            return Result.success(savedQuote);
        } catch (IllegalStateException e) {
            return Result.failure(QuoteCommandFailure.INVALID_QUOTE_STATE);
        } catch (IllegalArgumentException e) {
            return Result.failure(QuoteCommandFailure.INVALID_QUOTE_DATA);
        }
    }

    @Override
    public Result<Quote, QuoteCommandFailure> handle(com.andeva.atelier.platform.billing.domain.model.commands.ApproveQuoteCommand command) {
        var quoteOpt = quoteRepository.findById(command.quoteId());
        
        if (quoteOpt.isEmpty()) {
            return Result.failure(QuoteCommandFailure.QUOTE_NOT_FOUND);
        }

        var quote = quoteOpt.get();

        try {
            quote.approve();
            var savedQuote = quoteRepository.save(quote);
            return Result.success(savedQuote);
        } catch (IllegalStateException e) {
            return Result.failure(QuoteCommandFailure.INVALID_QUOTE_STATE);
        }
    }

    @Override
    public Result<Quote, QuoteCommandFailure> handle(com.andeva.atelier.platform.billing.domain.model.commands.CancelQuoteCommand command) {
        var quoteOpt = quoteRepository.findById(command.quoteId());
        
        if (quoteOpt.isEmpty()) {
            return Result.failure(QuoteCommandFailure.QUOTE_NOT_FOUND);
        }

        var quote = quoteOpt.get();

        try {
            quote.cancel();
            var savedQuote = quoteRepository.save(quote);
            return Result.success(savedQuote);
        } catch (IllegalStateException e) {
            return Result.failure(QuoteCommandFailure.INVALID_QUOTE_STATE);
        }
    }
}
