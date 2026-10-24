package com.tuxlogic.shiftiq.platform.core.application.internal.commandservices;

import com.tuxlogic.shiftiq.platform.core.domain.model.aggregates.Customer;
import com.tuxlogic.shiftiq.platform.core.domain.model.commands.CreateCustomerCommand;
import com.tuxlogic.shiftiq.platform.core.domain.model.valueobjects.Document;
import com.tuxlogic.shiftiq.platform.core.domain.model.valueobjects.DocumentType;
import com.tuxlogic.shiftiq.platform.core.domain.model.valueobjects.PersonName;
import com.tuxlogic.shiftiq.platform.core.domain.model.valueobjects.Phone;
import com.tuxlogic.shiftiq.platform.core.domain.model.valueobjects.UserId;
import com.tuxlogic.shiftiq.platform.core.domain.repositories.CustomerRepository;
import com.tuxlogic.shiftiq.platform.shared.application.outboundservices.identity.CompanyIdentityResult;
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
class CustomerCommandServiceImplTest {

    @Mock
    private CustomerRepository customerRepository;

    @Mock
    private IdentityValidationService identityValidationService;

    private CustomerCommandServiceImpl service;

    private final UserId userId = new UserId(UUID.randomUUID());
    private final PersonName personName = new PersonName("Juan", "Pérez");
    private final Phone phone = new Phone("987654321");

    @BeforeEach
    void setUp() {
        service = new CustomerCommandServiceImpl(customerRepository, identityValidationService);
    }

    @Test
    @DisplayName("creating natural customer with valid DNI succeeds")
    void createCustomerNaturalWithValidDniSuccess() {
        when(customerRepository.existsByUserId(userId)).thenReturn(false);
        when(identityValidationService.validateDni("12345678")).thenReturn(Optional.of(
                new PersonIdentityResult("DNI", "12345678", "JUAN", "PEREZ LOPEZ", "JUAN PEREZ LOPEZ", "Av. Peru 123", "Lima", "Lima", "Miraflores", "7", true)
        ));
        when(customerRepository.save(any(Customer.class))).thenAnswer(inv -> inv.getArgument(0));

        var command = new CreateCustomerCommand(
                userId, false, personName, null, new Document(DocumentType.DNI, "12345678"), phone
        );

        var result = service.handle(command);
        assertThat(result).isPresent();
        verify(customerRepository).save(any(Customer.class));
    }

    @Test
    @DisplayName("creating natural customer with valid CE succeeds")
    void createCustomerNaturalWithValidCeSuccess() {
        when(customerRepository.existsByUserId(userId)).thenReturn(false);
        when(identityValidationService.validateCe("001234567")).thenReturn(Optional.of(
                new PersonIdentityResult("CE", "001234567", "JOHN", "DOE SMITH", "JOHN DOE SMITH", null, null, null, null, null, true)
        ));
        when(customerRepository.save(any(Customer.class))).thenAnswer(inv -> inv.getArgument(0));

        var command = new CreateCustomerCommand(
                userId, false, personName, null, new Document(DocumentType.CE, "001234567"), phone
        );

        var result = service.handle(command);
        assertThat(result).isPresent();
        verify(customerRepository).save(any(Customer.class));
    }

    @Test
    @DisplayName("creating natural customer with unverified DNI throws notFound")
    void createCustomerNaturalWithNotFoundDniThrowsNotFound() {
        when(customerRepository.existsByUserId(userId)).thenReturn(false);
        when(identityValidationService.validateDni("12345678")).thenReturn(Optional.empty());

        var command = new CreateCustomerCommand(
                userId, false, personName, null, new Document(DocumentType.DNI, "12345678"), phone
        );

        assertThatThrownBy(() -> service.handle(command))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("shared.error.identity.notFound");
    }

    @Test
    @DisplayName("creating natural customer with unverified CE throws notFound")
    void createCustomerNaturalWithNotFoundCeThrowsNotFound() {
        when(customerRepository.existsByUserId(userId)).thenReturn(false);
        when(identityValidationService.validateCe("001234567")).thenReturn(Optional.empty());

        var command = new CreateCustomerCommand(
                userId, false, personName, null, new Document(DocumentType.CE, "001234567"), phone
        );

        assertThatThrownBy(() -> service.handle(command))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("shared.error.identity.notFound");
    }

    @Test
    @DisplayName("creating corporate customer with valid RUC succeeds")
    void createCustomerCorporateWithValidRucSuccess() {
        when(customerRepository.existsByUserId(userId)).thenReturn(false);
        when(identityValidationService.validateRuc("20123456789")).thenReturn(Optional.of(
                new CompanyIdentityResult("20123456789", "EMPRESA SAC", "ACTIVO", "HABIDO",
                        "Av. Siempre Viva 123", "Lima", "Lima", "Miraflores", "150122", true, true, true)
        ));
        when(customerRepository.save(any(Customer.class))).thenAnswer(inv -> inv.getArgument(0));

        var command = new CreateCustomerCommand(
                userId, true, null, "EMPRESA SAC", new Document(DocumentType.RUC, "20123456789"), phone
        );

        var result = service.handle(command);
        assertThat(result).isPresent();
        verify(customerRepository).save(any(Customer.class));
    }

    @Test
    @DisplayName("creating corporate customer with non-RUC document throws error")
    void createCustomerCorporateWithNonRucDocumentThrowsError() {
        when(customerRepository.existsByUserId(userId)).thenReturn(false);

        var command = new CreateCustomerCommand(
                userId, true, null, "EMPRESA SAC", new Document(DocumentType.DNI, "12345678"), phone
        );

        assertThatThrownBy(() -> service.handle(command))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("billing.error.invoice.ruc.documentType");
    }

    @Test
    @DisplayName("creating corporate customer with unverified RUC throws notFound")
    void createCustomerCorporateWithNotFoundRucThrowsNotFound() {
        when(customerRepository.existsByUserId(userId)).thenReturn(false);
        when(identityValidationService.validateRuc("20123456789")).thenReturn(Optional.empty());

        var command = new CreateCustomerCommand(
                userId, true, null, "EMPRESA SAC", new Document(DocumentType.RUC, "20123456789"), phone
        );

        assertThatThrownBy(() -> service.handle(command))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("shared.error.identity.notFound");
    }

    @Test
    @DisplayName("creating corporate customer with inactive RUC throws rucInactive")
    void createCustomerCorporateWithInactiveRucThrowsInactive() {
        when(customerRepository.existsByUserId(userId)).thenReturn(false);
        when(identityValidationService.validateRuc("20123456789")).thenReturn(Optional.of(
                new CompanyIdentityResult("20123456789", "EMPRESA SAC", "SUSPENDIDO", "HABIDO",
                        "Av. Siempre Viva 123", "Lima", "Lima", "Miraflores", "150122", false, false, true)
        ));

        var command = new CreateCustomerCommand(
                userId, true, null, "EMPRESA SAC", new Document(DocumentType.RUC, "20123456789"), phone
        );

        assertThatThrownBy(() -> service.handle(command))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("shared.error.identity.rucInactive");
    }

    @Test
    @DisplayName("creating corporate customer with not-habido RUC throws rucNotHabido")
    void createCustomerCorporateWithNotHabidoRucThrowsNotHabido() {
        when(customerRepository.existsByUserId(userId)).thenReturn(false);
        when(identityValidationService.validateRuc("20123456789")).thenReturn(Optional.of(
                new CompanyIdentityResult("20123456789", "EMPRESA SAC", "ACTIVO", "NO HABIDO",
                        "Av. Siempre Viva 123", "Lima", "Lima", "Miraflores", "150122", true, false, false)
        ));

        var command = new CreateCustomerCommand(
                userId, true, null, "EMPRESA SAC", new Document(DocumentType.RUC, "20123456789"), phone
        );

        assertThatThrownBy(() -> service.handle(command))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("shared.error.identity.rucNotHabido");
    }
}
