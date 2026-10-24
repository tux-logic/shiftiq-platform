package com.tuxlogic.shiftiq.platform.billing.infrastructure.persistence.jpa.adapters;

import com.tuxlogic.shiftiq.platform.billing.application.outboundservices.FiscalCorrelativeService;
import com.tuxlogic.shiftiq.platform.billing.infrastructure.persistence.jpa.entities.VoucherSequencePersistenceEntity;
import com.tuxlogic.shiftiq.platform.billing.infrastructure.persistence.jpa.repositories.VoucherSequenceJpaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

/**
 * Implementation of FiscalCorrelativeService using database pessimistic write locks to prevent
 * collisions across multiple instances. Correlatives are partitioned by workshop and series
 * so that each workshop maintains independent, consecutive SUNAT numbering.
 */
@Service
public class FiscalCorrelativeServiceImpl implements FiscalCorrelativeService {

    private static final UUID DEFAULT_WORKSHOP_ID = new UUID(0L, 0L);

    private final VoucherSequenceJpaRepository sequenceRepository;

    public FiscalCorrelativeServiceImpl(VoucherSequenceJpaRepository sequenceRepository) {
        this.sequenceRepository = sequenceRepository;
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRED)
    public String nextCorrelative(UUID workshopId, String series) {
        UUID effectiveWorkshopId = workshopId != null ? workshopId : DEFAULT_WORKSHOP_ID;
        String cleanSeries = (series != null && !series.isBlank()) ? series.toUpperCase() : "B001";

        var sequence = sequenceRepository.findByWorkshopIdAndSeriesForUpdate(effectiveWorkshopId, cleanSeries)
                .orElseGet(() -> new VoucherSequencePersistenceEntity(effectiveWorkshopId, cleanSeries, 0));

        int nextValue = sequence.getLastCorrelative() + 1;
        sequence.setLastCorrelative(nextValue);
        sequenceRepository.save(sequence);

        return String.format("%08d", nextValue);
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRED)
    public String nextCorrelative(String series) {
        return nextCorrelative(null, series);
    }
}
