package com.tuxlogic.shiftiq.platform.billing.application.internal.commandservices;

import com.tuxlogic.shiftiq.platform.billing.domain.model.valueobjects.VoucherCommandFailure;
import com.tuxlogic.shiftiq.platform.billing.application.commandservices.VoucherCommandService;
import com.tuxlogic.shiftiq.platform.billing.application.outboundservices.FactosGateway;
import com.tuxlogic.shiftiq.platform.billing.domain.model.aggregates.Voucher;
import com.tuxlogic.shiftiq.platform.billing.domain.model.commands.AddPaymentCommand;
import com.tuxlogic.shiftiq.platform.billing.domain.model.commands.GenerateVoucherCommand;
import com.tuxlogic.shiftiq.platform.billing.domain.model.valueobjects.QuoteStatus;
import com.tuxlogic.shiftiq.platform.billing.domain.model.valueobjects.VoucherStatus;
import com.tuxlogic.shiftiq.platform.billing.domain.repositories.QuoteRepository;
import com.tuxlogic.shiftiq.platform.billing.domain.repositories.VoucherRepository;
import com.tuxlogic.shiftiq.platform.core.application.queryservices.BranchQueryService;
import com.tuxlogic.shiftiq.platform.core.application.queryservices.WorkshopQueryService;
import com.tuxlogic.shiftiq.platform.core.domain.model.queries.GetBranchByIdQuery;
import com.tuxlogic.shiftiq.platform.core.domain.model.queries.GetWorkshopByIdQuery;
import com.tuxlogic.shiftiq.platform.shared.domain.model.valueobjects.BranchId;
import com.tuxlogic.shiftiq.platform.shared.application.result.Result;
import com.tuxlogic.shiftiq.platform.operations.application.queryservices.WorkOrderQueryService;
import com.tuxlogic.shiftiq.platform.operations.domain.model.queries.GetWorkOrderByIdQuery;
import com.tuxlogic.shiftiq.platform.operations.domain.model.valueobjects.WorkOrderId;
import com.tuxlogic.shiftiq.platform.inventory.application.queryservices.ProductQueryService;
import com.tuxlogic.shiftiq.platform.inventory.domain.model.queries.GetProductByIdQuery;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.UUID;

/**
 * Implementation of the VoucherCommandService interface.
 * Handles the business use cases for Voucher operations, interacting with repositories
 * and with the Factos external service to emit documents to the tax authority.
 */
import lombok.extern.slf4j.Slf4j;
import com.tuxlogic.shiftiq.platform.billing.application.outboundservices.PaymentGateway;

@Slf4j
@Service
@Transactional
public class VoucherCommandServiceImpl implements VoucherCommandService {


    private final VoucherRepository voucherRepository;
    private final QuoteRepository quoteRepository;
    private final BranchQueryService branchQueryService;
    private final WorkshopQueryService workshopQueryService;
    private final FactosGateway factosGateway;
    private final PaymentGateway paymentGateway;
    private final WorkOrderQueryService workOrderQueryService;
    private final ProductQueryService productQueryService;

    public VoucherCommandServiceImpl(
            VoucherRepository voucherRepository,
            QuoteRepository quoteRepository,
            BranchQueryService branchQueryService,
            WorkshopQueryService workshopQueryService,
            FactosGateway factosGateway,
            PaymentGateway paymentGateway,
            WorkOrderQueryService workOrderQueryService,
            ProductQueryService productQueryService) {
        this.voucherRepository = voucherRepository;
        this.quoteRepository = quoteRepository;
        this.branchQueryService = branchQueryService;
        this.workshopQueryService = workshopQueryService;
        this.factosGateway = factosGateway;
        this.paymentGateway = paymentGateway;
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
        if (voucherRepository.findByQuoteId(command.quoteId()).isPresent()) {
            return Result.failure(VoucherCommandFailure.QUOTE_ALREADY_INVOICED);
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

        // 3. Issue Voucher via Factos
        var invoiceResultOpt = factosGateway.issueVoucher(
                issuerRuc,
                command.type(),
                command.customerDocumentType(),
                command.customerDocumentNumber(),
                command.customerName(),
                getDetailedBillingItems(quote)
        );

        if (invoiceResultOpt.isEmpty()) {
            return Result.failure(VoucherCommandFailure.FACTOS_ISSUANCE_FAILED);
        }

        var invoiceResult = invoiceResultOpt.get();
        UUID externalInvoiceId = UUID.nameUUIDFromBytes((invoiceResult.series() + "-" + invoiceResult.correlative()).getBytes(StandardCharsets.UTF_8));

        // 4. Create and save Voucher Aggregate
        try {
            var voucher = new Voucher(
                    command.quoteId(),
                    command.type(),
                    command.customerDocumentType(),
                    command.customerDocumentNumber(),
                    command.customerName(),
                    quote.getTotalAmount(),
                    externalInvoiceId,
                    invoiceResult.pdfUrl()
            );

            var savedVoucher = voucherRepository.save(voucher);
            return Result.success(savedVoucher);
        } catch (IllegalArgumentException e) {
            return Result.failure(VoucherCommandFailure.INVALID_VOUCHER_DATA);
        } catch (org.springframework.dao.DataIntegrityViolationException e) {
            log.error("Database constraint violation when generating voucher: {}", e.getMessage());
            return Result.failure(VoucherCommandFailure.QUOTE_ALREADY_INVOICED);
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
            if (voucher.getStatus() == VoucherStatus.PAID) {
                return Result.failure(VoucherCommandFailure.VOUCHER_ALREADY_PAID);
            }
            if (voucher.getStatus() == VoucherStatus.CANCELED) {
                return Result.failure(VoucherCommandFailure.VOUCHER_CANCELED);
            }
            if (voucher.getTotalPaidAmount().add(command.amount().amount()).compareTo(voucher.getTotalAmount().amount()) > 0) {
                return Result.failure(VoucherCommandFailure.PAYMENT_EXCEEDS_TOTAL_DEBT);
            }

            voucher.addPayment(command.amount(), command.method(), branchId);
            var savedVoucher = voucherRepository.save(voucher);
            return Result.success(savedVoucher);
        } catch (IllegalArgumentException | IllegalStateException e) {
            return Result.failure(VoucherCommandFailure.INVALID_VOUCHER_DATA);
        }

    }

    @Override
    public Result<Voucher, VoucherCommandFailure> handle(com.tuxlogic.shiftiq.platform.billing.domain.model.commands.RemovePaymentCommand command) {
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
    public Result<Voucher, VoucherCommandFailure> handle(com.tuxlogic.shiftiq.platform.billing.domain.model.commands.ProcessCheckoutCommand command) {
        // 1. Get and validate Quote
        var quoteOpt = quoteRepository.findById(command.quoteId());
        if (quoteOpt.isEmpty()) {
            return Result.failure(VoucherCommandFailure.QUOTE_NOT_FOUND);
        }
        var quote = quoteOpt.get();
        if (quote.getStatus() != QuoteStatus.APPROVED) {
            return Result.failure(VoucherCommandFailure.QUOTE_NOT_APPROVED);
        }
        if (voucherRepository.findByQuoteId(command.quoteId()).isPresent()) {
            return Result.failure(VoucherCommandFailure.QUOTE_ALREADY_INVOICED);
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

        // 3. Issue Voucher via Factos
        var invoiceResultOpt = factosGateway.issueVoucher(
                issuerRuc,
                command.type(),
                command.customerDocumentType(),
                command.customerDocumentNumber(),
                command.customerName(),
                getDetailedBillingItems(quote)
        );

        if (invoiceResultOpt.isEmpty()) {
            return Result.failure(VoucherCommandFailure.FACTOS_ISSUANCE_FAILED);
        }

        var invoiceResult = invoiceResultOpt.get();
        UUID externalInvoiceId = UUID.nameUUIDFromBytes((invoiceResult.series() + "-" + invoiceResult.correlative()).getBytes(StandardCharsets.UTF_8));

        // 4. Create Voucher Aggregate
        try {
            var voucher = new Voucher(
                    command.quoteId(),
                    command.type(),
                    command.customerDocumentType(),
                    command.customerDocumentNumber(),
                    command.customerName(),
                    quote.getTotalAmount(),
                    externalInvoiceId,
                    invoiceResult.pdfUrl()
            );

            // 5. Add full payment to the Voucher
            voucher.addPayment(quote.getTotalAmount(), command.method(), quote.getBranchId().value());

            // 6. Save the fully paid Voucher
            var savedVoucher = voucherRepository.save(voucher);
            return Result.success(savedVoucher);
        } catch (IllegalArgumentException | IllegalStateException e) {
            return Result.failure(VoucherCommandFailure.INVALID_VOUCHER_DATA);
        } catch (org.springframework.dao.DataIntegrityViolationException e) {
            log.error("Database constraint violation when processing checkout: {}", e.getMessage());
            return Result.failure(VoucherCommandFailure.QUOTE_ALREADY_INVOICED);
        }
    }

    @Override
    @Transactional
    public Result<Voucher, VoucherCommandFailure> handle(com.tuxlogic.shiftiq.platform.billing.domain.model.commands.ProcessMercadoPagoCheckoutCommand command) {
        // 1. Validate Quote exists and is in APPROVED status FIRST (prevents info leakage/unauthorized replay probing)
        var quoteOpt = quoteRepository.findById(command.quoteId());
        if (quoteOpt.isEmpty()) {
            return Result.failure(VoucherCommandFailure.QUOTE_NOT_FOUND);
        }
        var quote = quoteOpt.get();
        if (quote.getStatus() != QuoteStatus.APPROVED) {
            return Result.failure(VoucherCommandFailure.QUOTE_NOT_APPROVED);
        }

        // 2. Ensure a voucher has not already been issued for this quote
        if (voucherRepository.findByQuoteId(command.quoteId()).isPresent()) {
            log.warn("Quote ID '{}' has already been invoiced", command.quoteId());
            return Result.failure(VoucherCommandFailure.QUOTE_ALREADY_INVOICED);
        }

        // 3. Replay prevention check: Ensure paymentId hasn't been consumed by another voucher
        if (voucherRepository.existsByExternalPaymentId(command.paymentId())) {
            log.warn("Mercado Pago payment ID '{}' has already been used for another voucher", command.paymentId());
            return Result.failure(VoucherCommandFailure.PAYMENT_ALREADY_CONSUMED);
        }

        // 4. Verify Mercado Pago Payment status, amount, external reference, and currency
        var paymentResultOpt = paymentGateway.getPaymentIntent(command.paymentId());
        if (paymentResultOpt.isEmpty()) {
            return Result.failure(VoucherCommandFailure.PAYMENT_NOT_FOUND);
        }
        var paymentResult = paymentResultOpt.get();
        if (!"approved".equalsIgnoreCase(paymentResult.status()) && !"succeeded".equalsIgnoreCase(paymentResult.status())) {
            log.warn("Payment status '{}' is not approved", paymentResult.status());
            return Result.failure(VoucherCommandFailure.INVALID_VOUCHER_DATA);
        }
        if (paymentResult.amount().compareTo(quote.getTotalAmount().amount()) != 0) {
            log.warn("Payment amount '{}' does not match quote total amount '{}'", paymentResult.amount(), quote.getTotalAmount().amount());
            return Result.failure(VoucherCommandFailure.INVALID_VOUCHER_DATA);
        }
        if (paymentResult.externalReference() == null || !quote.getId().toString().equals(paymentResult.externalReference())) {
            log.warn("Payment external reference '{}' does not match quote ID '{}'", paymentResult.externalReference(), quote.getId());
            return Result.failure(VoucherCommandFailure.INVALID_VOUCHER_DATA);
        }
        if (paymentResult.currency() != null && !"PEN".equalsIgnoreCase(paymentResult.currency())) {
            log.warn("Payment currency '{}' is not 'PEN'", paymentResult.currency());
            return Result.failure(VoucherCommandFailure.INVALID_VOUCHER_DATA);
        }

        // 5. Query Core context for Issuer RUC (Tax ID)
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

        // 6. Issue Voucher via Factos (SUNAT)
        var invoiceResultOpt = factosGateway.issueVoucher(
                issuerRuc,
                command.type(),
                command.customerDocumentType(),
                command.customerDocumentNumber(),
                command.customerName(),
                getDetailedBillingItems(quote)
        );

        if (invoiceResultOpt.isEmpty()) {
            return Result.failure(VoucherCommandFailure.FACTOS_ISSUANCE_FAILED);
        }

        var invoiceResult = invoiceResultOpt.get();
        UUID externalInvoiceId = UUID.nameUUIDFromBytes((invoiceResult.series() + "-" + invoiceResult.correlative()).getBytes(StandardCharsets.UTF_8));

        // 7. Create Voucher Aggregate and record Mercado Pago payment with external payment ID
        try {
            var voucher = new Voucher(
                    command.quoteId(),
                    command.type(),
                    command.customerDocumentType(),
                    command.customerDocumentNumber(),
                    command.customerName(),
                    quote.getTotalAmount(),
                    externalInvoiceId,
                    invoiceResult.pdfUrl()
            );

            voucher.addPayment(
                    quote.getTotalAmount(),
                    com.tuxlogic.shiftiq.platform.billing.domain.model.valueobjects.PaymentMethod.CREDIT_CARD,
                    quote.getBranchId().value(),
                    "MERCADO_PAGO",
                    command.paymentId()
            );

            var savedVoucher = voucherRepository.save(voucher);
            return Result.success(savedVoucher);
        } catch (IllegalArgumentException | IllegalStateException e) {
            return Result.failure(VoucherCommandFailure.INVALID_VOUCHER_DATA);
        } catch (org.springframework.dao.DataIntegrityViolationException e) {
            log.error("Database constraint violation when saving Mercado Pago voucher: {}", e.getMessage());
            return Result.failure(VoucherCommandFailure.QUOTE_ALREADY_INVOICED);
        }
    }



    private List<FactosGateway.FactosItem> getDetailedBillingItems(com.tuxlogic.shiftiq.platform.billing.domain.model.aggregates.Quote quote) {
        List<FactosGateway.FactosItem> items = new java.util.ArrayList<>();
        
        var workOrderOpt = workOrderQueryService.handle(new GetWorkOrderByIdQuery(new WorkOrderId(quote.getWorkOrderId())));
        if (workOrderOpt.isPresent()) {
            var workOrder = workOrderOpt.get();
            if (workOrder.getTasks() != null) {
                for (var task : workOrder.getTasks()) {
                    // Calculate labor price (task total price - sum of its products)
                    com.tuxlogic.shiftiq.platform.shared.domain.model.valueobjects.Money laborPrice = task.getPrice();
                    if (task.getProducts() != null) {
                        for (var productAssoc : task.getProducts()) {
                            if (!productAssoc.isDeleted()) {
                                laborPrice = laborPrice.minus(productAssoc.getTotalAmount());
                            }
                        }
                    }
                    
                    // Add the task itself as labor item
                    items.add(new FactosGateway.FactosItem(
                            "TASK-" + task.getId(),
                            task.getDescription().value(),
                            BigDecimal.ONE,
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
                                
                                items.add(new FactosGateway.FactosItem(
                                        "PROD-" + productAssoc.getProductId().value(),
                                        productName,
                                        new BigDecimal(productAssoc.getQuantity().value()),
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
            items.add(new FactosGateway.FactosItem(
                    "SERV-001",
                    "Servicios de taller automotriz según orden " + quote.getWorkOrderId(),
                    BigDecimal.ONE,
                    quote.getTotalAmount().amount()
            ));
        }
        
        return items;
    }
}
