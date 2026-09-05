package com.tuxlogic.shiftiq.platform.fleet.application.internal.commandservices;

import com.tuxlogic.shiftiq.platform.fleet.application.commandservices.EmployeeRegistrationCommandFailure;
import com.tuxlogic.shiftiq.platform.fleet.domain.model.aggregates.EmployeeRegistration;
import com.tuxlogic.shiftiq.platform.fleet.domain.model.commands.DeleteEmployeeRegistrationCommand;
import com.tuxlogic.shiftiq.platform.fleet.domain.model.valueobjects.EmployeeRegistrationId;
import com.tuxlogic.shiftiq.platform.fleet.domain.model.valueobjects.EmployeeRegistrationStatus;
import com.tuxlogic.shiftiq.platform.fleet.domain.repositories.EmployeeRegistrationRepository;
import com.tuxlogic.shiftiq.platform.shared.domain.model.valueobjects.BranchId;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EmployeeRegistrationCommandServiceImplTest {

    @Mock
    private EmployeeRegistrationRepository repository;

    @InjectMocks
    private EmployeeRegistrationCommandServiceImpl commandService;

    private EmployeeRegistration registration;
    private EmployeeRegistrationId registrationId;

    @BeforeEach
    void setUp() {
        registrationId = new EmployeeRegistrationId(UUID.randomUUID());
        registration = new EmployeeRegistration(
                registrationId,
                UUID.randomUUID(),
                new BranchId(UUID.randomUUID()),
                "Mechanic",
                "Senior Mechanic",
                new BigDecimal("5000.00"),
                EmployeeRegistrationStatus.ACTIVE,
                Instant.now(), Instant.now(), null
        );
    }

    @Test
    void handle_DeleteEmployeeRegistration_WhenFound_ReturnsSuccessAndDeactivates() {
        // Arrange
        DeleteEmployeeRegistrationCommand command = new DeleteEmployeeRegistrationCommand(registrationId);
        when(repository.findById(registrationId)).thenReturn(Optional.of(registration));
        when(repository.save(registration)).thenReturn(registration);

        // Act
        var result = commandService.handle(command);

        // Assert
        assertTrue(result.isSuccess());
        assertEquals(registrationId.value(), result.success().get());
        assertEquals(EmployeeRegistrationStatus.INACTIVE, registration.getStatus());
        assertNotNull(registration.getDeletedAt());
        verify(repository, times(1)).save(registration);
    }

    @Test
    void handle_DeleteEmployeeRegistration_WhenNotFound_ReturnsFailure() {
        // Arrange
        DeleteEmployeeRegistrationCommand command = new DeleteEmployeeRegistrationCommand(registrationId);
        when(repository.findById(registrationId)).thenReturn(Optional.empty());

        // Act
        var result = commandService.handle(command);

        // Assert
        assertTrue(result.isFailure());
        assertEquals(EmployeeRegistrationCommandFailure.REGISTRATION_NOT_FOUND, result.failure().get());
        verify(repository, never()).save(any());
    }
}
