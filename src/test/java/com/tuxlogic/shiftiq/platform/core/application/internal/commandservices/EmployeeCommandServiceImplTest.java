package com.tuxlogic.shiftiq.platform.core.application.internal.commandservices;

import com.tuxlogic.shiftiq.platform.core.domain.model.aggregates.Employee;
import com.tuxlogic.shiftiq.platform.core.domain.model.commands.CreateEmployeeCommand;
import com.tuxlogic.shiftiq.platform.core.domain.model.valueobjects.Document;
import com.tuxlogic.shiftiq.platform.core.domain.model.valueobjects.DocumentType;
import com.tuxlogic.shiftiq.platform.core.domain.model.valueobjects.PersonName;
import com.tuxlogic.shiftiq.platform.core.domain.model.valueobjects.Phone;
import com.tuxlogic.shiftiq.platform.core.domain.model.valueobjects.UserId;
import com.tuxlogic.shiftiq.platform.core.domain.repositories.EmployeeRepository;
import com.tuxlogic.shiftiq.platform.shared.application.outboundservices.identity.IdentityValidationService;
import com.tuxlogic.shiftiq.platform.shared.application.outboundservices.identity.PersonIdentityResult;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class EmployeeCommandServiceImplTest {

    @Mock
    private EmployeeRepository employeeRepository;

    @Mock
    private IdentityValidationService identityValidationService;

    private EmployeeCommandServiceImpl service;

    private final UserId userId = new UserId(UUID.randomUUID());
    private final PersonName personName = new PersonName("Carlos", "Gómez");
    private final Phone phone = new Phone("912345678");

    @BeforeEach
    void setUp() {
        service = new EmployeeCommandServiceImpl(employeeRepository, identityValidationService);
    }

    @Test
    @DisplayName("creating employee with valid DNI succeeds")
    void createEmployeeWithValidDniSuccess() {
        when(employeeRepository.existsByUserId(userId)).thenReturn(false);
        when(identityValidationService.validateDni("12345678")).thenReturn(Optional.of(
                new PersonIdentityResult("DNI", "12345678", "CARLOS", "GOMEZ RUIZ", "CARLOS GOMEZ RUIZ",
                        "Av. Larco 456", "Lima", "Lima", "Miraflores", "1", true)
        ));
        when(employeeRepository.save(any(Employee.class))).thenAnswer(inv -> inv.getArgument(0));

        var command = new CreateEmployeeCommand(userId, personName, new Document(DocumentType.DNI, "12345678"), phone);

        var result = service.handle(command);
        assertThat(result).isPresent();
        verify(employeeRepository).save(any(Employee.class));
    }

    @Test
    @DisplayName("creating employee with valid CE succeeds")
    void createEmployeeWithValidCeSuccess() {
        when(employeeRepository.existsByUserId(userId)).thenReturn(false);
        when(identityValidationService.validateCe("001234567")).thenReturn(Optional.of(
                new PersonIdentityResult("CE", "001234567", "CARLOS", "GOMEZ", "CARLOS GOMEZ",
                        null, null, null, null, null, true)
        ));
        when(employeeRepository.save(any(Employee.class))).thenAnswer(inv -> inv.getArgument(0));

        var command = new CreateEmployeeCommand(userId, personName, new Document(DocumentType.CE, "001234567"), phone);

        var result = service.handle(command);
        assertThat(result).isPresent();
        verify(employeeRepository).save(any(Employee.class));
    }

    @Test
    @DisplayName("creating employee with unverified DNI throws notFound")
    void createEmployeeWithNotFoundDniThrowsNotFound() {
        when(employeeRepository.existsByUserId(userId)).thenReturn(false);
        when(identityValidationService.validateDni("12345678")).thenReturn(Optional.empty());

        var command = new CreateEmployeeCommand(userId, personName, new Document(DocumentType.DNI, "12345678"), phone);

        assertThatThrownBy(() -> service.handle(command))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("shared.error.identity.notFound");
    }

    @Test
    @DisplayName("creating employee with unverified CE throws notFound")
    void createEmployeeWithNotFoundCeThrowsNotFound() {
        when(employeeRepository.existsByUserId(userId)).thenReturn(false);
        when(identityValidationService.validateCe("001234567")).thenReturn(Optional.empty());

        var command = new CreateEmployeeCommand(userId, personName, new Document(DocumentType.CE, "001234567"), phone);

        assertThatThrownBy(() -> service.handle(command))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("shared.error.identity.notFound");
    }
}
