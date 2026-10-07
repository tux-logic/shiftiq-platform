package com.tuxlogic.shiftiq.platform.billing.domain.model.entities;

import com.tuxlogic.shiftiq.platform.billing.domain.model.valueobjects.PaymentMethod;
import com.tuxlogic.shiftiq.platform.shared.domain.model.valueobjects.Money;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Entity representing a monetary payment made against a {@link com.tuxlogic.shiftiq.platform.billing.domain.model.aggregates.Voucher}.
 * Payments track the amount paid, payment method used, branch ID, payment timestamp, and optional external provider reference.
 */
@Getter
public class Payment {
    private UUID id;
    private Money amount;
    private PaymentMethod method;
    private UUID branchId;
    private LocalDateTime paidAt;
    private String paymentProvider;
    private String externalPaymentId;

    /**
     * Default constructor required for persistence frameworks.
     */
    public Payment() {
        // Required for persistence
    }

    public Payment(Money amount, PaymentMethod method, UUID branchId) {
        this(amount, method, branchId, "MANUAL", null);
    }

    public Payment(Money amount, PaymentMethod method, UUID branchId, String paymentProvider, String externalPaymentId) {
        if (amount == null || amount.amount().compareTo(java.math.BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("billing.error.payment.invalidAmount");
        }
        if (method == null) {
            throw new IllegalArgumentException("billing.error.payment.methodRequired");
        }
        if (branchId == null) {
            throw new IllegalArgumentException("billing.error.payment.branchIdRequired");
        }

        this.id = UUID.randomUUID();
        this.amount = amount;
        this.method = method;
        this.branchId = branchId;
        this.paidAt = LocalDateTime.now();
        this.paymentProvider = paymentProvider != null ? paymentProvider : "MANUAL";
        this.externalPaymentId = externalPaymentId;
    }

    // For persistence rebuilding
    public Payment(UUID id, Money amount, PaymentMethod method, UUID branchId) {
        this(id, amount, method, branchId, LocalDateTime.now(), "MANUAL", null);
    }

    public Payment(UUID id, Money amount, PaymentMethod method, UUID branchId, LocalDateTime paidAt) {
        this(id, amount, method, branchId, paidAt, "MANUAL", null);
    }

    public Payment(UUID id, Money amount, PaymentMethod method, UUID branchId, LocalDateTime paidAt, String paymentProvider, String externalPaymentId) {
        this.id = id;
        this.amount = amount;
        this.method = method;
        this.branchId = branchId;
        this.paidAt = paidAt != null ? paidAt : LocalDateTime.now();
        this.paymentProvider = paymentProvider != null ? paymentProvider : "MANUAL";
        this.externalPaymentId = externalPaymentId;
    }
}

