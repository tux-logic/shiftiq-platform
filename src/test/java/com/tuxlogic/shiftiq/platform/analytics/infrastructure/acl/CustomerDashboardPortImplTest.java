package com.tuxlogic.shiftiq.platform.analytics.infrastructure.acl;

import com.tuxlogic.shiftiq.platform.billing.application.queryservices.QuoteQueryService;
import com.tuxlogic.shiftiq.platform.billing.domain.model.aggregates.Quote;
import com.tuxlogic.shiftiq.platform.billing.domain.model.queries.GetQuotesByBranchIdQuery;
import com.tuxlogic.shiftiq.platform.billing.domain.model.valueobjects.QuoteStatus;
import com.tuxlogic.shiftiq.platform.core.application.queryservices.CustomerQueryService;
import com.tuxlogic.shiftiq.platform.core.domain.model.aggregates.Customer;
import com.tuxlogic.shiftiq.platform.core.domain.model.queries.GetCustomerByUserIdQuery;
import com.tuxlogic.shiftiq.platform.core.domain.model.valueobjects.UserId;
import com.tuxlogic.shiftiq.platform.fleet.application.queryservices.AppointmentQueryService;
import com.tuxlogic.shiftiq.platform.fleet.domain.model.aggregates.Appointment;
import com.tuxlogic.shiftiq.platform.fleet.domain.model.valueobjects.AppointmentStatus;
import com.tuxlogic.shiftiq.platform.iot.application.queryservices.DtcAlertQueryService;
import com.tuxlogic.shiftiq.platform.iot.application.queryservices.VehicleQueryService;
import com.tuxlogic.shiftiq.platform.iot.domain.model.aggregates.DtcAlert;
import com.tuxlogic.shiftiq.platform.iot.domain.model.aggregates.Vehicle;
import com.tuxlogic.shiftiq.platform.iot.domain.model.queries.GetActiveVehiclesByCustomerIdQuery;
import com.tuxlogic.shiftiq.platform.iot.domain.model.queries.GetVehicleDtcAlertHistoryQuery;
import com.tuxlogic.shiftiq.platform.shared.domain.model.valueobjects.VehicleId;
import com.tuxlogic.shiftiq.platform.operations.application.queryservices.WorkOrderQueryService;
import com.tuxlogic.shiftiq.platform.operations.domain.model.aggregates.WorkOrder;
import com.tuxlogic.shiftiq.platform.operations.domain.model.queries.GetWorkOrdersByVehicleIdQuery;
import com.tuxlogic.shiftiq.platform.operations.domain.model.valueobjects.WorkOrderId;
import com.tuxlogic.shiftiq.platform.operations.domain.model.valueobjects.WorkOrderStatus;
import com.tuxlogic.shiftiq.platform.shared.application.result.Result;
import com.tuxlogic.shiftiq.platform.shared.domain.model.valueobjects.BranchId;
import com.tuxlogic.shiftiq.platform.shared.domain.model.valueobjects.CustomerId;
import com.tuxlogic.shiftiq.platform.shared.domain.model.valueobjects.Money;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class CustomerDashboardPortImplTest {

    private CustomerQueryService customerQueryService;
    private VehicleQueryService vehicleQueryService;
    private DtcAlertQueryService dtcAlertQueryService;
    private AppointmentQueryService appointmentQueryService;
    private WorkOrderQueryService workOrderQueryService;
    private QuoteQueryService quoteQueryService;
    private CustomerDashboardPortImpl port;

    @BeforeEach
    void setUp() {
        customerQueryService = mock(CustomerQueryService.class);
        vehicleQueryService = mock(VehicleQueryService.class);
        dtcAlertQueryService = mock(DtcAlertQueryService.class);
        appointmentQueryService = mock(AppointmentQueryService.class);
        workOrderQueryService = mock(WorkOrderQueryService.class);
        quoteQueryService = mock(QuoteQueryService.class);
        port = new CustomerDashboardPortImpl(
                customerQueryService,
                vehicleQueryService,
                dtcAlertQueryService,
                appointmentQueryService,
                workOrderQueryService,
                quoteQueryService
        );
    }

    @Test
    @DisplayName("returns empty when no customer profile exists for userId")
    void returnsEmptyWhenCustomerNotFound() {
        UUID userId = UUID.randomUUID();
        when(customerQueryService.handle(new GetCustomerByUserIdQuery(new UserId(userId))))
                .thenReturn(Optional.empty());

        assertThat(port.loadSummaryForUser(userId)).isEmpty();
    }

    @Test
    @DisplayName("aggregates vehicles, DTC alerts, pending appointments, active work orders, and draft quotes")
    void aggregatesCustomerDashboardSummary() {
        UUID userId = UUID.randomUUID();
        UUID customerId = UUID.randomUUID();
        UUID vehicleId = UUID.randomUUID();
        UUID branchId = UUID.randomUUID();
        UUID workOrderId = UUID.randomUUID();
        UUID quoteId = UUID.randomUUID();

        Customer customer = mock(Customer.class);
        when(customer.getId()).thenReturn(new CustomerId(customerId));
        when(customerQueryService.handle(new GetCustomerByUserIdQuery(new UserId(userId))))
                .thenReturn(Optional.of(customer));

        Vehicle vehicle = mock(Vehicle.class);
        when(vehicle.getId()).thenReturn(new VehicleId(vehicleId));
        when(vehicleQueryService.handle(new GetActiveVehiclesByCustomerIdQuery(new CustomerId(customerId))))
                .thenReturn(List.of(vehicle));

        when(dtcAlertQueryService.handle(new GetVehicleDtcAlertHistoryQuery(
                new com.tuxlogic.shiftiq.platform.shared.domain.model.valueobjects.VehicleId(vehicleId))))
                .thenReturn(List.of(mock(DtcAlert.class), mock(DtcAlert.class)));

        Appointment pendingAppointment = mock(Appointment.class);
        when(pendingAppointment.getId()).thenReturn(UUID.randomUUID());
        when(pendingAppointment.getBranchId()).thenReturn(new BranchId(branchId));
        when(pendingAppointment.getVehicleId()).thenReturn(new com.tuxlogic.shiftiq.platform.shared.domain.model.valueobjects.VehicleId(vehicleId));
        when(pendingAppointment.getScheduledStart()).thenReturn(LocalDateTime.now().plusDays(1));
        when(pendingAppointment.getStatus()).thenReturn(AppointmentStatus.PENDING);

        Appointment completedAppointment = mock(Appointment.class);
        when(completedAppointment.getStatus()).thenReturn(AppointmentStatus.COMPLETED);

        when(appointmentQueryService.handle(new CustomerId(customerId)))
                .thenReturn(Result.success(List.of(pendingAppointment, completedAppointment)));

        WorkOrder activeOrder = mock(WorkOrder.class);
        when(activeOrder.getId()).thenReturn(new WorkOrderId(workOrderId));
        when(activeOrder.getBranchId()).thenReturn(new BranchId(branchId));
        when(activeOrder.getVehicleId()).thenReturn(new com.tuxlogic.shiftiq.platform.shared.domain.model.valueobjects.VehicleId(vehicleId));
        when(activeOrder.getInternalNumber()).thenReturn(1042);
        when(activeOrder.getStatus()).thenReturn(WorkOrderStatus.IN_PROGRESS);
        when(workOrderQueryService.handle(new GetWorkOrdersByVehicleIdQuery(
                new com.tuxlogic.shiftiq.platform.shared.domain.model.valueobjects.VehicleId(vehicleId))))
                .thenReturn(List.of(activeOrder));

        Quote draftQuote = mock(Quote.class);
        when(draftQuote.getId()).thenReturn(quoteId);
        when(draftQuote.getWorkOrderId()).thenReturn(workOrderId);
        when(draftQuote.getBranchId()).thenReturn(new BranchId(branchId));
        when(draftQuote.getTotalAmount()).thenReturn(Money.of(new BigDecimal("250.00")));
        when(draftQuote.getStatus()).thenReturn(QuoteStatus.DRAFT);

        Quote otherOrderQuote = mock(Quote.class);
        when(otherOrderQuote.getWorkOrderId()).thenReturn(UUID.randomUUID());
        when(otherOrderQuote.getStatus()).thenReturn(QuoteStatus.DRAFT);

        when(quoteQueryService.handle(new GetQuotesByBranchIdQuery(new BranchId(branchId))))
                .thenReturn(List.of(draftQuote, otherOrderQuote));

        var result = port.loadSummaryForUser(userId);

        assertThat(result).isPresent();
        var summary = result.get();
        assertThat(summary.customerId()).isEqualTo(customerId);
        assertThat(summary.activeVehiclesCount()).isEqualTo(1);
        assertThat(summary.activeDtcAlertsCount()).isEqualTo(2);
        assertThat(summary.upcomingAppointmentsCount()).isEqualTo(1);
        assertThat(summary.activeWorkOrdersCount()).isEqualTo(1);
        assertThat(summary.pendingQuotesCount()).isEqualTo(1);
        assertThat(summary.pendingQuotes()).singleElement().satisfies(q ->
                assertThat(q.quoteId()).isEqualTo(quoteId));
    }
}
