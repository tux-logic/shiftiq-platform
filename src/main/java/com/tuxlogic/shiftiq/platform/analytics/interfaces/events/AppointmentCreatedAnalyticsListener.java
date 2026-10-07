package com.tuxlogic.shiftiq.platform.analytics.interfaces.events;

import com.tuxlogic.shiftiq.platform.analytics.application.internal.services.BranchAnalyticsSnapshotService;
import com.tuxlogic.shiftiq.platform.analytics.domain.repositories.BranchAnalyticsRepository;
import com.tuxlogic.shiftiq.platform.fleet.domain.model.events.AppointmentCreatedEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
public class AppointmentCreatedAnalyticsListener {

    private static final Logger log = LoggerFactory.getLogger(AppointmentCreatedAnalyticsListener.class);
    private final BranchAnalyticsSnapshotService snapshotService;
    private final BranchAnalyticsRepository repository;

    public AppointmentCreatedAnalyticsListener(BranchAnalyticsSnapshotService snapshotService, BranchAnalyticsRepository repository) {
        this.snapshotService = snapshotService;
        this.repository = repository;
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT, fallbackExecution = true)
    public void on(AppointmentCreatedEvent event) {
        try {
            if (event.branchId() == null) return;
            var snapshot = snapshotService.getOrCreateTodaySnapshot(event.branchId());
            snapshot.incrementTotalAppointments();
            repository.save(snapshot);
            log.info("Analytics updated for AppointmentCreatedEvent on branch {}", event.branchId().value());
        } catch (Exception e) {
            log.error("Failed to process AppointmentCreatedEvent in analytics", e);
        }
    }
}
