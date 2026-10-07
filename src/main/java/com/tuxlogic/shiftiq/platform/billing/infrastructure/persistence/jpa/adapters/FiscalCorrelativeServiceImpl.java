package com.tuxlogic.shiftiq.platform.billing.infrastructure.persistence.jpa.adapters;

import com.tuxlogic.shiftiq.platform.billing.application.outboundservices.FiscalCorrelativeService;
import com.tuxlogic.shiftiq.platform.billing.infrastructure.persistence.jpa.entities.VoucherSequencePersistenceEntity;
import com.tuxlogic.shiftiq.platform.billing.infrastructure.persistence.jpa.repositories.VoucherSequenceJpaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

/**
 * Implementation of FiscalCorrelativeService using database pessimistic write locks to prevent
 * collisions across multiple instances. The allocation joins the caller transaction so the
 * correlative bump commits atomically with the voucher insert and never leaves numbering gaps.
 */
@Service
public class FiscalCorrelativeServiceImpl implements FiscalCorrelativeService {

    private final VoucherSequenceJpaRepository sequenceRepository;

    public FiscalCorrelativeServiceImpl(VoucherSequenceJpaRepository sequenceRepository) {
        this.sequenceRepository = sequenceRepository;
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRED)
    public String nextCorrelative(String series) {
        String cleanSeries = (series != null && !series.isBlank()) ? series.toUpperCase() : "B001";
        var sequence = sequenceRepository.findBySeriesForUpdate(cleanSeries)
                .orElseGet(() -> new VoucherSequencePersistenceEntity(cleanSeries, 0));

        int nextValue = sequence.getLastCorrelative() + 1;
        sequence.setLastCorrelative(nextValue);
        sequenceRepository.save(sequence);

        return String.format("%08d", nextValue);
    }
}
