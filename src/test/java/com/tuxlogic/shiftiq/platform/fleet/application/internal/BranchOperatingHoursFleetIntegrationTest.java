package com.tuxlogic.shiftiq.platform.fleet.application.internal;

import com.tuxlogic.shiftiq.platform.fleet.application.commandservices.AppointmentCommandFailure;
import com.tuxlogic.shiftiq.platform.fleet.application.internal.commandservices.AppointmentCommandServiceImpl;
import com.tuxlogic.shiftiq.platform.fleet.application.internal.queryservices.BranchAvailabilityQueryServiceImpl;
import com.tuxlogic.shiftiq.platform.fleet.application.outboundservices.ExternalCoreService;
import com.tuxlogic.shiftiq.platform.fleet.application.outboundservices.ExternalVehicleService;
import com.tuxlogic.shiftiq.platform.fleet.domain.model.commands.CreateAppointmentCommand;
import com.tuxlogic.shiftiq.platform.fleet.domain.model.valueobjects.AppointmentSummary;
import com.tuxlogic.shiftiq.platform.fleet.domain.repositories.AppointmentRepository;
import com.tuxlogic.shiftiq.platform.shared.domain.model.valueobjects.BranchId;
import com.tuxlogic.shiftiq.platform.shared.domain.model.valueobjects.CustomerId;
import com.tuxlogic.shiftiq.platform.shared.domain.model.valueobjects.VehicleId;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class BranchOperatingHoursFleetIntegrationTest {

    @Mock
    private ExternalCoreService externalCoreService;
    @Mock
    private ExternalVehicleService externalVehicleService;
    @Mock
    private AppointmentRepository appointmentRepository;

    @Test
    void availabilityReturnsEmptySlotsWhenClosedAndDynamicSlotsWhenOpen() {
        var service = new BranchAvailabilityQueryServiceImpl(externalCoreService, appointmentRepository);
        var branchId = UUID.randomUUID();
        var bayId = UUID.randomUUID();
        LocalDate sunday = LocalDate.of(2026, 12, 6);
        LocalDate holidayHalfDay = LocalDate.of(2026, 12, 8);

        when(externalCoreService.findActiveBayIdsByBranchId(branchId)).thenReturn(List.of(bayId));
        when(externalCoreService.findEffectiveOperatingWindow(branchId, sunday))
                .thenReturn(Optional.of(new ExternalCoreService.OperatingWindow(false, null, null, 60)));
        when(externalCoreService.findEffectiveOperatingWindow(branchId, holidayHalfDay))
                .thenReturn(Optional.of(new ExternalCoreService.OperatingWindow(true, LocalTime.of(9, 0), LocalTime.of(13, 0), 60)));

        var closedAvailability = service.getAvailability(branchId, sunday);
        assertThat(closedAvailability.slots()).isEmpty();

        var halfDayAvailability = service.getAvailability(branchId, holidayHalfDay);
        assertThat(halfDayAvailability.slots()).hasSize(4);
        assertThat(halfDayAvailability.slots().get(0).start()).isEqualTo(LocalTime.of(9, 0));
        assertThat(halfDayAvailability.slots().get(3).end()).isEqualTo(LocalTime.of(13, 0));
    }

    @Test
    void appointmentCreationRejectsClosedDayOrOutsideOperatingHours() {
        var service = new AppointmentCommandServiceImpl(appointmentRepository, externalCoreService, externalVehicleService);
        var branchId = new BranchId(UUID.randomUUID());
        var customerId = new CustomerId(UUID.randomUUID());
        var vehicleId = new VehicleId(UUID.randomUUID());

        when(externalCoreService.existsBranchById(branchId)).thenReturn(true);
        when(externalCoreService.existsCustomerById(customerId)).thenReturn(true);
        when(externalVehicleService.existsVehicleById(vehicleId)).thenReturn(true);

        LocalDate closedDate = LocalDate.now().plusDays(3);
        when(externalCoreService.findEffectiveOperatingWindow(branchId.value(), closedDate))
                .thenReturn(Optional.of(new ExternalCoreService.OperatingWindow(false, null, null, 60)));

        var closedResult = service.handle(new CreateAppointmentCommand(
                branchId, customerId, vehicleId, null, LocalDateTime.of(closedDate, LocalTime.of(10, 0)), new AppointmentSummary("Test")
        ));
        assertThat(closedResult.isFailure()).isTrue();
        assertThat(closedResult.failure().get()).isEqualTo(AppointmentCommandFailure.INVALID_APPOINTMENT_DATA);

        LocalDate openDate = LocalDate.now().plusDays(4);
        when(externalCoreService.findEffectiveOperatingWindow(branchId.value(), openDate))
                .thenReturn(Optional.of(new ExternalCoreService.OperatingWindow(true, LocalTime.of(9, 0), LocalTime.of(13, 0), 60)));

        var afterHoursResult = service.handle(new CreateAppointmentCommand(
                branchId, customerId, vehicleId, null, LocalDateTime.of(openDate, LocalTime.of(12, 30)), new AppointmentSummary("Late")
        ));
        assertThat(afterHoursResult.isFailure()).isTrue();
        assertThat(afterHoursResult.failure().get()).isEqualTo(AppointmentCommandFailure.INVALID_APPOINTMENT_DATA);
    }
}
