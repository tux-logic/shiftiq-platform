package com.tuxlogic.shiftiq.platform.analytics.interfaces.events;

import com.tuxlogic.shiftiq.platform.analytics.application.internal.services.AnalyticsDeltaDispatcher;
import com.tuxlogic.shiftiq.platform.analytics.domain.model.valueobjects.AnalyticsDelta;
import com.tuxlogic.shiftiq.platform.fleet.domain.model.events.AppointmentCreatedEvent;
import com.tuxlogic.shiftiq.platform.inventory.domain.model.events.LowStockAlertClearedEvent;
import com.tuxlogic.shiftiq.platform.inventory.domain.model.events.LowStockAlertTriggeredEvent;
import com.tuxlogic.shiftiq.platform.iot.domain.model.events.DtcAlertTriggeredEvent;
import com.tuxlogic.shiftiq.platform.iot.domain.model.valueobjects.DtcAlertSeverity;
import com.tuxlogic.shiftiq.platform.operations.domain.model.events.WorkOrderCompletedEvent;
import com.tuxlogic.shiftiq.platform.operations.domain.model.events.WorkOrderReopenedEvent;
import com.tuxlogic.shiftiq.platform.shared.domain.model.valueobjects.BranchId;
import com.tuxlogic.shiftiq.platform.shared.domain.model.valueobjects.Money;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

class AnalyticsEventListenersTest {

    private AnalyticsDeltaDispatcher dispatcher;
    private BranchId branchId;

    @BeforeEach
    void setUp() {
        dispatcher = Mockito.mock(AnalyticsDeltaDispatcher.class);
        branchId = new BranchId(UUID.randomUUID());
    }

    @Test
    @DisplayName("WorkOrderCompletedAnalyticsListener dispatches one completed work order and its revenue")
    void testWorkOrderCompletedListener() {
        var listener = new WorkOrderCompletedAnalyticsListener(dispatcher);
        var event = new WorkOrderCompletedEvent(this, branchId, null, null, new Money(new BigDecimal("150.00")));

        listener.on(event);

        verify(dispatcher).dispatch(branchId, new AnalyticsDelta(new BigDecimal("150.00"), 1, 0, 0));
    }

    @Test
    @DisplayName("WorkOrderReopenedAnalyticsListener dispatches the negation of the completion")
    void testWorkOrderReopenedListener() {
        var listener = new WorkOrderReopenedAnalyticsListener(dispatcher);
        var event = new WorkOrderReopenedEvent(this, branchId, null, null, new Money(new BigDecimal("150.00")));

        listener.on(event);

        verify(dispatcher).dispatch(branchId, new AnalyticsDelta(new BigDecimal("-150.00"), -1, 0, 0));
    }

    @Test
    @DisplayName("AppointmentCreatedAnalyticsListener increments the appointments counter")
    void testAppointmentCreatedListener() {
        var listener = new AppointmentCreatedAnalyticsListener(dispatcher);
        var event = new AppointmentCreatedEvent(this, UUID.randomUUID(), branchId, null, null, LocalDateTime.now());

        listener.on(event);

        verify(dispatcher).dispatch(branchId, new AnalyticsDelta(BigDecimal.ZERO, 0, 1, 0));
    }

    @Test
    @DisplayName("DtcAlertAnalyticsListener increments the dtc alerts counter")
    void testDtcAlertListener() {
        var listener = new DtcAlertAnalyticsListener(dispatcher);
        var event = new DtcAlertTriggeredEvent(UUID.randomUUID(), branchId, null, "P0300", DtcAlertSeverity.HIGH);

        listener.on(event);

        verify(dispatcher).dispatch(branchId, new AnalyticsDelta(BigDecimal.ZERO, 0, 0, 1));
    }

    @Test
    @DisplayName("InventoryStockAnalyticsListener recomputes the low stock count on both events")
    void testInventoryStockListener() {
        var listener = new InventoryStockAnalyticsListener(dispatcher);
        var triggeredEvent = new LowStockAlertTriggeredEvent(UUID.randomUUID(), branchId, 2, 5);
        var clearedEvent = new LowStockAlertClearedEvent(UUID.randomUUID(), branchId, 10, 5);

        listener.on(triggeredEvent);
        listener.on(clearedEvent);

        verify(dispatcher, Mockito.times(2)).dispatchLowStockRecompute(branchId);
        verify(dispatcher, never()).dispatch(Mockito.eq(branchId), Mockito.any(AnalyticsDelta.class));
    }

    @Test
    @DisplayName("listeners skip events without branch")
    void testListenersSkipEventsWithoutBranch() {
        new WorkOrderCompletedAnalyticsListener(dispatcher)
                .on(new WorkOrderCompletedEvent(this, null, null, null, new Money(BigDecimal.ZERO)));
        new WorkOrderReopenedAnalyticsListener(dispatcher)
                .on(new WorkOrderReopenedEvent(this, null, null, null, new Money(BigDecimal.ZERO)));
        new AppointmentCreatedAnalyticsListener(dispatcher)
                .on(new AppointmentCreatedEvent(this, UUID.randomUUID(), null, null, null, LocalDateTime.now()));
        new DtcAlertAnalyticsListener(dispatcher)
                .on(new DtcAlertTriggeredEvent(UUID.randomUUID(), null, null, "P0300", DtcAlertSeverity.HIGH));
        new InventoryStockAnalyticsListener(dispatcher)
                .on(new LowStockAlertTriggeredEvent(UUID.randomUUID(), null, 2, 5));

        Mockito.verifyNoInteractions(dispatcher);
    }
}
