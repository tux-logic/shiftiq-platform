package com.tuxlogic.shiftiq.platform.analytics.infrastructure.acl;

import com.tuxlogic.shiftiq.platform.analytics.domain.model.queries.CustomerDashboardSummary;
import com.tuxlogic.shiftiq.platform.analytics.domain.model.queries.CustomerDashboardSummary.ActiveWorkOrderItem;
import com.tuxlogic.shiftiq.platform.analytics.domain.model.queries.CustomerDashboardSummary.PendingQuoteItem;
import com.tuxlogic.shiftiq.platform.analytics.domain.model.queries.CustomerDashboardSummary.UpcomingAppointmentItem;
import com.tuxlogic.shiftiq.platform.analytics.domain.services.CustomerDashboardPort;
import com.tuxlogic.shiftiq.platform.billing.application.queryservices.QuoteQueryService;
import com.tuxlogic.shiftiq.platform.billing.domain.model.queries.GetQuotesByBranchIdQuery;
import com.tuxlogic.shiftiq.platform.billing.domain.model.valueobjects.QuoteStatus;
import com.tuxlogic.shiftiq.platform.core.application.queryservices.CustomerQueryService;
import com.tuxlogic.shiftiq.platform.core.domain.model.queries.GetCustomerByUserIdQuery;
import com.tuxlogic.shiftiq.platform.core.domain.model.valueobjects.UserId;
import com.tuxlogic.shiftiq.platform.fleet.application.queryservices.AppointmentQueryService;
import com.tuxlogic.shiftiq.platform.fleet.domain.model.valueobjects.AppointmentStatus;
import com.tuxlogic.shiftiq.platform.iot.application.queryservices.DtcAlertQueryService;
import com.tuxlogic.shiftiq.platform.iot.application.queryservices.VehicleQueryService;
import com.tuxlogic.shiftiq.platform.iot.domain.model.aggregates.Vehicle;
import com.tuxlogic.shiftiq.platform.iot.domain.model.queries.GetActiveVehiclesByCustomerIdQuery;
import com.tuxlogic.shiftiq.platform.iot.domain.model.queries.GetVehicleDtcAlertHistoryQuery;
import com.tuxlogic.shiftiq.platform.operations.application.queryservices.WorkOrderQueryService;
import com.tuxlogic.shiftiq.platform.operations.domain.model.aggregates.WorkOrder;
import com.tuxlogic.shiftiq.platform.operations.domain.model.queries.GetWorkOrdersByVehicleIdQuery;
import com.tuxlogic.shiftiq.platform.operations.domain.model.valueobjects.WorkOrderStatus;
import com.tuxlogic.shiftiq.platform.shared.domain.model.valueobjects.CustomerId;
import com.tuxlogic.shiftiq.platform.shared.domain.model.valueobjects.VehicleId;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Read-only anti-corruption adapter implementing {@link CustomerDashboardPort}
 * exclusively through published {@code QueryService} contracts.
 */
@Component
public class CustomerDashboardPortImpl implements CustomerDashboardPort {

    private static final int PREVIEW_LIMIT = 5;

    private final CustomerQueryService customerQueryService;
    private final VehicleQueryService vehicleQueryService;
    private final DtcAlertQueryService dtcAlertQueryService;
    private final AppointmentQueryService appointmentQueryService;
    private final WorkOrderQueryService workOrderQueryService;
    private final QuoteQueryService quoteQueryService;

    public CustomerDashboardPortImpl(
            CustomerQueryService customerQueryService,
            VehicleQueryService vehicleQueryService,
            DtcAlertQueryService dtcAlertQueryService,
            AppointmentQueryService appointmentQueryService,
            WorkOrderQueryService workOrderQueryService,
            QuoteQueryService quoteQueryService
    ) {
        this.customerQueryService = customerQueryService;
        this.vehicleQueryService = vehicleQueryService;
        this.dtcAlertQueryService = dtcAlertQueryService;
        this.appointmentQueryService = appointmentQueryService;
        this.workOrderQueryService = workOrderQueryService;
        this.quoteQueryService = quoteQueryService;
    }

    @Override
    public Optional<CustomerDashboardSummary> loadSummaryForUser(UUID userId) {
        if (userId == null) {
            return Optional.empty();
        }
        var customerOpt = customerQueryService.handle(new GetCustomerByUserIdQuery(new UserId(userId)));
        if (customerOpt.isEmpty()) {
            return Optional.empty();
        }

        UUID customerUuid = customerOpt.get().getId().value();
        List<Vehicle> vehicles = vehicleQueryService.handle(new GetActiveVehiclesByCustomerIdQuery(new CustomerId(customerUuid)));
        List<UUID> vehicleIds = vehicles.stream()
                .map(v -> v.getId().value())
                .toList();

        int dtcAlertsCount = vehicleIds.stream()
                .mapToInt(vehicleId -> dtcAlertQueryService.handle(new GetVehicleDtcAlertHistoryQuery(new VehicleId(vehicleId))).size())
                .sum();

        var appointments = appointmentQueryService.handle(new CustomerId(customerUuid))
                .success()
                .orElseGet(List::of)
                .stream()
                .filter(a -> a.getDeletedAt() == null && a.getStatus() == AppointmentStatus.PENDING)
                .sorted(Comparator.comparing(a -> a.getScheduledStart()))
                .toList();

        List<WorkOrder> allVehicleOrders = vehicleIds.stream()
                .flatMap(vehicleId -> workOrderQueryService.handle(new GetWorkOrdersByVehicleIdQuery(new VehicleId(vehicleId))).stream())
                .filter(wo -> wo.getDeletedAt() == null)
                .toList();

        List<WorkOrder> activeOrders = allVehicleOrders.stream()
                .filter(wo -> wo.getStatus() == WorkOrderStatus.PENDING || wo.getStatus() == WorkOrderStatus.IN_PROGRESS)
                .toList();

        Set<UUID> customerWorkOrderIds = allVehicleOrders.stream()
                .map(wo -> wo.getId().value())
                .collect(Collectors.toSet());

        List<PendingQuoteItem> pendingQuotes = allVehicleOrders.stream()
                .map(WorkOrder::getBranchId)
                .distinct()
                .flatMap(branchId -> quoteQueryService.handle(new GetQuotesByBranchIdQuery(branchId)).stream())
                .filter(q -> q.getStatus() == QuoteStatus.DRAFT && customerWorkOrderIds.contains(q.getWorkOrderId()))
                .map(q -> new PendingQuoteItem(
                        q.getId(),
                        q.getWorkOrderId(),
                        q.getBranchId().value(),
                        q.getTotalAmount() != null ? q.getTotalAmount().amount() : BigDecimal.ZERO,
                        q.getStatus().name()
                ))
                .toList();

        return Optional.of(new CustomerDashboardSummary(
                customerUuid,
                vehicles.size(),
                dtcAlertsCount,
                appointments.size(),
                activeOrders.size(),
                pendingQuotes.size(),
                appointments.stream()
                        .limit(PREVIEW_LIMIT)
                        .map(a -> new UpcomingAppointmentItem(
                                a.getId(),
                                a.getBranchId().value(),
                                a.getVehicleId().value(),
                                a.getScheduledStart(),
                                a.getStatus().name()
                        ))
                        .toList(),
                activeOrders.stream()
                        .limit(PREVIEW_LIMIT)
                        .map(wo -> new ActiveWorkOrderItem(
                                wo.getId().value(),
                                wo.getBranchId().value(),
                                wo.getVehicleId().value(),
                                wo.getInternalNumber(),
                                wo.getStatus().name()
                        ))
                        .toList(),
                pendingQuotes.stream().limit(PREVIEW_LIMIT).toList()
        ));
    }
}
