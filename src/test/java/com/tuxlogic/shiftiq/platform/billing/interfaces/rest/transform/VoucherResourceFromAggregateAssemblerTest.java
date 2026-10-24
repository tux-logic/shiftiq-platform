package com.tuxlogic.shiftiq.platform.billing.interfaces.rest.transform;

import com.tuxlogic.shiftiq.platform.billing.domain.model.aggregates.Voucher;
import com.tuxlogic.shiftiq.platform.billing.domain.model.valueobjects.VoucherStatus;
import com.tuxlogic.shiftiq.platform.billing.domain.model.valueobjects.VoucherType;
import com.tuxlogic.shiftiq.platform.billing.infrastructure.persistence.jpa.assemblers.VoucherPersistenceAssembler;
import com.tuxlogic.shiftiq.platform.billing.infrastructure.persistence.jpa.entities.VoucherPersistenceEntity;
import com.tuxlogic.shiftiq.platform.billing.interfaces.rest.resources.VoucherResource;
import com.tuxlogic.shiftiq.platform.shared.domain.model.valueobjects.Money;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class VoucherResourceFromAggregateAssemblerTest {

    @Test
    @DisplayName("toResourceFromAggregate exposes series, correlative, and formatted documentNumber")
    void toResourceFromAggregateExposesFiscalIdentifiers() {
        UUID voucherId = UUID.randomUUID();
        UUID quoteId = UUID.randomUUID();
        Voucher voucher = new Voucher(
                voucherId,
                quoteId,
                VoucherType.INVOICE,
                "RUC",
                "20601234567",
                "Empresa SAC",
                Money.of(200.0),
                VoucherStatus.PAID,
                UUID.randomUUID(),
                "https://pdf.url",
                List.of(),
                "00000042",
                "F001",
                Instant.now(),
                Instant.now()
        );

        VoucherResource resource = VoucherResourceFromAggregateAssembler.toResourceFromAggregate(voucher);

        assertThat(resource.series()).isEqualTo("F001");
        assertThat(resource.correlative()).isEqualTo("00000042");
        assertThat(resource.documentNumber()).isEqualTo("F001-00000042");
    }

    @Test
    @DisplayName("VoucherPersistenceAssembler round-trips series and correlative")
    void persistenceAssemblerRoundTripsFiscalIdentifiers() {
        UUID voucherId = UUID.randomUUID();
        UUID quoteId = UUID.randomUUID();
        Voucher voucher = new Voucher(
                voucherId,
                quoteId,
                VoucherType.RECEIPT,
                "DNI",
                "12345678",
                "Juan Perez",
                Money.of(150.0),
                VoucherStatus.PAID,
                UUID.randomUUID(),
                "https://pdf.url",
                List.of(),
                "00000100",
                "B001",
                Instant.now(),
                Instant.now()
        );

        VoucherPersistenceEntity entity = VoucherPersistenceAssembler.toEntity(voucher);
        assertThat(entity.getSeries()).isEqualTo("B001");
        assertThat(entity.getCorrelative()).isEqualTo("00000100");

        Voucher restored = VoucherPersistenceAssembler.toAggregate(entity);
        assertThat(restored.getSeries()).isEqualTo("B001");
        assertThat(restored.getCorrelative()).isEqualTo("00000100");
        assertThat(restored.getDocumentNumber()).isEqualTo("B001-00000100");
    }
}
