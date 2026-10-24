package com.tuxlogic.shiftiq.platform.billing.infrastructure.persistence.jpa.adapters;

import com.tuxlogic.shiftiq.platform.billing.infrastructure.persistence.jpa.entities.VoucherSequencePersistenceEntity;
import com.tuxlogic.shiftiq.platform.billing.infrastructure.persistence.jpa.repositories.VoucherSequenceJpaRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class FiscalCorrelativeServiceImplTest {

    @Mock
    private VoucherSequenceJpaRepository sequenceRepository;

    private FiscalCorrelativeServiceImpl service;

    @BeforeEach
    void setUp() {
        service = new FiscalCorrelativeServiceImpl(sequenceRepository);
    }

    @Test
    @DisplayName("nextCorrelative creates fresh sequence starting at 00000001 when none exists for workshop")
    void nextCorrelativeWhenNoSequenceExistsStartsAtOne() {
        UUID workshopId = UUID.randomUUID();
        when(sequenceRepository.findByWorkshopIdAndSeriesForUpdate(eq(workshopId), eq("F001")))
                .thenReturn(Optional.empty());

        String correlative = service.nextCorrelative(workshopId, "F001");

        assertThat(correlative).isEqualTo("00000001");

        ArgumentCaptor<VoucherSequencePersistenceEntity> captor = ArgumentCaptor.forClass(VoucherSequencePersistenceEntity.class);
        verify(sequenceRepository).save(captor.capture());
        assertThat(captor.getValue().getWorkshopId()).isEqualTo(workshopId);
        assertThat(captor.getValue().getSeries()).isEqualTo("F001");
        assertThat(captor.getValue().getLastCorrelative()).isEqualTo(1);
    }

    @Test
    @DisplayName("nextCorrelative increments existing sequence for the same workshop")
    void nextCorrelativeIncrementsExisting() {
        UUID workshopId = UUID.randomUUID();
        VoucherSequencePersistenceEntity existing = new VoucherSequencePersistenceEntity(workshopId, "F001", 5);
        when(sequenceRepository.findByWorkshopIdAndSeriesForUpdate(eq(workshopId), eq("F001")))
                .thenReturn(Optional.of(existing));

        String correlative = service.nextCorrelative(workshopId, "F001");

        assertThat(correlative).isEqualTo("00000006");
        assertThat(existing.getLastCorrelative()).isEqualTo(6);
        verify(sequenceRepository).save(existing);
    }

    @Test
    @DisplayName("nextCorrelative with null workshopId falls back to default workshop ID")
    void nextCorrelativeWithNullWorkshopIdUsesDefault() {
        UUID defaultId = new UUID(0L, 0L);
        when(sequenceRepository.findByWorkshopIdAndSeriesForUpdate(eq(defaultId), eq("B001")))
                .thenReturn(Optional.empty());

        String correlative = service.nextCorrelative(null, "B001");

        assertThat(correlative).isEqualTo("00000001");
        ArgumentCaptor<VoucherSequencePersistenceEntity> captor = ArgumentCaptor.forClass(VoucherSequencePersistenceEntity.class);
        verify(sequenceRepository).save(captor.capture());
        assertThat(captor.getValue().getWorkshopId()).isEqualTo(defaultId);
    }
}
