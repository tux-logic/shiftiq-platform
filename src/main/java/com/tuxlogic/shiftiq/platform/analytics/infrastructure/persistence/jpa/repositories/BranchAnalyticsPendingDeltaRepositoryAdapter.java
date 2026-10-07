package com.tuxlogic.shiftiq.platform.analytics.infrastructure.persistence.jpa.repositories;

import com.tuxlogic.shiftiq.platform.analytics.domain.model.aggregates.PendingAnalyticsDelta;
import com.tuxlogic.shiftiq.platform.analytics.domain.model.valueobjects.AnalyticsDelta;
import com.tuxlogic.shiftiq.platform.analytics.domain.repositories.BranchAnalyticsPendingDeltaRepository;
import com.tuxlogic.shiftiq.platform.analytics.infrastructure.persistence.jpa.entities.BranchAnalyticsPendingDeltaPersistenceEntity;
import com.tuxlogic.shiftiq.platform.shared.domain.model.valueobjects.BranchId;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class BranchAnalyticsPendingDeltaRepositoryAdapter implements BranchAnalyticsPendingDeltaRepository {

    private final JpaBranchAnalyticsPendingDeltaRepository jpaRepository;

    public BranchAnalyticsPendingDeltaRepositoryAdapter(JpaBranchAnalyticsPendingDeltaRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    public PendingAnalyticsDelta save(PendingAnalyticsDelta pending) {
        return toDomain(jpaRepository.save(toEntity(pending)));
    }

    @Override
    public List<PendingAnalyticsDelta> findPending(int limit) {
        if (limit <= 0) {
            return List.of();
        }
        var pageable = PageRequest.of(0, limit, Sort.by(Sort.Direction.ASC, "createdAt"));
        return jpaRepository.findAll(pageable).stream()
                .map(this::toDomain)
                .toList();
    }

    @Override
    public void delete(PendingAnalyticsDelta pending) {
        jpaRepository.deleteById(pending.getId());
    }

    private BranchAnalyticsPendingDeltaPersistenceEntity toEntity(PendingAnalyticsDelta pending) {
        var entity = new BranchAnalyticsPendingDeltaPersistenceEntity();
        entity.setId(pending.getId());
        entity.setBranchId(pending.getBranchId().value());
        entity.setDeltaDate(pending.getDeltaDate());
        entity.setRevenue(pending.getDelta().revenue());
        entity.setWorkOrders(pending.getDelta().workOrders());
        entity.setAppointments(pending.getDelta().appointments());
        entity.setDtcAlerts(pending.getDelta().dtcAlerts());
        entity.setLowStockRecompute(pending.isLowStockRecompute());
        entity.setAttempts(pending.getAttempts());
        entity.setLastError(pending.getLastError());
        entity.setCreatedAt(pending.getCreatedAt());
        entity.setUpdatedAt(pending.getUpdatedAt());
        return entity;
    }

    private PendingAnalyticsDelta toDomain(BranchAnalyticsPendingDeltaPersistenceEntity entity) {
        var delta = new AnalyticsDelta(
                entity.getRevenue(),
                entity.getWorkOrders(),
                entity.getAppointments(),
                entity.getDtcAlerts());
        return new PendingAnalyticsDelta(
                entity.getId(),
                new BranchId(entity.getBranchId()),
                entity.getDeltaDate(),
                delta,
                entity.isLowStockRecompute(),
                entity.getAttempts(),
                entity.getLastError(),
                entity.getCreatedAt(),
                entity.getUpdatedAt());
    }
}
