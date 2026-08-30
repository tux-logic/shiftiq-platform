package com.andeva.atelier.platform.billing.application.internal.commandservices;

import com.andeva.atelier.platform.billing.domain.model.valueobjects.VoucherCommandFailure;
import com.andeva.atelier.platform.billing.application.commandservices.VoucherCommandService;
import com.andeva.atelier.platform.billing.application.outboundservices.FacthubGateway;
import com.andeva.atelier.platform.billing.domain.model.aggregates.Voucher;
import com.andeva.atelier.platform.billing.domain.model.commands.AddPaymentCommand;
import com.andeva.atelier.platform.billing.domain.model.commands.GenerateVoucherCommand;
import com.andeva.atelier.platform.billing.domain.model.valueobjects.QuoteStatus;
import com.andeva.atelier.platform.billing.domain.repositories.QuoteRepository;
import com.andeva.atelier.platform.billing.domain.repositories.VoucherRepository;
import com.andeva.atelier.platform.core.application.queryservices.BranchQueryService;
import com.andeva.atelier.platform.core.application.queryservices.WorkshopQueryService;
import com.andeva.atelier.platform.core.domain.model.queries.GetBranchByIdQuery;
import com.andeva.atelier.platform.core.domain.model.queries.GetWorkshopByIdQuery;
import com.andeva.atelier.platform.shared.domain.model.valueobjects.BranchId;
import com.andeva.atelier.platform.shared.application.result.Result;
import com.andeva.atelier.platform.operations.application.queryservices.WorkOrderQueryService;
import com.andeva.atelier.platform.operations.domain.model.queries.GetWorkOrderByIdQuery;
import com.andeva.atelier.platform.operations.domain.model.valueobjects.WorkOrderId;
import com.andeva.atelier.platform.inventory.application.queryservices.ProductQueryService;
import com.andeva.atelier.platform.inventory.domain.model.queries.GetProductByIdQuery;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

/**
 * Implementation of the VoucherCommandService interface.
 * Orchestrates the business logic for creating and paying invoices/receipts.
 * Integrates with the Operations bounded context to validate branch data, 
 * and with the Facthub external service to emit documents to the tax authority.
 */
@Service
public class VoucherCommandServiceImpl implements VoucherCommandService {

    private final VoucherRepository voucherRepository;
    private final QuoteRepository quoteRepository;
    private final BranchQueryService branchQueryService;
    private final WorkshopQueryService workshopQueryService;
    private final FacthubGateway facthubGateway;
    private final WorkOrderQueryService workOrderQueryService;
    private final ProductQueryService productQueryService;

    public VoucherCommandServiceImpl(
            VoucherRepository voucherRepository,
            QuoteRepository quoteRepository,
            BranchQueryService branchQueryService,
            WorkshopQueryService workshopQueryService,
            FacthubGateway facthubGateway,
            WorkOrderQueryService workOrderQueryService,
            ProductQueryService productQueryService) {
        this.voucherRepository = voucherRepository;
        this.quoteRepository = quoteRepository;
        this.branchQueryService = branchQueryService;
        this.workshopQueryService = workshopQueryService;
        this.facthubGateway = facthubGateway;
        this.workOrderQueryService = workOrderQueryService;
        this.productQueryService = productQueryService;
    }

    @Override
    @Transactional
    public Result<Voucher, VoucherCommandFailure> handle(GenerateVoucherCommand command) {
        // 1. Get and validate Quote
        var quoteOpt = quoteRepository.findById(command.quoteId());
        if (quoteOpt.isEmpty()) {
            return Result.failure(VoucherCommandFailure.QUOTE_NOT_FOUND);
        }
        var quote = quoteOpt.get();
        if (quote.getStatus() != QuoteStatus.APPROVED) {
            return Result.failure(VoucherCommandFailure.QUOTE_NOT_APPROVED);
        }

        // 2. Query Core context for Issuer RUC (Tax ID)
        var coreBranchId = new BranchId(quote.getBranchId().value());
        var branchOpt = branchQueryService.handle(new GetBranchByIdQuery(coreBranchId));
        if (branchOpt.isEmpty()) {
            return Result.failure(VoucherCommandFailure.ISSUER_NOT_FOUND);
        }
        
        var workshopOpt = workshopQueryService.handle(new GetWorkshopByIdQuery(branchOpt.get().getWorkshopId()));
        if (workshopOpt.isEmpty()) {
            return Result.failure(VoucherCommandFailure.ISSUER_NOT_FOUND);
        }
        String issuerRuc = workshopOpt.get().getTaxId().value();

        // 3. Issue Voucher via Facthub
        var externalInvoiceIdOpt = facthubGateway.issueVoucher(
                issuerRuc,
                command.type(),
                command.customerDocumentType(),
                command.customerDocumentNumber(),
                command.customerName(),
                getDetailedBillingItems(quote)
        );

        if (externalInvoiceIdOpt.isEmpty()) {
            return Result.failure(VoucherCommandFailure.FACTHUB_ISSUANCE_FAILED);
        }

        // 4. Create and save Voucher Aggregate
        try {
            var voucher = new Voucher(
                    command.quoteId(),
                    command.type(),
                    command.customerDocumentType(),
                    command.customerDocumentNumber(),
                    command.customerName(),
                    quote.getTotalAmount(),
                    externalInvoiceIdOpt.get()
            );

            var savedVoucher = voucherRepository.save(voucher);
            return Result.success(savedVoucher);
        } catch (IllegalArgumentException e) {
            return Result.failure(VoucherCommandFailure.INVALID_VOUCHER_DATA);
        }
    }

    @Override
    @Transactional
    public Result<Voucher, VoucherCommandFailure> handle(AddPaymentCommand command) {
        var voucherOpt = voucherRepository.findById(command.voucherId());
        if (voucherOpt.isEmpty()) {
            return Result.failure(VoucherCommandFailure.VOUCHER_NOT_FOUND);
        }

        var voucher = voucherOpt.get();
        var quoteOpt = quoteRepository.findById(voucher.getQuoteId());
        if (quoteOpt.isEmpty()) {
            return Result.failure(VoucherCommandFailure.QUOTE_NOT_FOUND);
        }
        var branchId = quoteOpt.get().getBranchId().value();

        try {
            voucher.addPayment(command.amount(), command.method(), branchId);
            var savedVoucher = voucherRepository.save(voucher);
            return Result.success(savedVoucher);
        } catch (IllegalStateException e) {
            if (e.getMessage().contains("already paid")) {
                return Result.failure(VoucherCommandFailure.VOUCHER_ALREADY_PAID);
            }
            if (e.getMessage().contains("canceled")) {
                return Result.failure(VoucherCommandFailure.VOUCHER_CANCELED);
            }
            if (e.getMessage().contains("exceeds")) {
                return Result.failure(VoucherCommandFailure.PAYMENT_EXCEEDS_TOTAL_DEBT);
            }
            return Result.failure(VoucherCommandFailure.INVALID_VOUCHER_DATA);
        } catch (IllegalArgumentException e) {
            return Result.failure(VoucherCommandFailure.INVALID_VOUCHER_DATA);
        }
    }

    @Override
    public Result<Voucher, VoucherCommandFailure> handle(com.andeva.atelier.platform.billing.domain.model.commands.RemovePaymentCommand command) {
        var voucherOpt = voucherRepository.findById(command.voucherId());
        
        if (voucherOpt.isEmpty()) {
            return Result.failure(VoucherCommandFailure.VOUCHER_NOT_FOUND);
        }

        var voucher = voucherOpt.get();

        try {
            voucher.removePayment(command.paymentId());
            var savedVoucher = voucherRepository.save(voucher);
            return Result.success(savedVoucher);
        } catch (IllegalArgumentException e) {
            return Result.failure(VoucherCommandFailure.PAYMENT_NOT_FOUND);
        } catch (IllegalStateException e) {
            return Result.failure(VoucherCommandFailure.VOUCHER_CANCELED);
        }
    }

    @Override
    @Transactional
    public Result<Voucher, VoucherCommandFailure> handle(com.andeva.atelier.platform.billing.domain.model.commands.ProcessCheckoutCommand command) {
        // 1. Get and validate Quote
        var quoteOpt = quoteRepository.findById(command.quoteId());
        if (quoteOpt.isEmpty()) {
            return Result.failure(VoucherCommandFailure.QUOTE_NOT_FOUND);
        }
        var quote = quoteOpt.get();
        if (quote.getStatus() != QuoteStatus.APPROVED) {
            return Result.failure(VoucherCommandFailure.QUOTE_NOT_APPROVED);
        }

        // 2. Query Core context for Issuer RUC (Tax ID)
        var coreBranchId = new BranchId(quote.getBranchId().value());
        var branchOpt = branchQueryService.handle(new GetBranchByIdQuery(coreBranchId));
        if (branchOpt.isEmpty()) {
            return Result.failure(VoucherCommandFailure.ISSUER_NOT_FOUND);
        }
        
        var workshopOpt = workshopQueryService.handle(new GetWorkshopByIdQuery(branchOpt.get().getWorkshopId()));
        if (workshopOpt.isEmpty()) {
            return Result.failure(VoucherCommandFailure.ISSUER_NOT_FOUND);
        }
        String issuerRuc = workshopOpt.get().getTaxId().value();

        // 3. Issue Voucher via Facthub
        var externalInvoiceIdOpt = facthubGateway.issueVoucher(
                issuerRuc,
                command.type(),
                command.customerDocumentType(),
                command.customerDocumentNumber(),
                command.customerName(),
                getDetailedBillingItems(quote)
        );

        if (externalInvoiceIdOpt.isEmpty()) {
            return Result.failure(VoucherCommandFailure.FACTHUB_ISSUANCE_FAILED);
        }

        // 4. Create Voucher Aggregate
        try {
            var voucher = new Voucher(
                    command.quoteId(),
                    command.type(),
                    command.customerDocumentType(),
                    command.customerDocumentNumber(),
                    command.customerName(),
                    quote.getTotalAmount(),
                    externalInvoiceIdOpt.get()
            );

            // 5. Add full payment to the Voucher
            voucher.addPayment(quote.getTotalAmount(), command.method(), quote.getBranchId().value());

            // 6. Save the fully paid Voucher
            var savedVoucher = voucherRepository.save(voucher);
            return Result.success(savedVoucher);
        } catch (IllegalArgumentException | IllegalStateException e) {
            return Result.failure(VoucherCommandFailure.INVALID_VOUCHER_DATA);
        }
    }

    private List<FacthubGateway.FacthubItem> getDetailedBillingItems(com.andeva.atelier.platform.billing.domain.model.aggregates.Quote quote) {
        List<FacthubGateway.FacthubItem> items = new java.util.ArrayList<>();
        
        var workOrderOpt = workOrderQueryService.handle(new GetWorkOrderByIdQuery(new WorkOrderId(quote.getWorkOrderId())));
        if (workOrderOpt.isPresent()) {
            var workOrder = workOrderOpt.get();
            if (workOrder.getTasks() != null) {
                for (var task : workOrder.getTasks()) {
                    // Calculate labor price (task total price - sum of its products)
                    com.andeva.atelier.platform.shared.domain.model.valueobjects.Money laborPrice = task.getPrice();
                    if (task.getProducts() != null) {
                        for (var productAssoc : task.getProducts()) {
                            if (!productAssoc.isDeleted()) {
                                laborPrice = laborPrice.minus(productAssoc.getTotalAmount());
                            }
                        }
                    }
                    
                    // Add the task itself as labor item
                    items.add(new FacthubGateway.FacthubItem(
                            task.getDescription().value(),
                            1,
                            laborPrice.amount()
                    ));
                    
                    // Add each product
                    if (task.getProducts() != null) {
                        for (var productAssoc : task.getProducts()) {
                            if (!productAssoc.isDeleted()) {
                                String productName = "Producto " + productAssoc.getProductId().value();
                                var productOpt = productQueryService.handle(new GetProductByIdQuery(productAssoc.getProductId().value()));
                                if (productOpt.isPresent()) {
                                    productName = productOpt.get().getName().name();
                                }
                                
                                items.add(new FacthubGateway.FacthubItem(
                                        productName,
                                        productAssoc.getQuantity().value(),
                                        productAssoc.getUnitPrice().amount()
                                ));
                            }
                        }
                    }
                }
            }
        }
        
        // Fallback to summary item if no items could be resolved
        if (items.isEmpty()) {
            items.add(new FacthubGateway.FacthubItem(
                    "Servicios de taller automotriz según orden " + quote.getWorkOrderId(),
                    1,
                    quote.getTotalAmount().amount()
            ));
        }
        
        return items;
    }
}
