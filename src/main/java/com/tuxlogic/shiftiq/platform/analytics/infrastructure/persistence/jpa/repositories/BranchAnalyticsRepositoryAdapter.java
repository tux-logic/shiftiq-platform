package com.tuxlogic.shiftiq.platform.analytics.infrastructure.persistence.jpa.repositories;

import com.tuxlogic.shiftiq.platform.analytics.domain.model.aggregates.BranchAnalyticsSnapshot;
import com.tuxlogic.shiftiq.platform.analytics.domain.model.valueobjects.SnapshotId;
import com.tuxlogic.shiftiq.platform.analytics.domain.repositories.BranchAnalyticsRepository;
import com.tuxlogic.shiftiq.platform.analytics.infrastructure.persistence.jpa.entities.BranchAnalyticsSnapshotPersistenceEntity;
import com.tuxlogic.shiftiq.platform.shared.domain.model.valueobjects.BranchId;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Component
public class BranchAnalyticsRepositoryAdapter implements BranchAnalyticsRepository {

    private final JpaBranchAnalyticsSnapshotRepository jpaRepository;

    public BranchAnalyticsRepositoryAdapter(JpaBranchAnalyticsSnapshotRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    public BranchAnalyticsSnapshot save(BranchAnalyticsSnapshot snapshot) {
        var entity = toEntity(snapshot);
        var savedEntity = jpaRepository.save(entity);
        return toDomain(savedEntity);
    }

    @Override
    public Optional<BranchAnalyticsSnapshot> findById(SnapshotId id) {
        return jpaRepository.findById(id.value()).map(this::toDomain);
    }

    @Override
    public Optional<BranchAnalyticsSnapshot> findByBranchIdAndSnapshotDate(BranchId branchId, LocalDate date) {
        return jpaRepository.findByBranchIdAndSnapshotDate(branchId.value(), date).map(this::toDomain);
    }

    @Override
    public List<BranchAnalyticsSnapshot> findByBranchIdAndSnapshotDateBetween(BranchId branchId, LocalDate startDate, LocalDate endDate) {
        return jpaRepository.findByBranchIdAndSnapshotDateBetween(branchId.value(), startDate, endDate)
                .stream()
                .map(this::toDomain)
                .toList();
    }

    @Override
    public List<BranchAnalyticsSnapshot> findBySnapshotDate(LocalDate date) {
        return jpaRepository.findBySnapshotDate(date)
                .stream()
                .map(this::toDomain)
                .toList();
    }

    private BranchAnalyticsSnapshotPersistenceEntity toEntity(BranchAnalyticsSnapshot snapshot) {
        var entity = new BranchAnalyticsSnapshotPersistenceEntity();
        entity.setId(snapshot.getId().value());
        entity.setBranchId(snapshot.getBranchId().value());
        entity.setSnapshotDate(snapshot.getSnapshotDate());
        entity.setTotalRevenue(snapshot.getTotalRevenue());
        entity.setCompletedWorkOrdersCount(snapshot.getCompletedWorkOrdersCount());
        entity.setTotalAppointmentsCount(snapshot.getTotalAppointmentsCount());
        entity.setLowStockAlertsCount(snapshot.getLowStockAlertsCount());
        entity.setDtcAlertsCount(snapshot.getDtcAlertsCount());
        entity.setCreatedAt(snapshot.getCreatedAt());
        entity.setUpdatedAt(snapshot.getUpdatedAt());
        return entity;
    }

    private BranchAnalyticsSnapshot toDomain(BranchAnalyticsSnapshotPersistenceEntity entity) {
        return new BranchAnalyticsSnapshot(
                new SnapshotId(entity.getId()),
                new BranchId(entity.getBranchId()),
                entity.getSnapshotDate(),
                entity.getTotalRevenue(),
                entity.getCompletedWorkOrdersCount(),
                entity.getTotalAppointmentsCount(),
                entity.getLowStockAlertsCount(),
                entity.getDtcAlertsCount(),
                entity.getCreatedAt(),
                entity.getUpdatedAt()
        );
    }
}
