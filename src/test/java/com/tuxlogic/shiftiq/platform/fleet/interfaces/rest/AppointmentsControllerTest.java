package com.tuxlogic.shiftiq.platform.fleet.interfaces.rest;

import com.tuxlogic.shiftiq.platform.fleet.application.commandservices.AppointmentCommandFailure;
import com.tuxlogic.shiftiq.platform.fleet.application.commandservices.AppointmentCommandService;
import com.tuxlogic.shiftiq.platform.fleet.application.queryservices.AppointmentQueryService;
import com.tuxlogic.shiftiq.platform.fleet.domain.model.aggregates.Appointment;
import com.tuxlogic.shiftiq.platform.fleet.domain.model.commands.CreateAppointmentCommand;
import com.tuxlogic.shiftiq.platform.fleet.interfaces.rest.resources.CreateAppointmentResource;
import com.tuxlogic.shiftiq.platform.fleet.domain.model.valueobjects.AppointmentSummary;
import com.tuxlogic.shiftiq.platform.shared.application.result.Result;
import com.tuxlogic.shiftiq.platform.shared.domain.model.valueobjects.BranchId;
import com.tuxlogic.shiftiq.platform.shared.domain.model.valueobjects.CustomerId;
import com.tuxlogic.shiftiq.platform.shared.domain.model.valueobjects.VehicleId;
import com.tuxlogic.shiftiq.platform.shared.infrastructure.security.MultiTenancySecurityService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.MessageSource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.time.LocalDateTime;
import java.util.Locale;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AppointmentsControllerTest {

    @Mock
    private AppointmentCommandService commandService;

    @Mock
    private AppointmentQueryService queryService;

    @Mock
    private MessageSource messageSource;

    @Mock
    private MultiTenancySecurityService multiTenancySecurityService;

    private AppointmentsController controller;

    @BeforeEach
    void setUp() {
        controller = new AppointmentsController(commandService, queryService, messageSource, multiTenancySecurityService);
    }

    @Test
    void createAppointment_WhenValid_ShouldReturnCreated() {
        UUID branchId = UUID.randomUUID();
        UUID customerId = UUID.randomUUID();
        UUID vehicleId = UUID.randomUUID();
        LocalDateTime now = LocalDateTime.now().plusDays(1);

        CreateAppointmentResource resource = new CreateAppointmentResource(
                branchId,
                customerId,
                vehicleId,
                now,
                "Consulta de prueba"
        );

        Appointment appointment = new Appointment(
                new BranchId(branchId),
                new CustomerId(customerId),
                new VehicleId(vehicleId),
                now,
                new AppointmentSummary("Consulta de prueba")
        );

        when(commandService.handle(any(CreateAppointmentCommand.class))).thenReturn(Result.success(appointment));

        ResponseEntity<?> response = controller.createAppointment(resource);

        assertNotNull(response);
        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        verify(commandService).handle(any(CreateAppointmentCommand.class));
    }

    @Test
    void createAppointment_WhenConflict_ShouldReturnError() {
        UUID branchId = UUID.randomUUID();
        UUID customerId = UUID.randomUUID();
        UUID vehicleId = UUID.randomUUID();
        LocalDateTime now = LocalDateTime.now().plusDays(1);

        CreateAppointmentResource resource = new CreateAppointmentResource(
                branchId,
                customerId,
                vehicleId,
                now,
                "Solapamiento"
        );

        when(commandService.handle(any(CreateAppointmentCommand.class))).thenReturn(Result.failure(AppointmentCommandFailure.APPOINTMENT_ALREADY_EXISTS));
        when(messageSource.getMessage(eq("fleet.error.appointment.alreadyExists"), any(), any(Locale.class))).thenReturn("Appointment already exists");

        ResponseEntity<?> response = controller.createAppointment(resource);

        assertNotNull(response);
        assertEquals(HttpStatus.CONFLICT, response.getStatusCode());
    }

    @Test
    void getById_WhenForbidden_ShouldReturnForbiddenStatus() {
        UUID appointmentId = UUID.randomUUID();
        UUID branchId = UUID.randomUUID();

        Appointment appointment = new Appointment(
                new BranchId(branchId),
                new CustomerId(UUID.randomUUID()),
                new VehicleId(UUID.randomUUID()),
                LocalDateTime.now(),
                new AppointmentSummary("Forbidden check")
        );

        when(queryService.handle(appointmentId)).thenReturn(Result.success(appointment));
        when(multiTenancySecurityService.isAuthorizedForBranch(branchId)).thenReturn(false);

        ResponseEntity<?> response = controller.getById(appointmentId);

        assertNotNull(response);
        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
    }

    @Test
    void updateAppointment_WhenForbidden_ShouldReturnForbiddenStatus() {
        UUID appointmentId = UUID.randomUUID();
        UUID branchId = UUID.randomUUID();

        Appointment appointment = new Appointment(
                new BranchId(branchId),
                new CustomerId(UUID.randomUUID()),
                new VehicleId(UUID.randomUUID()),
                LocalDateTime.now(),
                new AppointmentSummary("Forbidden check")
        );

        when(queryService.handle(appointmentId)).thenReturn(Result.success(appointment));
        when(multiTenancySecurityService.isAuthorizedForBranch(branchId)).thenReturn(false);

        ResponseEntity<?> response = controller.updateAppointment(appointmentId, null);

        assertNotNull(response);
        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
    }

    @Test
    void deleteAppointment_WhenForbidden_ShouldReturnForbiddenStatus() {
        UUID appointmentId = UUID.randomUUID();
        UUID branchId = UUID.randomUUID();

        Appointment appointment = new Appointment(
                new BranchId(branchId),
                new CustomerId(UUID.randomUUID()),
                new VehicleId(UUID.randomUUID()),
                LocalDateTime.now(),
                new AppointmentSummary("Forbidden check")
        );

        when(queryService.handle(appointmentId)).thenReturn(Result.success(appointment));
        when(multiTenancySecurityService.isAuthorizedForBranch(branchId)).thenReturn(false);

        ResponseEntity<?> response = controller.deleteAppointment(appointmentId);

        assertNotNull(response);
        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
    }

    @Test
    void getAppointments_WhenBranchForbidden_ShouldReturnForbiddenStatus() {
        UUID branchId = UUID.randomUUID();
        when(multiTenancySecurityService.isAuthorizedForBranch(branchId)).thenReturn(false);

        ResponseEntity<?> response = controller.getAppointments(branchId, null, null, null);

        assertNotNull(response);
        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
    }
}
