package com.tuxlogic.shiftiq.platform.billing.infrastructure.persistence.jpa.assemblers;

import com.tuxlogic.shiftiq.platform.billing.domain.model.aggregates.Quote;
import com.tuxlogic.shiftiq.platform.billing.domain.model.aggregates.Voucher;
import com.tuxlogic.shiftiq.platform.billing.domain.model.commands.CreateQuoteCommand;
import com.tuxlogic.shiftiq.platform.billing.domain.model.valueobjects.PaymentMethod;
import com.tuxlogic.shiftiq.platform.billing.domain.model.valueobjects.VoucherType;
import com.tuxlogic.shiftiq.platform.billing.infrastructure.persistence.jpa.entities.QuotePersistenceEntity;
import com.tuxlogic.shiftiq.platform.billing.infrastructure.persistence.jpa.entities.VoucherPersistenceEntity;
import com.tuxlogic.shiftiq.platform.shared.domain.model.valueobjects.BranchId;
import com.tuxlogic.shiftiq.platform.shared.domain.model.valueobjects.Money;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class BillingPersistenceAssemblerTest {

    @Test
    void quoteToEntityLeavesIdNullForNewAggregate() {
        var workOrderId = UUID.randomUUID();
        var branchId = new BranchId(UUID.randomUUID());
        var quote = new Quote(new CreateQuoteCommand(workOrderId, branchId, 10.0), new Money(new BigDecimal("200.00")));

        var entity = QuotePersistenceAssembler.toEntity(quote);

        assertThat(quote.getId()).isNotNull();
        assertThat(entity.getId()).isNull();
        assertThat(entity.getWorkOrderId()).isEqualTo(workOrderId);
        assertThat(entity.getBranchId()).isEqualTo(branchId);
        assertThat(entity.getTotalAmount().amount()).isEqualByComparingTo("180.00");
    }

    @Test
    void quoteUpdatePreservesExistingPersistenceEntityId() {
        var existingId = UUID.randomUUID();
        var entity = new QuotePersistenceEntity();
        entity.setId(existingId);

        var quote = new Quote(
                new CreateQuoteCommand(UUID.randomUUID(), new BranchId(UUID.randomUUID()), 0.0),
                new Money(new BigDecimal("195.00"))
        );

        QuotePersistenceAssembler.updateEntityFromAggregate(entity, quote);

        assertThat(entity.getId()).isEqualTo(existingId);
    }

    @Test
    void voucherToEntityLeavesVoucherIdNullWhileAssigningPaymentId() {
        var quoteId = UUID.randomUUID();
        var branchId = UUID.randomUUID();
        var voucher = new Voucher(
                quoteId,
                VoucherType.RECEIPT,
                "DNI",
                "75981176",
                "ALAN JAIVI MAMANI VILCA",
                new Money(new BigDecimal("195.00")),
                null
        );
        voucher.addPayment(new Money(new BigDecimal("195.00")), PaymentMethod.CREDIT_CARD, branchId, "MERCADO_PAGO", "12345");

        var entity = VoucherPersistenceAssembler.toEntity(voucher);

        assertThat(voucher.getId()).isNotNull();
        assertThat(entity.getId()).isNull();
        assertThat(entity.getQuoteId()).isEqualTo(quoteId);
        assertThat(entity.getPayments()).hasSize(1);
        assertThat(entity.getPayments().get(0).getId()).isNotNull();
    }

    @Test
    void voucherUpdatePreservesExistingPersistenceEntityId() {
        var existingId = UUID.randomUUID();
        var entity = new VoucherPersistenceEntity();
        entity.setId(existingId);

        var voucher = new Voucher(
                UUID.randomUUID(),
                VoucherType.RECEIPT,
                "DNI",
                "75981176",
                "ALAN JAIVI MAMANI VILCA",
                new Money(new BigDecimal("195.00")),
                null
        );

        VoucherPersistenceAssembler.updateEntityFromAggregate(entity, voucher);

        assertThat(entity.getId()).isEqualTo(existingId);
    }
}
