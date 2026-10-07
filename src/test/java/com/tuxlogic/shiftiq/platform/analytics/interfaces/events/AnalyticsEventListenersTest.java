package com.tuxlogic.shiftiq.platform.analytics.interfaces.events;

import com.tuxlogic.shiftiq.platform.analytics.application.internal.services.BranchAnalyticsSnapshotService;
import com.tuxlogic.shiftiq.platform.analytics.domain.model.aggregates.BranchAnalyticsSnapshot;
import com.tuxlogic.shiftiq.platform.analytics.domain.repositories.BranchAnalyticsRepository;
import com.tuxlogic.shiftiq.platform.fleet.domain.model.events.AppointmentCreatedEvent;
import com.tuxlogic.shiftiq.platform.inventory.domain.model.events.LowStockAlertClearedEvent;
import com.tuxlogic.shiftiq.platform.inventory.domain.model.events.LowStockAlertTriggeredEvent;
import com.tuxlogic.shiftiq.platform.iot.domain.model.events.DtcAlertTriggeredEvent;
import com.tuxlogic.shiftiq.platform.iot.domain.model.valueobjects.DtcAlertSeverity;
import com.tuxlogic.shiftiq.platform.operations.domain.model.events.WorkOrderCompletedEvent;
import com.tuxlogic.shiftiq.platform.shared.domain.model.valueobjects.BranchId;
import com.tuxlogic.shiftiq.platform.shared.domain.model.valueobjects.Money;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class AnalyticsEventListenersTest {

    private BranchAnalyticsSnapshotService snapshotService;
    private BranchAnalyticsRepository repository;
    private BranchId branchId;
    private BranchAnalyticsSnapshot snapshot;

    @BeforeEach
    void setUp() {
        snapshotService = Mockito.mock(BranchAnalyticsSnapshotService.class);
        repository = Mockito.mock(BranchAnalyticsRepository.class);
        branchId = new BranchId(UUID.randomUUID());
        snapshot = new BranchAnalyticsSnapshot(branchId, LocalDate.now());
        when(snapshotService.getOrCreateTodaySnapshot(branchId)).thenReturn(snapshot);
    }

    @Test
    @DisplayName("WorkOrderCompletedAnalyticsListener updates completed work orders and revenue")
    void testWorkOrderCompletedListener() {
        var listener = new WorkOrderCompletedAnalyticsListener(snapshotService, repository);
        var event = new WorkOrderCompletedEvent(this, branchId, null, null, new Money(new BigDecimal("150.00")));

        listener.on(event);

        assertThat(snapshot.getCompletedWorkOrdersCount()).isEqualTo(1);
        assertThat(snapshot.getTotalRevenue()).isEqualTo(new BigDecimal("150.00"));
        verify(repository).save(snapshot);
    }

    @Test
    @DisplayName("AppointmentCreatedAnalyticsListener increments appointments count")
    void testAppointmentCreatedListener() {
        var listener = new AppointmentCreatedAnalyticsListener(snapshotService, repository);
        var event = new AppointmentCreatedEvent(this, UUID.randomUUID(), branchId, null, null, LocalDateTime.now());

        listener.on(event);

        assertThat(snapshot.getTotalAppointmentsCount()).isEqualTo(1);
        verify(repository).save(snapshot);
    }

    @Test
    @DisplayName("DtcAlertAnalyticsListener increments dtc alerts count")
    void testDtcAlertListener() {
        var listener = new DtcAlertAnalyticsListener(snapshotService, repository);
        var event = new DtcAlertTriggeredEvent(UUID.randomUUID(), branchId, null, "P0300", DtcAlertSeverity.HIGH);

        listener.on(event);

        assertThat(snapshot.getDtcAlertsCount()).isEqualTo(1);
        verify(repository).save(snapshot);
    }

    @Test
    @DisplayName("InventoryStockAnalyticsListener updates low stock alerts count")
    void testInventoryStockListener() {
        var listener = new InventoryStockAnalyticsListener(snapshotService, repository);
        var triggeredEvent = new LowStockAlertTriggeredEvent(UUID.randomUUID(), branchId, 2, 5);
        var clearedEvent = new LowStockAlertClearedEvent(UUID.randomUUID(), branchId, 10, 5);

        listener.on(triggeredEvent);
        assertThat(snapshot.getLowStockAlertsCount()).isEqualTo(1);

        listener.on(clearedEvent);
        assertThat(snapshot.getLowStockAlertsCount()).isEqualTo(0);
        verify(repository, Mockito.times(2)).save(snapshot);
    }
}
