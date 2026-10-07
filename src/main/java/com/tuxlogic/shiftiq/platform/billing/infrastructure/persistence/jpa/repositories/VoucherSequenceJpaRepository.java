package com.tuxlogic.shiftiq.platform.billing.infrastructure.persistence.jpa.repositories;

import com.tuxlogic.shiftiq.platform.billing.infrastructure.persistence.jpa.entities.VoucherSequencePersistenceEntity;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface VoucherSequenceJpaRepository extends JpaRepository<VoucherSequencePersistenceEntity, String> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT s FROM VoucherSequencePersistenceEntity s WHERE s.series = :series")
    Optional<VoucherSequencePersistenceEntity> findBySeriesForUpdate(@Param("series") String series);
}
