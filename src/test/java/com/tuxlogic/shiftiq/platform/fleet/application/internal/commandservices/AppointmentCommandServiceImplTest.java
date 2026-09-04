package com.tuxlogic.shiftiq.platform.fleet.application.internal.commandservices;

import com.tuxlogic.shiftiq.platform.fleet.application.commandservices.AppointmentCommandFailure;
import com.tuxlogic.shiftiq.platform.fleet.domain.model.aggregates.Appointment;
import com.tuxlogic.shiftiq.platform.fleet.domain.model.commands.CreateAppointmentCommand;
import com.tuxlogic.shiftiq.platform.fleet.domain.repositories.AppointmentRepository;
import com.tuxlogic.shiftiq.platform.shared.domain.model.valueobjects.BranchId;
import com.tuxlogic.shiftiq.platform.shared.domain.model.valueobjects.CustomerId;
import com.tuxlogic.shiftiq.platform.shared.domain.model.valueobjects.VehicleId;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AppointmentCommandServiceImplTest {

    @Mock
    private AppointmentRepository appointmentRepository;

    @InjectMocks
    private AppointmentCommandServiceImpl commandService;

    private CreateAppointmentCommand createCommand;

    @BeforeEach
    void setUp() {
        createCommand = new CreateAppointmentCommand(
                new BranchId(UUID.randomUUID()),
                new CustomerId(UUID.randomUUID()),
                new VehicleId(UUID.randomUUID()),
                LocalDateTime.now().plusDays(1),
                new com.tuxlogic.shiftiq.platform.fleet.domain.model.valueobjects.AppointmentSummary("General Checkup")
        );
    }

    @Test
    void handle_CreateAppointment_WhenNoOverlap_ReturnsSuccess() {
        // Arrange
        when(appointmentRepository.existsOverlappingAppointment(
                any(BranchId.class), any(VehicleId.class), any(LocalDateTime.class), any(LocalDateTime.class)
        )).thenReturn(false);

        Appointment mockAppointment = new Appointment(
                createCommand.branchId(),
                createCommand.customerId(),
                createCommand.vehicleId(),
                createCommand.scheduledStart(),
                createCommand.notes()
        );
        when(appointmentRepository.save(any(Appointment.class))).thenReturn(mockAppointment);

        // Act
        var result = commandService.handle(createCommand);

        // Assert
        assertTrue(result.isSuccess());
        assertNotNull(result.success().get());
        verify(appointmentRepository, times(1)).save(any(Appointment.class));
    }

    @Test
    void handle_CreateAppointment_WhenOverlap_ReturnsFailure() {
        // Arrange
        when(appointmentRepository.existsOverlappingAppointment(
                any(BranchId.class), any(VehicleId.class), any(LocalDateTime.class), any(LocalDateTime.class)
        )).thenReturn(true);

        // Act
        var result = commandService.handle(createCommand);

        // Assert
        assertTrue(result.isFailure());
        assertEquals(AppointmentCommandFailure.APPOINTMENT_ALREADY_EXISTS, result.failure().get());
        verify(appointmentRepository, never()).save(any(Appointment.class));
    }
}
