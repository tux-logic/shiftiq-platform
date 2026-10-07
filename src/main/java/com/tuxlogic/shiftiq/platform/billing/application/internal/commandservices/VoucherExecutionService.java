package com.tuxlogic.shiftiq.platform.billing.application.internal.commandservices;

import com.tuxlogic.shiftiq.platform.billing.application.outboundservices.FiscalCorrelativeService;
import com.tuxlogic.shiftiq.platform.billing.application.outboundservices.PaymentGateway;
import com.tuxlogic.shiftiq.platform.billing.domain.model.aggregates.Voucher;
import com.tuxlogic.shiftiq.platform.billing.domain.model.commands.ProcessMercadoPagoCheckoutCommand;
import com.tuxlogic.shiftiq.platform.billing.domain.model.valueobjects.PaymentMethod;
import com.tuxlogic.shiftiq.platform.billing.domain.model.valueobjects.QuoteStatus;
import com.tuxlogic.shiftiq.platform.billing.domain.model.valueobjects.VoucherCommandFailure;
import com.tuxlogic.shiftiq.platform.billing.domain.model.valueobjects.VoucherStatus;
import com.tuxlogic.shiftiq.platform.billing.domain.model.valueobjects.VoucherType;
import com.tuxlogic.shiftiq.platform.billing.domain.repositories.QuoteRepository;
import com.tuxlogic.shiftiq.platform.billing.domain.repositories.VoucherRepository;
import com.tuxlogic.shiftiq.platform.core.application.queryservices.BranchQueryService;
import com.tuxlogic.shiftiq.platform.core.application.queryservices.WorkshopQueryService;
import com.tuxlogic.shiftiq.platform.core.domain.model.queries.GetBranchByIdQuery;
import com.tuxlogic.shiftiq.platform.core.domain.model.queries.GetWorkshopByIdQuery;
import com.tuxlogic.shiftiq.platform.shared.domain.model.valueobjects.BranchId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.UUID;
import org.springframework.dao.DataIntegrityViolationException;

/**
 * Service managing isolated, fine-grained database transactions for voucher lifecycle operations.
 * Enforces strict transactional decoupling around external SUNAT (Factos) invocations.
 */
@Service
public class VoucherExecutionService {

    private static final Logger log = LoggerFactory.getLogger(VoucherExecutionService.class);

    private final VoucherRepository voucherRepository;
    private final QuoteRepository quoteRepository;
    private final PaymentGateway paymentGateway;
    private final BranchQueryService branchQueryService;
    private final WorkshopQueryService workshopQueryService;
    private final FiscalCorrelativeService fiscalCorrelativeService;

    public VoucherExecutionService(
            VoucherRepository voucherRepository,
            QuoteRepository quoteRepository,
            PaymentGateway paymentGateway,
            BranchQueryService branchQueryService,
            WorkshopQueryService workshopQueryService,
            FiscalCorrelativeService fiscalCorrelativeService
    ) {
        this.voucherRepository = voucherRepository;
        this.quoteRepository = quoteRepository;
        this.paymentGateway = paymentGateway;
        this.branchQueryService = branchQueryService;
        this.workshopQueryService = workshopQueryService;
        this.fiscalCorrelativeService = fiscalCorrelativeService;
    }

    /**
     * Executes the initial phase of voucher checkout in an isolated, immediately committed transaction.
     * Prevents race conditions and guarantees that prepayment and sequential correlative allocation are persisted.
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public VoucherPreparationResult prepareOrFindVoucher(ProcessMercadoPagoCheckoutCommand command) {
        // 1. Serialize quote access with pessimistic write lock (falls back to findById in tests if unstubbed)
        var quoteOpt = quoteRepository.findByIdForUpdate(command.quoteId())
                .or(() -> quoteRepository.findById(command.quoteId()));
        if (quoteOpt.isEmpty()) {
            return new VoucherPreparationResult.Failed(VoucherCommandFailure.QUOTE_NOT_FOUND);
        }
        var quote = quoteOpt.get();
        if (quote.getStatus() != QuoteStatus.APPROVED) {
            return new VoucherPreparationResult.Failed(VoucherCommandFailure.QUOTE_NOT_APPROVED);
        }

        // 2. Check existing voucher
        var existingVoucherOpt = voucherRepository.findByQuoteId(command.quoteId());
        if (existingVoucherOpt.isPresent()) {
            var existingVoucher = existingVoucherOpt.get();
            boolean paymentMatches = existingVoucher.getPayments().stream()
                    .anyMatch(p -> command.paymentId().equals(p.getExternalPaymentId()));

            if (!paymentMatches) {
                log.warn("Quote ID '{}' already invoiced under a different payment ID", command.quoteId());
                return new VoucherPreparationResult.Failed(VoucherCommandFailure.QUOTE_ALREADY_INVOICED);
            }

            if (existingVoucher.getStatus() == VoucherStatus.PAID) {
                log.info("Voucher for quote ID '{}' and payment ID '{}' already emitted. Returning existing voucher.",
                        command.quoteId(), command.paymentId());
                return new VoucherPreparationResult.AlreadyEmitted(existingVoucher);
            }

            if (existingVoucher.getStatus() == VoucherStatus.EMISSION_FAILED) {
                log.info("Voucher for quote ID '{}' is in EMISSION_FAILED. Re-verifying payment with MP before retry...",
                        command.quoteId());
                var paymentError = validatePayment(quote, command.paymentId());
                if (paymentError != null) {
                    return new VoucherPreparationResult.Failed(paymentError);
                }

                String issuerRuc = getIssuerRuc(quote);
                if (issuerRuc == null) {
                    return new VoucherPreparationResult.Failed(VoucherCommandFailure.ISSUER_NOT_FOUND);
                }

                String correlative = existingVoucher.getCorrelative();
                if (correlative == null || correlative.isBlank()) {
                    String series = command.type() == VoucherType.INVOICE ? "F001" : "B001";
                    correlative = fiscalCorrelativeService.nextCorrelative(series);
                    existingVoucher.setCorrelative(correlative);
                }
                existingVoucher.setUpdatedAt(Instant.now());
                voucherRepository.save(existingVoucher);
                return new VoucherPreparationResult.ReadyToEmit(existingVoucher, quote, issuerRuc, correlative);
            }

            if (existingVoucher.getStatus() == VoucherStatus.PENDING) {
                Instant lastUpdate = existingVoucher.getUpdatedAt() != null ? existingVoucher.getUpdatedAt() : existingVoucher.getCreatedAt();
                boolean isStale = lastUpdate != null && Duration.between(lastUpdate, Instant.now()).toSeconds() >= 60;
                if (isStale) {
                    log.info("Voucher for quote ID '{}' is stale PENDING (>=60s). Re-verifying payment with MP before recovery...",
                            command.quoteId());
                    var paymentError = validatePayment(quote, command.paymentId());
                    if (paymentError != null) {
                        return new VoucherPreparationResult.Failed(paymentError);
                    }

                    String issuerRuc = getIssuerRuc(quote);
                    if (issuerRuc == null) {
                        return new VoucherPreparationResult.Failed(VoucherCommandFailure.ISSUER_NOT_FOUND);
                    }
                    existingVoucher.setUpdatedAt(Instant.now());
                    voucherRepository.save(existingVoucher);
                    return new VoucherPreparationResult.ReadyToEmit(existingVoucher, quote, issuerRuc, existingVoucher.getCorrelative());
                } else {
                    log.info("Voucher for quote ID '{}' is currently PENDING (<60s in progress)", command.quoteId());
                    return new VoucherPreparationResult.InProgress(existingVoucher);
                }
            }
        }

        // 3. Replay prevention check: Ensure paymentId hasn't been consumed by another voucher
        if (voucherRepository.existsByExternalPaymentId(command.paymentId())) {
            log.warn("Mercado Pago payment ID '{}' has already been used for another voucher", command.paymentId());
            return new VoucherPreparationResult.Failed(VoucherCommandFailure.PAYMENT_ALREADY_CONSUMED);
        }

        // 4. Verify Mercado Pago Payment status, amount, external reference, and currency
        var paymentError = validatePayment(quote, command.paymentId());
        if (paymentError != null) {
            return new VoucherPreparationResult.Failed(paymentError);
        }

        // 5. Query Core context for Issuer RUC (Tax ID)
        String issuerRuc = getIssuerRuc(quote);
        if (issuerRuc == null) {
            return new VoucherPreparationResult.Failed(VoucherCommandFailure.ISSUER_NOT_FOUND);
        }

        // 6. Allocate fiscal correlative and reserve Voucher in PENDING status with prepayment
        String series = command.type() == VoucherType.INVOICE ? "F001" : "B001";
        String correlative = fiscalCorrelativeService.nextCorrelative(series);

        try {
            var voucher = new Voucher(
                    command.quoteId(),
                    command.type(),
                    command.customerDocumentType(),
                    command.customerDocumentNumber(),
                    command.customerName(),
                    quote.getTotalAmount(),
                    null,
                    null
            );
            voucher.setCorrelative(correlative);
            voucher.recordPrepayment(
                    quote.getTotalAmount(),
                    PaymentMethod.CREDIT_CARD,
                    quote.getBranchId().value(),
                    "MERCADO_PAGO",
                    command.paymentId()
            );

            var savedVoucher = voucherRepository.save(voucher);
            return new VoucherPreparationResult.ReadyToEmit(savedVoucher, quote, issuerRuc, correlative);
        } catch (DataIntegrityViolationException dive) {
            log.warn("Concurrent insert conflict on voucher for quote ID '{}': {}", command.quoteId(), dive.getMessage());
            var conflictOpt = voucherRepository.findByQuoteId(command.quoteId());
            if (conflictOpt.isPresent()) {
                var cv = conflictOpt.get();
                if (cv.getStatus() == VoucherStatus.PAID) {
                    return new VoucherPreparationResult.AlreadyEmitted(cv);
                }
                return new VoucherPreparationResult.InProgress(cv);
            }
            return new VoucherPreparationResult.Failed(VoucherCommandFailure.QUOTE_ALREADY_INVOICED);
        } catch (IllegalArgumentException | IllegalStateException e) {
            log.error("Failed to construct voucher: {}", e.getMessage());
            return new VoucherPreparationResult.Failed(VoucherCommandFailure.INVALID_VOUCHER_DATA);
        }
    }

    private String getIssuerRuc(com.tuxlogic.shiftiq.platform.billing.domain.model.aggregates.Quote quote) {
        var coreBranchId = new BranchId(quote.getBranchId().value());
        var branchOpt = branchQueryService.handle(new GetBranchByIdQuery(coreBranchId));
        if (branchOpt.isEmpty()) {
            return null;
        }
        var workshopOpt = workshopQueryService.handle(new GetWorkshopByIdQuery(branchOpt.get().getWorkshopId()));
        return workshopOpt.map(w -> w.getTaxId().value()).orElse(null);
    }

    private VoucherCommandFailure validatePayment(com.tuxlogic.shiftiq.platform.billing.domain.model.aggregates.Quote quote, String paymentId) {
        var paymentResultOpt = paymentGateway.getPaymentStatusByExternalId(paymentId);
        if (paymentResultOpt.isEmpty()) {
            return VoucherCommandFailure.PAYMENT_NOT_FOUND;
        }
        var paymentResult = paymentResultOpt.get();
        if (!"approved".equalsIgnoreCase(paymentResult.status()) && !"succeeded".equalsIgnoreCase(paymentResult.status())) {
            log.warn("Payment status '{}' is not approved", paymentResult.status());
            return VoucherCommandFailure.INVALID_VOUCHER_DATA;
        }
        if (paymentResult.amount().compareTo(quote.getTotalAmount().amount()) != 0) {
            log.warn("Payment amount '{}' does not match quote total amount '{}'", paymentResult.amount(), quote.getTotalAmount().amount());
            return VoucherCommandFailure.INVALID_VOUCHER_DATA;
        }
        if (paymentResult.externalReference() == null || !quote.getId().toString().equals(paymentResult.externalReference())) {
            log.warn("Payment external reference '{}' does not match quote ID '{}'", paymentResult.externalReference(), quote.getId());
            return VoucherCommandFailure.INVALID_VOUCHER_DATA;
        }
        if (paymentResult.currency() == null || !"PEN".equalsIgnoreCase(paymentResult.currency())) {
            log.warn("Payment currency '{}' is invalid or not 'PEN'", paymentResult.currency());
            return VoucherCommandFailure.INVALID_VOUCHER_DATA;
        }
        return null;
    }

    /**
     * Confirms successful emission in SUNAT and transitions the voucher to PAID in an isolated transaction.
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public Voucher markEmissionSuccess(UUID voucherId, UUID externalInvoiceId, String pdfUrl) {
        var voucher = voucherRepository.findById(voucherId)
                .orElseThrow(() -> new IllegalStateException("Voucher not found: " + voucherId));
        voucher.markEmissionSuccessful(externalInvoiceId, pdfUrl);
        return voucherRepository.save(voucher);
    }

    /**
     * Records emission failure in SUNAT and transitions the voucher to EMISSION_FAILED in an isolated transaction.
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public Voucher markEmissionFailed(UUID voucherId) {
        var voucher = voucherRepository.findById(voucherId)
                .orElseThrow(() -> new IllegalStateException("Voucher not found: " + voucherId));
        voucher.markEmissionFailed();
        return voucherRepository.save(voucher);
    }
}
