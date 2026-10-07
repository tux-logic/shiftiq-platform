package com.tuxlogic.shiftiq.platform.analytics.application.internal.services;

import com.tuxlogic.shiftiq.platform.analytics.domain.model.aggregates.BranchAnalyticsSnapshot;
import com.tuxlogic.shiftiq.platform.analytics.domain.repositories.BranchAnalyticsRepository;
import com.tuxlogic.shiftiq.platform.shared.domain.model.valueobjects.BranchId;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

@Service
public class BranchAnalyticsSnapshotService {

    private final BranchAnalyticsRepository repository;

    public BranchAnalyticsSnapshotService(BranchAnalyticsRepository repository) {
        this.repository = repository;
    }

    @Transactional
    public BranchAnalyticsSnapshot getOrCreateTodaySnapshot(BranchId branchId) {
        var today = LocalDate.now();
        return repository.findByBranchIdAndSnapshotDate(branchId, today)
                .orElseGet(() -> repository.save(new BranchAnalyticsSnapshot(branchId, today)));
    }
}
