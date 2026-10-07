package com.tuxlogic.shiftiq.platform.billing.infrastructure.persistence.jpa.entities;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * JPA entity representing a customer's billing and payment intent.
 * Stores customer tax document selection before redirecting to the payment gateway,
 * enabling autonomous, asynchronous issuance of electronic vouchers by the webhook.
 */
@Entity
@Table(name = "payment_intents")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class PaymentIntentPersistenceEntity {

    @Id
    private UUID id;

    @Column(name = "quote_id", nullable = false, unique = true)
    private UUID quoteId;

    @Column(name = "voucher_type", nullable = false, length = 20)
    private String voucherType;

    @Column(name = "customer_document_type", nullable = false, length = 20)
    private String customerDocumentType;

    @Column(name = "customer_document_number", nullable = false, length = 20)
    private String customerDocumentNumber;

    @Column(name = "customer_name", nullable = false, length = 150)
    private String customerName;

    @Column(name = "amount", nullable = false, precision = 10, scale = 2)
    private BigDecimal amount;

    @Column(name = "currency", nullable = false, length = 3)
    private String currency;

    @Column(name = "status", nullable = false, length = 20)
    private String status;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    public PaymentIntentPersistenceEntity(
            UUID quoteId,
            BigDecimal amount,
            String voucherType,
            String customerDocumentType,
            String customerDocumentNumber,
            String customerName
    ) {
        this.id = UUID.randomUUID();
        this.quoteId = quoteId;
        this.amount = amount;
        this.voucherType = voucherType;
        this.customerDocumentType = customerDocumentType;
        this.customerDocumentNumber = customerDocumentNumber;
        this.customerName = customerName;
        this.currency = "PEN";
        this.status = "PENDING";
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }
}
