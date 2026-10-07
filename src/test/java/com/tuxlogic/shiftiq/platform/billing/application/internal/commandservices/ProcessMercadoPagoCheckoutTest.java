package com.tuxlogic.shiftiq.platform.billing.application.internal.commandservices;

import com.tuxlogic.shiftiq.platform.billing.application.outboundservices.FactosGateway;
import com.tuxlogic.shiftiq.platform.billing.application.outboundservices.PaymentGateway;
import com.tuxlogic.shiftiq.platform.billing.application.outboundservices.PaymentResult;
import com.tuxlogic.shiftiq.platform.billing.domain.model.aggregates.Quote;
import com.tuxlogic.shiftiq.platform.billing.domain.model.aggregates.Voucher;
import com.tuxlogic.shiftiq.platform.billing.domain.model.commands.GenerateVoucherCommand;
import com.tuxlogic.shiftiq.platform.billing.domain.model.commands.ProcessMercadoPagoCheckoutCommand;
import com.tuxlogic.shiftiq.platform.billing.domain.model.valueobjects.*;
import com.tuxlogic.shiftiq.platform.billing.domain.repositories.QuoteRepository;
import com.tuxlogic.shiftiq.platform.billing.domain.repositories.VoucherRepository;
import com.tuxlogic.shiftiq.platform.core.application.queryservices.BranchQueryService;
import com.tuxlogic.shiftiq.platform.core.application.queryservices.WorkshopQueryService;
import com.tuxlogic.shiftiq.platform.operations.application.queryservices.WorkOrderQueryService;
import com.tuxlogic.shiftiq.platform.shared.application.result.Result;
import com.tuxlogic.shiftiq.platform.shared.domain.model.valueobjects.BranchId;
import com.tuxlogic.shiftiq.platform.shared.domain.model.valueobjects.Money;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

import com.tuxlogic.shiftiq.platform.billing.application.outboundservices.FiscalCorrelativeService;
import com.tuxlogic.shiftiq.platform.billing.infrastructure.persistence.jpa.repositories.PaymentIntentJpaRepository;

@ExtendWith(MockitoExtension.class)
class ProcessMercadoPagoCheckoutTest {

    @Mock
    private VoucherRepository voucherRepository;
    @Mock
    private QuoteRepository quoteRepository;
    @Mock
    private BranchQueryService branchQueryService;
    @Mock
    private WorkshopQueryService workshopQueryService;
    @Mock
    private FactosGateway factosGateway;
    @Mock
    private PaymentGateway paymentGateway;
    @Mock
    private WorkOrderQueryService workOrderQueryService;
    @Mock
    private FiscalCorrelativeService fiscalCorrelativeService;
    @Mock
    private PaymentIntentJpaRepository paymentIntentJpaRepository;

    private VoucherExecutionService voucherExecutionService;
    private VoucherCommandServiceImpl service;

    @BeforeEach
    void setUp() {
        voucherExecutionService = new VoucherExecutionService(
                voucherRepository,
                quoteRepository,
                paymentGateway,
                branchQueryService,
                workshopQueryService,
                fiscalCorrelativeService
        );
        service = new VoucherCommandServiceImpl(
                voucherRepository,
                quoteRepository,
                branchQueryService,
                workshopQueryService,
                factosGateway,
                workOrderQueryService,
                null,
                voucherExecutionService,
                paymentIntentJpaRepository
        );
    }

    @Test
    @DisplayName("ProcessMercadoPagoCheckout fails with PAYMENT_ALREADY_CONSUMED when paymentId has already been consumed (Replay Attack)")
    void processCheckoutFailsWhenPaymentIdAlreadyConsumed() {
        UUID quoteId = UUID.randomUUID();
        Quote quote = new Quote(quoteId, UUID.randomUUID(), new BranchId(UUID.randomUUID()), new Money(new BigDecimal("100.00")), 0.0, new Money(new BigDecimal("100.00")), QuoteStatus.APPROVED);

        when(quoteRepository.findById(eq(quoteId))).thenReturn(Optional.of(quote));
        when(voucherRepository.findByQuoteId(eq(quoteId))).thenReturn(Optional.empty());

        String consumedPaymentId = "99887766";
        when(voucherRepository.existsByExternalPaymentId(eq(consumedPaymentId))).thenReturn(true);

        ProcessMercadoPagoCheckoutCommand command = new ProcessMercadoPagoCheckoutCommand(
                quoteId,
                VoucherType.RECEIPT,
                "DNI",
                "12345678",
                "Juan Perez",
                consumedPaymentId
        );

        Result<Voucher, VoucherCommandFailure> result = service.handle(command);

        assertThat(result.isFailure()).isTrue();
        assertThat(result.failure().get()).isEqualTo(VoucherCommandFailure.PAYMENT_ALREADY_CONSUMED);
    }

    @Test
    @DisplayName("ProcessMercadoPagoCheckout fails with QUOTE_ALREADY_INVOICED when quote has already been invoiced")
    void processCheckoutFailsWhenQuoteAlreadyInvoiced() {
        UUID quoteId = UUID.randomUUID();
        Quote quote = new Quote(quoteId, UUID.randomUUID(), new BranchId(UUID.randomUUID()), new Money(new BigDecimal("100.00")), 0.0, new Money(new BigDecimal("100.00")), QuoteStatus.APPROVED);
        Voucher existingVoucher = new Voucher(quoteId, VoucherType.RECEIPT, "DNI", "12345678", "Juan Perez", quote.getTotalAmount(), UUID.randomUUID(), "http://pdf");

        when(quoteRepository.findById(eq(quoteId))).thenReturn(Optional.of(quote));
        when(voucherRepository.findByQuoteId(eq(quoteId))).thenReturn(Optional.of(existingVoucher));

        ProcessMercadoPagoCheckoutCommand command = new ProcessMercadoPagoCheckoutCommand(
                quoteId,
                VoucherType.RECEIPT,
                "DNI",
                "12345678",
                "Juan Perez",
                "11223344"
        );

        Result<Voucher, VoucherCommandFailure> result = service.handle(command);

        assertThat(result.isFailure()).isTrue();
        assertThat(result.failure().get()).isEqualTo(VoucherCommandFailure.QUOTE_ALREADY_INVOICED);
        verify(voucherRepository, never()).existsByExternalPaymentId(any());
    }

    @Test
    @DisplayName("ProcessMercadoPagoCheckout fails with QUOTE_NOT_APPROVED when quote is in DRAFT state")
    void processCheckoutFailsWhenQuoteNotApproved() {
        UUID quoteId = UUID.randomUUID();
        Quote draftQuote = new Quote(quoteId, UUID.randomUUID(), new BranchId(UUID.randomUUID()), new Money(new BigDecimal("100.00")), 0.0, new Money(new BigDecimal("100.00")), QuoteStatus.DRAFT);

        when(quoteRepository.findById(eq(quoteId))).thenReturn(Optional.of(draftQuote));

        ProcessMercadoPagoCheckoutCommand command = new ProcessMercadoPagoCheckoutCommand(
                quoteId,
                VoucherType.RECEIPT,
                "DNI",
                "12345678",
                "Juan Perez",
                "11223344"
        );

        Result<Voucher, VoucherCommandFailure> result = service.handle(command);

        assertThat(result.isFailure()).isTrue();
        assertThat(result.failure().get()).isEqualTo(VoucherCommandFailure.QUOTE_NOT_APPROVED);
    }

    @Test
    @DisplayName("ProcessMercadoPagoCheckout fails with INVALID_VOUCHER_DATA when payment externalReference does not match quoteId")
    void processCheckoutFailsWhenExternalReferenceMismatched() {
        UUID quoteId = UUID.randomUUID();
        Quote quote = new Quote(quoteId, UUID.randomUUID(), new BranchId(UUID.randomUUID()), new Money(new BigDecimal("100.00")), 0.0, new Money(new BigDecimal("100.00")), QuoteStatus.APPROVED);

        when(quoteRepository.findById(eq(quoteId))).thenReturn(Optional.of(quote));
        when(voucherRepository.findByQuoteId(eq(quoteId))).thenReturn(Optional.empty());
        when(voucherRepository.existsByExternalPaymentId(any())).thenReturn(false);

        PaymentResult mockPayment = new PaymentResult("11223344", UUID.randomUUID().toString(), new BigDecimal("100.00"), "PEN", "approved");
        when(paymentGateway.getPaymentStatusByExternalId(eq("11223344"))).thenReturn(Optional.of(mockPayment));

        ProcessMercadoPagoCheckoutCommand command = new ProcessMercadoPagoCheckoutCommand(
                quoteId,
                VoucherType.RECEIPT,
                "DNI",
                "12345678",
                "Juan Perez",
                "11223344"
        );

        Result<Voucher, VoucherCommandFailure> result = service.handle(command);

        assertThat(result.isFailure()).isTrue();
        assertThat(result.failure().get()).isEqualTo(VoucherCommandFailure.INVALID_VOUCHER_DATA);
    }

    @Test
    @DisplayName("ProcessMercadoPagoCheckout fails with INVALID_VOUCHER_DATA when payment currency is not PEN")
    void processCheckoutFailsWhenCurrencyIsNotPen() {
        UUID quoteId = UUID.randomUUID();
        Quote quote = new Quote(quoteId, UUID.randomUUID(), new BranchId(UUID.randomUUID()), new Money(new BigDecimal("100.00")), 0.0, new Money(new BigDecimal("100.00")), QuoteStatus.APPROVED);

        when(quoteRepository.findById(eq(quoteId))).thenReturn(Optional.of(quote));
        when(voucherRepository.findByQuoteId(eq(quoteId))).thenReturn(Optional.empty());
        when(voucherRepository.existsByExternalPaymentId(any())).thenReturn(false);

        PaymentResult mockPayment = new PaymentResult("11223344", quoteId.toString(), new BigDecimal("100.00"), "USD", "approved");
        when(paymentGateway.getPaymentStatusByExternalId(eq("11223344"))).thenReturn(Optional.of(mockPayment));

        ProcessMercadoPagoCheckoutCommand command = new ProcessMercadoPagoCheckoutCommand(
                quoteId,
                VoucherType.RECEIPT,
                "DNI",
                "12345678",
                "Juan Perez",
                "11223344"
        );

        Result<Voucher, VoucherCommandFailure> result = service.handle(command);

        assertThat(result.isFailure()).isTrue();
        assertThat(result.failure().get()).isEqualTo(VoucherCommandFailure.INVALID_VOUCHER_DATA);
    }

    @Test
    @DisplayName("ProcessMercadoPagoCheckout fails with INVALID_VOUCHER_DATA when payment currency is null (Fail-Closed)")
    void processCheckoutFailsWhenCurrencyIsNull() {
        UUID quoteId = UUID.randomUUID();
        Quote quote = new Quote(quoteId, UUID.randomUUID(), new BranchId(UUID.randomUUID()), new Money(new BigDecimal("100.00")), 0.0, new Money(new BigDecimal("100.00")), QuoteStatus.APPROVED);

        when(quoteRepository.findById(eq(quoteId))).thenReturn(Optional.of(quote));
        when(voucherRepository.findByQuoteId(eq(quoteId))).thenReturn(Optional.empty());
        when(voucherRepository.existsByExternalPaymentId(any())).thenReturn(false);

        PaymentResult mockPayment = new PaymentResult("11223344", quoteId.toString(), new BigDecimal("100.00"), null, "approved");
        when(paymentGateway.getPaymentStatusByExternalId(eq("11223344"))).thenReturn(Optional.of(mockPayment));

        ProcessMercadoPagoCheckoutCommand command = new ProcessMercadoPagoCheckoutCommand(
                quoteId,
                VoucherType.RECEIPT,
                "DNI",
                "12345678",
                "Juan Perez",
                "11223344"
        );

        Result<Voucher, VoucherCommandFailure> result = service.handle(command);

        assertThat(result.isFailure()).isTrue();
        assertThat(result.failure().get()).isEqualTo(VoucherCommandFailure.INVALID_VOUCHER_DATA);
    }

    @Test
    @DisplayName("ProcessMercadoPagoCheckout fails with INVALID_VOUCHER_DATA when payment status is not approved")
    void processCheckoutFailsWhenPaymentStatusRejected() {
        UUID quoteId = UUID.randomUUID();
        Quote quote = new Quote(quoteId, UUID.randomUUID(), new BranchId(UUID.randomUUID()), new Money(new BigDecimal("100.00")), 0.0, new Money(new BigDecimal("100.00")), QuoteStatus.APPROVED);

        when(quoteRepository.findById(eq(quoteId))).thenReturn(Optional.of(quote));
        when(voucherRepository.findByQuoteId(eq(quoteId))).thenReturn(Optional.empty());
        when(voucherRepository.existsByExternalPaymentId(any())).thenReturn(false);

        PaymentResult mockPayment = new PaymentResult("11223344", quoteId.toString(), new BigDecimal("100.00"), "PEN", "rejected");
        when(paymentGateway.getPaymentStatusByExternalId(eq("11223344"))).thenReturn(Optional.of(mockPayment));

        ProcessMercadoPagoCheckoutCommand command = new ProcessMercadoPagoCheckoutCommand(
                quoteId,
                VoucherType.RECEIPT,
                "DNI",
                "12345678",
                "Juan Perez",
                "11223344"
        );

        Result<Voucher, VoucherCommandFailure> result = service.handle(command);

        assertThat(result.isFailure()).isTrue();
        assertThat(result.failure().get()).isEqualTo(VoucherCommandFailure.INVALID_VOUCHER_DATA);
    }

    @Test
    @DisplayName("ProcessMercadoPagoCheckout fails with INVALID_VOUCHER_DATA when payment amount does not match quote total")
    void processCheckoutFailsWhenPaymentAmountMismatched() {
        UUID quoteId = UUID.randomUUID();
        Quote quote = new Quote(quoteId, UUID.randomUUID(), new BranchId(UUID.randomUUID()), new Money(new BigDecimal("200.00")), 0.0, new Money(new BigDecimal("200.00")), QuoteStatus.APPROVED);

        when(quoteRepository.findById(eq(quoteId))).thenReturn(Optional.of(quote));
        when(voucherRepository.findByQuoteId(eq(quoteId))).thenReturn(Optional.empty());
        when(voucherRepository.existsByExternalPaymentId(any())).thenReturn(false);

        PaymentResult mockPayment = new PaymentResult("11223344", quoteId.toString(), new BigDecimal("100.00"), "PEN", "approved");
        when(paymentGateway.getPaymentStatusByExternalId(eq("11223344"))).thenReturn(Optional.of(mockPayment));

        ProcessMercadoPagoCheckoutCommand command = new ProcessMercadoPagoCheckoutCommand(
                quoteId,
                VoucherType.RECEIPT,
                "DNI",
                "12345678",
                "Juan Perez",
                "11223344"
        );

        Result<Voucher, VoucherCommandFailure> result = service.handle(command);

        assertThat(result.isFailure()).isTrue();
        assertThat(result.failure().get()).isEqualTo(VoucherCommandFailure.INVALID_VOUCHER_DATA);
    }

    @Test
    @DisplayName("ProcessMercadoPagoCheckout succeeds idempotently when voucher already exists and was paid with the same externalPaymentId")
    void processCheckoutSucceedsIdempotentlyWhenVoucherAlreadyIssuedWithSamePaymentId() {
        UUID quoteId = UUID.randomUUID();
        Quote quote = new Quote(quoteId, UUID.randomUUID(), new BranchId(UUID.randomUUID()), new Money(new BigDecimal("100.00")), 0.0, new Money(new BigDecimal("100.00")), QuoteStatus.APPROVED);

        Voucher existingVoucher = new Voucher(quoteId, VoucherType.RECEIPT, "DNI", "12345678", "Juan Perez", quote.getTotalAmount(), null, null);
        existingVoucher.recordPrepayment(quote.getTotalAmount(), PaymentMethod.CREDIT_CARD, quote.getBranchId().value(), "MERCADO_PAGO", "11223344");
        existingVoucher.markEmissionSuccessful(UUID.randomUUID(), "http://pdf");

        when(quoteRepository.findById(eq(quoteId))).thenReturn(Optional.of(quote));
        when(voucherRepository.findByQuoteId(eq(quoteId))).thenReturn(Optional.of(existingVoucher));

        ProcessMercadoPagoCheckoutCommand command = new ProcessMercadoPagoCheckoutCommand(
                quoteId,
                VoucherType.RECEIPT,
                "DNI",
                "12345678",
                "Juan Perez",
                "11223344"
        );

        Result<Voucher, VoucherCommandFailure> result = service.handle(command);

        assertThat(result.isSuccess()).isTrue();
        assertThat(result.success().get()).isSameAs(existingVoucher);
        assertThat(result.success().get().getStatus()).isEqualTo(VoucherStatus.PAID);
        verify(voucherRepository, never()).save(any());
        verify(factosGateway, never()).issueVoucher(any(), any(), any(), any(), any(), any(), any());
    }

    @Test
    @DisplayName("ProcessMercadoPagoCheckout retries successfully when voucher already exists in EMISSION_FAILED")
    void processCheckoutRetriesSuccessfullyWhenVoucherIsInEmissionFailed() {
        UUID quoteId = UUID.randomUUID();
        UUID branchId = UUID.randomUUID();
        UUID workshopId = UUID.randomUUID();
        Quote quote = new Quote(quoteId, UUID.randomUUID(), new BranchId(branchId), new Money(new BigDecimal("100.00")), 0.0, new Money(new BigDecimal("100.00")), QuoteStatus.APPROVED);

        Voucher failedVoucher = new Voucher(quoteId, VoucherType.RECEIPT, "DNI", "12345678", "Juan Perez", quote.getTotalAmount(), null, null);
        failedVoucher.setCorrelative("00000042");
        failedVoucher.recordPrepayment(quote.getTotalAmount(), PaymentMethod.CREDIT_CARD, branchId, "MERCADO_PAGO", "11223344");
        failedVoucher.markEmissionFailed();

        when(quoteRepository.findById(eq(quoteId))).thenReturn(Optional.of(quote));
        when(voucherRepository.findByQuoteId(eq(quoteId))).thenReturn(Optional.of(failedVoucher));

        PaymentResult mockPayment = new PaymentResult("11223344", quoteId.toString(), new BigDecimal("100.00"), "PEN", "approved");
        when(paymentGateway.getPaymentStatusByExternalId(eq("11223344"))).thenReturn(Optional.of(mockPayment));

        com.tuxlogic.shiftiq.platform.core.domain.model.aggregates.Branch mockBranch =
                mock(com.tuxlogic.shiftiq.platform.core.domain.model.aggregates.Branch.class);
        when(mockBranch.getWorkshopId()).thenReturn(new com.tuxlogic.shiftiq.platform.core.domain.model.valueobjects.WorkshopId(workshopId));
        when(branchQueryService.handle(any(com.tuxlogic.shiftiq.platform.core.domain.model.queries.GetBranchByIdQuery.class)))
                .thenReturn(Optional.of(mockBranch));

        com.tuxlogic.shiftiq.platform.core.domain.model.aggregates.Workshop mockWorkshop =
                mock(com.tuxlogic.shiftiq.platform.core.domain.model.aggregates.Workshop.class);
        when(mockWorkshop.getTaxId()).thenReturn(new com.tuxlogic.shiftiq.platform.core.domain.model.valueobjects.TaxId("20123456789"));
        when(workshopQueryService.handle(any(com.tuxlogic.shiftiq.platform.core.domain.model.queries.GetWorkshopByIdQuery.class)))
                .thenReturn(Optional.of(mockWorkshop));

        FactosGateway.FactosInvoiceResult invoiceResult =
                new FactosGateway.FactosInvoiceResult("B001", "00000042", "http://pdf-url", new BigDecimal("100.00"));
        when(factosGateway.issueVoucher(eq("20123456789"), eq(VoucherType.RECEIPT), eq("DNI"), eq("12345678"), eq("Juan Perez"), any(), eq("00000042")))
                .thenReturn(Optional.of(invoiceResult));

        when(voucherRepository.findById(failedVoucher.getId())).thenReturn(Optional.of(failedVoucher));
        when(voucherRepository.save(failedVoucher)).thenReturn(failedVoucher);

        ProcessMercadoPagoCheckoutCommand command = new ProcessMercadoPagoCheckoutCommand(
                quoteId,
                VoucherType.RECEIPT,
                "DNI",
                "12345678",
                "Juan Perez",
                "11223344"
        );

        Result<Voucher, VoucherCommandFailure> result = service.handle(command);

        assertThat(result.isSuccess()).isTrue();
        assertThat(result.success().get().getStatus()).isEqualTo(VoucherStatus.PAID);
        assertThat(result.success().get().getCorrelative()).isEqualTo("00000042");
        assertThat(result.success().get().getPdfUrl()).isEqualTo("http://pdf-url");
        verify(fiscalCorrelativeService, never()).nextCorrelative(any());
    }

    @Test
    @DisplayName("ProcessMercadoPagoCheckout fails with INVALID_VOUCHER_DATA when retrying EMISSION_FAILED but payment was refunded in MP")
    void processCheckoutFailsWhenRetryingEmissionFailedWithRefundedPayment() {
        UUID quoteId = UUID.randomUUID();
        UUID branchId = UUID.randomUUID();
        Quote quote = new Quote(quoteId, UUID.randomUUID(), new BranchId(branchId), new Money(new BigDecimal("100.00")), 0.0, new Money(new BigDecimal("100.00")), QuoteStatus.APPROVED);

        Voucher failedVoucher = new Voucher(quoteId, VoucherType.RECEIPT, "DNI", "12345678", "Juan Perez", quote.getTotalAmount(), null, null);
        failedVoucher.recordPrepayment(quote.getTotalAmount(), PaymentMethod.CREDIT_CARD, branchId, "MERCADO_PAGO", "11223344");
        failedVoucher.markEmissionFailed();

        when(quoteRepository.findById(eq(quoteId))).thenReturn(Optional.of(quote));
        when(voucherRepository.findByQuoteId(eq(quoteId))).thenReturn(Optional.of(failedVoucher));

        // MP returns refunded
        PaymentResult refundedPayment = new PaymentResult("11223344", quoteId.toString(), new BigDecimal("100.00"), "PEN", "refunded");
        when(paymentGateway.getPaymentStatusByExternalId(eq("11223344"))).thenReturn(Optional.of(refundedPayment));

        ProcessMercadoPagoCheckoutCommand command = new ProcessMercadoPagoCheckoutCommand(
                quoteId,
                VoucherType.RECEIPT,
                "DNI",
                "12345678",
                "Juan Perez",
                "11223344"
        );

        Result<Voucher, VoucherCommandFailure> result = service.handle(command);

        assertThat(result.isFailure()).isTrue();
        assertThat(result.failure().get()).isEqualTo(VoucherCommandFailure.INVALID_VOUCHER_DATA);
        verify(factosGateway, never()).issueVoucher(any(), any(), any(), any(), any(), any(), any());
    }

    @Test
    @DisplayName("ProcessMercadoPagoCheckout recovers stale PENDING voucher (>=60s) with existing correlative")
    void processCheckoutRecoversStalePendingVoucher() {
        UUID quoteId = UUID.randomUUID();
        UUID branchId = UUID.randomUUID();
        UUID workshopId = UUID.randomUUID();
        Quote quote = new Quote(quoteId, UUID.randomUUID(), new BranchId(branchId), new Money(new BigDecimal("100.00")), 0.0, new Money(new BigDecimal("100.00")), QuoteStatus.APPROVED);

        // Stale voucher created 120s ago
        java.time.Instant oldTime = java.time.Instant.now().minusSeconds(120);
        Voucher staleVoucher = new Voucher(quoteId, VoucherType.RECEIPT, "DNI", "12345678", "Juan Perez", quote.getTotalAmount(), null, null);
        staleVoucher.setCorrelative("00000088");
        staleVoucher.setUpdatedAt(oldTime);
        staleVoucher.recordPrepayment(quote.getTotalAmount(), PaymentMethod.CREDIT_CARD, branchId, "MERCADO_PAGO", "11223344");
        staleVoucher.setUpdatedAt(oldTime);

        when(quoteRepository.findById(eq(quoteId))).thenReturn(Optional.of(quote));
        when(voucherRepository.findByQuoteId(eq(quoteId))).thenReturn(Optional.of(staleVoucher));

        PaymentResult mockPayment = new PaymentResult("11223344", quoteId.toString(), new BigDecimal("100.00"), "PEN", "approved");
        when(paymentGateway.getPaymentStatusByExternalId(eq("11223344"))).thenReturn(Optional.of(mockPayment));

        com.tuxlogic.shiftiq.platform.core.domain.model.aggregates.Branch mockBranch =
                mock(com.tuxlogic.shiftiq.platform.core.domain.model.aggregates.Branch.class);
        when(mockBranch.getWorkshopId()).thenReturn(new com.tuxlogic.shiftiq.platform.core.domain.model.valueobjects.WorkshopId(workshopId));
        when(branchQueryService.handle(any(com.tuxlogic.shiftiq.platform.core.domain.model.queries.GetBranchByIdQuery.class)))
                .thenReturn(Optional.of(mockBranch));

        com.tuxlogic.shiftiq.platform.core.domain.model.aggregates.Workshop mockWorkshop =
                mock(com.tuxlogic.shiftiq.platform.core.domain.model.aggregates.Workshop.class);
        when(mockWorkshop.getTaxId()).thenReturn(new com.tuxlogic.shiftiq.platform.core.domain.model.valueobjects.TaxId("20123456789"));
        when(workshopQueryService.handle(any(com.tuxlogic.shiftiq.platform.core.domain.model.queries.GetWorkshopByIdQuery.class)))
                .thenReturn(Optional.of(mockWorkshop));

        FactosGateway.FactosInvoiceResult invoiceResult =
                new FactosGateway.FactosInvoiceResult("B001", "00000088", "http://pdf-stale", new BigDecimal("100.00"));
        when(factosGateway.issueVoucher(eq("20123456789"), eq(VoucherType.RECEIPT), eq("DNI"), eq("12345678"), eq("Juan Perez"), any(), eq("00000088")))
                .thenReturn(Optional.of(invoiceResult));

        when(voucherRepository.findById(staleVoucher.getId())).thenReturn(Optional.of(staleVoucher));
        when(voucherRepository.save(staleVoucher)).thenReturn(staleVoucher);

        ProcessMercadoPagoCheckoutCommand command = new ProcessMercadoPagoCheckoutCommand(
                quoteId,
                VoucherType.RECEIPT,
                "DNI",
                "12345678",
                "Juan Perez",
                "11223344"
        );

        Result<Voucher, VoucherCommandFailure> result = service.handle(command);

        assertThat(result.isSuccess()).isTrue();
        assertThat(result.success().get().getStatus()).isEqualTo(VoucherStatus.PAID);
        assertThat(result.success().get().getCorrelative()).isEqualTo("00000088");
        verify(fiscalCorrelativeService, never()).nextCorrelative(any());
    }

    @Test
    @DisplayName("ProcessMercadoPagoCheckout returns VOUCHER_EMISSION_IN_PROGRESS when voucher is active PENDING (<60s)")
    void processCheckoutReturnsEmissionInProgressWhenVoucherIsActivePending() {
        UUID quoteId = UUID.randomUUID();
        UUID branchId = UUID.randomUUID();
        Quote quote = new Quote(quoteId, UUID.randomUUID(), new BranchId(branchId), new Money(new BigDecimal("100.00")), 0.0, new Money(new BigDecimal("100.00")), QuoteStatus.APPROVED);

        // Active voucher created 5s ago
        Voucher activeVoucher = new Voucher(quoteId, VoucherType.RECEIPT, "DNI", "12345678", "Juan Perez", quote.getTotalAmount(), null, null);
        activeVoucher.setCorrelative("00000099");
        activeVoucher.recordPrepayment(quote.getTotalAmount(), PaymentMethod.CREDIT_CARD, branchId, "MERCADO_PAGO", "11223344");

        when(quoteRepository.findById(eq(quoteId))).thenReturn(Optional.of(quote));
        when(voucherRepository.findByQuoteId(eq(quoteId))).thenReturn(Optional.of(activeVoucher));

        ProcessMercadoPagoCheckoutCommand command = new ProcessMercadoPagoCheckoutCommand(
                quoteId,
                VoucherType.RECEIPT,
                "DNI",
                "12345678",
                "Juan Perez",
                "11223344"
        );

        Result<Voucher, VoucherCommandFailure> result = service.handle(command);

        assertThat(result.isFailure()).isTrue();
        assertThat(result.failure().get()).isEqualTo(VoucherCommandFailure.VOUCHER_EMISSION_IN_PROGRESS);
        verify(factosGateway, never()).issueVoucher(any(), any(), any(), any(), any(), any(), any());
    }

    @Test
    @DisplayName("GenerateVoucher reserves the fiscal correlative before calling Factos and persists it on the voucher")
    void generateVoucherReservesCorrelativeBeforeCallingFactos() {
        UUID quoteId = UUID.randomUUID();
        UUID branchId = UUID.randomUUID();
        Quote quote = approvedQuote(quoteId, branchId);

        when(quoteRepository.findById(eq(quoteId))).thenReturn(Optional.of(quote));
        when(voucherRepository.findByQuoteId(eq(quoteId))).thenReturn(Optional.empty());
        stubIssuer("20123456789", UUID.randomUUID());

        when(fiscalCorrelativeService.nextCorrelative(eq("F001"))).thenReturn("00000007");
        List<SavedVoucherState> savedStates = stubVoucherPersistence();

        FactosGateway.FactosInvoiceResult invoiceResult =
                new FactosGateway.FactosInvoiceResult("F001", "00000007", "http://pdf-url", new BigDecimal("100.00"));
        when(factosGateway.issueVoucher(eq("20123456789"), eq(VoucherType.INVOICE), eq("RUC"), eq("20601234567"),
                eq("Transportes Lima S.A.C."), any(), eq("00000007")))
                .thenReturn(Optional.of(invoiceResult));

        GenerateVoucherCommand command = new GenerateVoucherCommand(
                quoteId,
                VoucherType.INVOICE,
                "RUC",
                "20601234567",
                "Transportes Lima S.A.C."
        );

        Result<Voucher, VoucherCommandFailure> result = service.handle(command);

        assertThat(result.isSuccess()).isTrue();
        Voucher emittedVoucher = result.success().get();
        assertThat(emittedVoucher.getCorrelative()).isEqualTo("00000007");
        assertThat(emittedVoucher.getBranchId()).isEqualTo(branchId);
        assertThat(emittedVoucher.getExternalInvoiceId()).isNotNull();

        SavedVoucherState reserved = savedStates.get(0);
        assertThat(reserved.correlative()).isEqualTo("00000007");
        assertThat(reserved.branchId()).isEqualTo(branchId);
        assertThat(reserved.externalInvoiceId()).isNull();
        assertThat(reserved.status()).isEqualTo(VoucherStatus.PENDING);

        InOrder inOrder = inOrder(fiscalCorrelativeService, factosGateway);
        inOrder.verify(fiscalCorrelativeService).nextCorrelative(eq("F001"));
        inOrder.verify(factosGateway).issueVoucher(any(), any(), any(), any(), any(), any(), any());
    }

    @Test
    @DisplayName("GenerateVoucher keeps the reserved correlative attached to the voucher when Factos fails")
    void generateVoucherKeepsReservedCorrelativeWhenFactosFails() {
        UUID quoteId = UUID.randomUUID();
        UUID branchId = UUID.randomUUID();
        Quote quote = approvedQuote(quoteId, branchId);

        when(quoteRepository.findById(eq(quoteId))).thenReturn(Optional.of(quote));
        when(voucherRepository.findByQuoteId(eq(quoteId))).thenReturn(Optional.empty());
        stubIssuer("20123456789", UUID.randomUUID());

        when(fiscalCorrelativeService.nextCorrelative(eq("F001"))).thenReturn("00000007");
        List<SavedVoucherState> savedStates = stubVoucherPersistence();

        when(factosGateway.issueVoucher(eq("20123456789"), eq(VoucherType.INVOICE), eq("RUC"), eq("20601234567"),
                eq("Transportes Lima S.A.C."), any(), eq("00000007")))
                .thenReturn(Optional.empty());

        GenerateVoucherCommand command = new GenerateVoucherCommand(
                quoteId,
                VoucherType.INVOICE,
                "RUC",
                "20601234567",
                "Transportes Lima S.A.C."
        );

        Result<Voucher, VoucherCommandFailure> result = service.handle(command);

        assertThat(result.isFailure()).isTrue();
        assertThat(result.failure().get()).isEqualTo(VoucherCommandFailure.FACTOS_ISSUANCE_FAILED);
        verify(fiscalCorrelativeService).nextCorrelative(eq("F001"));

        SavedVoucherState reserved = savedStates.get(0);
        assertThat(reserved.correlative()).isEqualTo("00000007");
        assertThat(reserved.externalInvoiceId()).isNull();
        SavedVoucherState failed = savedStates.get(savedStates.size() - 1);
        assertThat(failed.correlative()).isEqualTo("00000007");
        assertThat(failed.status()).isEqualTo(VoucherStatus.EMISSION_FAILED);
    }

    @Test
    @DisplayName("GenerateVoucher retries a failed emission reusing the reserved correlative instead of allocating a new one")
    void generateVoucherRetryReusesReservedCorrelative() {
        UUID quoteId = UUID.randomUUID();
        UUID branchId = UUID.randomUUID();
        Quote quote = approvedQuote(quoteId, branchId);
        Voucher failedVoucher = new Voucher(
                UUID.randomUUID(), quoteId, VoucherType.INVOICE, "RUC", "20601234567", "Transportes Lima S.A.C.",
                quote.getTotalAmount(), VoucherStatus.EMISSION_FAILED, null, null, List.of(), "00000007");
        failedVoucher.setBranchId(branchId);

        when(quoteRepository.findById(eq(quoteId))).thenReturn(Optional.of(quote));
        when(voucherRepository.findByQuoteId(eq(quoteId))).thenReturn(Optional.of(failedVoucher));
        stubIssuer("20123456789", UUID.randomUUID());
        when(voucherRepository.findById(eq(failedVoucher.getId()))).thenReturn(Optional.of(failedVoucher));
        when(voucherRepository.save(any(Voucher.class))).thenAnswer(invocation -> invocation.getArgument(0));

        FactosGateway.FactosInvoiceResult invoiceResult =
                new FactosGateway.FactosInvoiceResult("F001", "00000007", "http://pdf-url", new BigDecimal("100.00"));
        when(factosGateway.issueVoucher(eq("20123456789"), eq(VoucherType.INVOICE), eq("RUC"), eq("20601234567"),
                eq("Transportes Lima S.A.C."), any(), eq("00000007")))
                .thenReturn(Optional.of(invoiceResult));

        GenerateVoucherCommand command = new GenerateVoucherCommand(
                quoteId,
                VoucherType.INVOICE,
                "RUC",
                "20601234567",
                "Transportes Lima S.A.C."
        );

        Result<Voucher, VoucherCommandFailure> result = service.handle(command);

        assertThat(result.isSuccess()).isTrue();
        assertThat(result.success().get().getCorrelative()).isEqualTo("00000007");
        assertThat(result.success().get().getStatus()).isEqualTo(VoucherStatus.PENDING);
        assertThat(result.success().get().getExternalInvoiceId()).isNotNull();
        verify(fiscalCorrelativeService, never()).nextCorrelative(any());
    }

    private Quote approvedQuote(UUID quoteId, UUID branchId) {
        return new Quote(quoteId, UUID.randomUUID(), new BranchId(branchId), new Money(new BigDecimal("100.00")), 0.0,
                new Money(new BigDecimal("100.00")), QuoteStatus.APPROVED);
    }

    private void stubIssuer(String taxId, UUID workshopId) {
        com.tuxlogic.shiftiq.platform.core.domain.model.aggregates.Branch mockBranch =
                mock(com.tuxlogic.shiftiq.platform.core.domain.model.aggregates.Branch.class);
        when(mockBranch.getWorkshopId()).thenReturn(new com.tuxlogic.shiftiq.platform.core.domain.model.valueobjects.WorkshopId(workshopId));
        when(branchQueryService.handle(any(com.tuxlogic.shiftiq.platform.core.domain.model.queries.GetBranchByIdQuery.class)))
                .thenReturn(Optional.of(mockBranch));

        com.tuxlogic.shiftiq.platform.core.domain.model.aggregates.Workshop mockWorkshop =
                mock(com.tuxlogic.shiftiq.platform.core.domain.model.aggregates.Workshop.class);
        when(mockWorkshop.getTaxId()).thenReturn(new com.tuxlogic.shiftiq.platform.core.domain.model.valueobjects.TaxId(taxId));
        when(workshopQueryService.handle(any(com.tuxlogic.shiftiq.platform.core.domain.model.queries.GetWorkshopByIdQuery.class)))
                .thenReturn(Optional.of(mockWorkshop));
    }

    private List<SavedVoucherState> stubVoucherPersistence() {
        List<SavedVoucherState> savedStates = new ArrayList<>();
        Voucher[] lastSaved = new Voucher[1];
        when(voucherRepository.save(any(Voucher.class))).thenAnswer(invocation -> {
            Voucher voucher = invocation.getArgument(0);
            lastSaved[0] = voucher;
            savedStates.add(new SavedVoucherState(
                    voucher.getCorrelative(), voucher.getExternalInvoiceId(), voucher.getStatus(), voucher.getBranchId()));
            return voucher;
        });
        when(voucherRepository.findById(any(UUID.class))).thenAnswer(invocation -> Optional.ofNullable(lastSaved[0]));
        return savedStates;
    }

    private record SavedVoucherState(String correlative, UUID externalInvoiceId, VoucherStatus status, UUID branchId) {
    }

    @Test
    @DisplayName("ProcessMercadoPagoCheckout verifies the Mercado Pago payment before acquiring the pessimistic quote lock")
    void processCheckoutValidatesPaymentBeforeAcquiringQuoteLock() {
        UUID quoteId = UUID.randomUUID();
        UUID branchId = UUID.randomUUID();
        UUID workshopId = UUID.randomUUID();
        Quote quote = new Quote(quoteId, UUID.randomUUID(), new BranchId(branchId), new Money(new BigDecimal("100.00")), 0.0, new Money(new BigDecimal("100.00")), QuoteStatus.APPROVED);

        when(quoteRepository.findById(eq(quoteId))).thenReturn(Optional.of(quote));
        when(quoteRepository.findByIdForUpdate(eq(quoteId))).thenReturn(Optional.of(quote));
        when(voucherRepository.findByQuoteId(eq(quoteId))).thenReturn(Optional.empty());

        PaymentResult mockPayment = new PaymentResult("11223344", quoteId.toString(), new BigDecimal("100.00"), "PEN", "approved");
        when(paymentGateway.getPaymentStatusByExternalId(eq("11223344"))).thenReturn(Optional.of(mockPayment));

        com.tuxlogic.shiftiq.platform.core.domain.model.aggregates.Branch mockBranch =
                mock(com.tuxlogic.shiftiq.platform.core.domain.model.aggregates.Branch.class);
        when(mockBranch.getWorkshopId()).thenReturn(new com.tuxlogic.shiftiq.platform.core.domain.model.valueobjects.WorkshopId(workshopId));
        when(branchQueryService.handle(any(com.tuxlogic.shiftiq.platform.core.domain.model.queries.GetBranchByIdQuery.class)))
                .thenReturn(Optional.of(mockBranch));

        com.tuxlogic.shiftiq.platform.core.domain.model.aggregates.Workshop mockWorkshop =
                mock(com.tuxlogic.shiftiq.platform.core.domain.model.aggregates.Workshop.class);
        when(mockWorkshop.getTaxId()).thenReturn(new com.tuxlogic.shiftiq.platform.core.domain.model.valueobjects.TaxId("20123456789"));
        when(workshopQueryService.handle(any(com.tuxlogic.shiftiq.platform.core.domain.model.queries.GetWorkshopByIdQuery.class)))
                .thenReturn(Optional.of(mockWorkshop));

        when(fiscalCorrelativeService.nextCorrelative(eq("B001"))).thenReturn("00000001");

        Voucher emittedVoucher = new Voucher(quoteId, VoucherType.RECEIPT, "DNI", "12345678", "Juan Perez", quote.getTotalAmount(), UUID.randomUUID(), "http://pdf-url");
        when(voucherRepository.save(any(Voucher.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(voucherRepository.findById(any(UUID.class))).thenReturn(Optional.of(emittedVoucher));

        FactosGateway.FactosInvoiceResult invoiceResult =
                new FactosGateway.FactosInvoiceResult("B001", "00000001", "http://pdf-url", new BigDecimal("100.00"));
        when(factosGateway.issueVoucher(eq("20123456789"), eq(VoucherType.RECEIPT), eq("DNI"), eq("12345678"), eq("Juan Perez"), any(), eq("00000001")))
                .thenReturn(Optional.of(invoiceResult));

        ProcessMercadoPagoCheckoutCommand command = new ProcessMercadoPagoCheckoutCommand(
                quoteId,
                VoucherType.RECEIPT,
                "DNI",
                "12345678",
                "Juan Perez",
                "11223344"
        );

        Result<Voucher, VoucherCommandFailure> result = service.handle(command);

        assertThat(result.isSuccess()).isTrue();
        ArgumentCaptor<Voucher> savedVoucherCaptor = ArgumentCaptor.forClass(Voucher.class);
        verify(voucherRepository, atLeastOnce()).save(savedVoucherCaptor.capture());
        assertThat(savedVoucherCaptor.getAllValues().get(0).getBranchId()).isEqualTo(branchId);
        InOrder inOrder = inOrder(quoteRepository, paymentGateway);
        inOrder.verify(quoteRepository).findById(eq(quoteId));
        inOrder.verify(paymentGateway).getPaymentStatusByExternalId(eq("11223344"));
        inOrder.verify(quoteRepository).findByIdForUpdate(eq(quoteId));
    }
}
