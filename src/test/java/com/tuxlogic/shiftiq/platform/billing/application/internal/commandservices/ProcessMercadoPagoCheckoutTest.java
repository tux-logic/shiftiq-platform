package com.tuxlogic.shiftiq.platform.billing.application.internal.commandservices;

import com.tuxlogic.shiftiq.platform.billing.application.outboundservices.FactosGateway;
import com.tuxlogic.shiftiq.platform.billing.application.outboundservices.PaymentGateway;
import com.tuxlogic.shiftiq.platform.billing.application.outboundservices.PaymentIntentResult;
import com.tuxlogic.shiftiq.platform.billing.domain.model.aggregates.Quote;
import com.tuxlogic.shiftiq.platform.billing.domain.model.aggregates.Voucher;
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
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

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

    private VoucherCommandServiceImpl service;

    @BeforeEach
    void setUp() {
        service = new VoucherCommandServiceImpl(
                voucherRepository,
                quoteRepository,
                branchQueryService,
                workshopQueryService,
                factosGateway,
                paymentGateway,
                workOrderQueryService,
                null
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

        PaymentIntentResult mockPayment = new PaymentIntentResult("11223344", UUID.randomUUID().toString(), new BigDecimal("100.00"), "PEN", "approved");
        when(paymentGateway.getPaymentIntent(eq("11223344"))).thenReturn(Optional.of(mockPayment));

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

        PaymentIntentResult mockPayment = new PaymentIntentResult("11223344", quoteId.toString(), new BigDecimal("100.00"), "USD", "approved");
        when(paymentGateway.getPaymentIntent(eq("11223344"))).thenReturn(Optional.of(mockPayment));

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

        PaymentIntentResult mockPayment = new PaymentIntentResult("11223344", quoteId.toString(), new BigDecimal("100.00"), "PEN", "rejected");
        when(paymentGateway.getPaymentIntent(eq("11223344"))).thenReturn(Optional.of(mockPayment));

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

        PaymentIntentResult mockPayment = new PaymentIntentResult("11223344", quoteId.toString(), new BigDecimal("100.00"), "PEN", "approved");
        when(paymentGateway.getPaymentIntent(eq("11223344"))).thenReturn(Optional.of(mockPayment));

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
}
