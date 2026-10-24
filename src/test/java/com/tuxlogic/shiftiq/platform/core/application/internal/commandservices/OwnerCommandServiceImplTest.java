package com.tuxlogic.shiftiq.platform.core.application.internal.commandservices;

import com.tuxlogic.shiftiq.platform.core.domain.model.aggregates.Owner;
import com.tuxlogic.shiftiq.platform.core.domain.model.commands.CreateOwnerCommand;
import com.tuxlogic.shiftiq.platform.core.domain.model.valueobjects.Document;
import com.tuxlogic.shiftiq.platform.core.domain.model.valueobjects.DocumentType;
import com.tuxlogic.shiftiq.platform.core.domain.model.valueobjects.PersonName;
import com.tuxlogic.shiftiq.platform.core.domain.model.valueobjects.Phone;
import com.tuxlogic.shiftiq.platform.core.domain.model.valueobjects.UserId;
import com.tuxlogic.shiftiq.platform.core.domain.repositories.OwnerRepository;
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
class OwnerCommandServiceImplTest {

    @Mock
    private OwnerRepository ownerRepository;

    @Mock
    private IdentityValidationService identityValidationService;

    private OwnerCommandServiceImpl service;

    private final UserId userId = new UserId(UUID.randomUUID());
    private final PersonName personName = new PersonName("Roberto", "Mendoza");
    private final Phone phone = new Phone("987123456");

    @BeforeEach
    void setUp() {
        service = new OwnerCommandServiceImpl(ownerRepository, identityValidationService);
    }

    @Test
    @DisplayName("creating owner with valid DNI succeeds")
    void createOwnerWithValidDniSuccess() {
        when(ownerRepository.existsByUserId(userId)).thenReturn(false);
        when(identityValidationService.validateDni("12345678")).thenReturn(Optional.of(
                new PersonIdentityResult("DNI", "12345678", "ROBERTO", "MENDOZA", "ROBERTO MENDOZA",
                        "Av. Benavides 789", "Lima", "Lima", "Miraflores", "2", true)
        ));
        when(ownerRepository.save(any(Owner.class))).thenAnswer(inv -> inv.getArgument(0));

        var command = new CreateOwnerCommand(userId, personName, new Document(DocumentType.DNI, "12345678"), phone);

        var result = service.handle(command);
        assertThat(result).isPresent();
        verify(ownerRepository).save(any(Owner.class));
    }

    @Test
    @DisplayName("creating owner with valid CE succeeds")
    void createOwnerWithValidCeSuccess() {
        when(ownerRepository.existsByUserId(userId)).thenReturn(false);
        when(identityValidationService.validateCe("001234567")).thenReturn(Optional.of(
                new PersonIdentityResult("CE", "001234567", "ROBERTO", "MENDOZA", "ROBERTO MENDOZA",
                        null, null, null, null, null, true)
        ));
        when(ownerRepository.save(any(Owner.class))).thenAnswer(inv -> inv.getArgument(0));

        var command = new CreateOwnerCommand(userId, personName, new Document(DocumentType.CE, "001234567"), phone);

        var result = service.handle(command);
        assertThat(result).isPresent();
        verify(ownerRepository).save(any(Owner.class));
    }

    @Test
    @DisplayName("creating owner with valid RUC succeeds")
    void createOwnerWithValidRucSuccess() {
        when(ownerRepository.existsByUserId(userId)).thenReturn(false);
        when(identityValidationService.validateRuc("10123456781")).thenReturn(Optional.of(
                new CompanyIdentityResult("10123456781", "ROBERTO MENDOZA", "ACTIVO", "HABIDO",
                        "Av. Benavides 789", "Lima", "Lima", "Miraflores", "150122", true, true, true)
        ));
        when(ownerRepository.save(any(Owner.class))).thenAnswer(inv -> inv.getArgument(0));

        var command = new CreateOwnerCommand(userId, personName, new Document(DocumentType.RUC, "10123456781"), phone);

        var result = service.handle(command);
        assertThat(result).isPresent();
        verify(ownerRepository).save(any(Owner.class));
    }

    @Test
    @DisplayName("creating owner with unverified DNI throws notFound")
    void createOwnerWithNotFoundDniThrowsNotFound() {
        when(ownerRepository.existsByUserId(userId)).thenReturn(false);
        when(identityValidationService.validateDni("12345678")).thenReturn(Optional.empty());

        var command = new CreateOwnerCommand(userId, personName, new Document(DocumentType.DNI, "12345678"), phone);

        assertThatThrownBy(() -> service.handle(command))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("shared.error.identity.notFound");
    }

    @Test
    @DisplayName("creating owner with inactive RUC throws rucInactive")
    void createOwnerWithInactiveRucThrowsInactive() {
        when(ownerRepository.existsByUserId(userId)).thenReturn(false);
        when(identityValidationService.validateRuc("10123456781")).thenReturn(Optional.of(
                new CompanyIdentityResult("10123456781", "ROBERTO MENDOZA", "BAJA", "HABIDO",
                        "Av. Benavides 789", "Lima", "Lima", "Miraflores", "150122", false, true, true)
        ));

        var command = new CreateOwnerCommand(userId, personName, new Document(DocumentType.RUC, "10123456781"), phone);

        assertThatThrownBy(() -> service.handle(command))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("shared.error.identity.rucInactive");
    }
}
