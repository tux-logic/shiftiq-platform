package com.tuxlogic.shiftiq.platform.core.application.internal.commandservices;

import com.tuxlogic.shiftiq.platform.core.application.queryservices.ProfileQueryService;
import com.tuxlogic.shiftiq.platform.core.domain.model.aggregates.Customer;
import com.tuxlogic.shiftiq.platform.core.domain.model.aggregates.Employee;
import com.tuxlogic.shiftiq.platform.core.domain.model.aggregates.Owner;
import com.tuxlogic.shiftiq.platform.core.domain.model.valueobjects.*;
import com.tuxlogic.shiftiq.platform.core.domain.repositories.CustomerRepository;
import com.tuxlogic.shiftiq.platform.core.domain.repositories.EmployeeRepository;
import com.tuxlogic.shiftiq.platform.core.domain.repositories.OwnerRepository;
import com.tuxlogic.shiftiq.platform.core.interfaces.rest.resources.UserProfileContextResource;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProfileCommandServiceImplTest {

    @Mock
    private OwnerRepository ownerRepository;

    @Mock
    private EmployeeRepository employeeRepository;

    @Mock
    private CustomerRepository customerRepository;

    @Mock
    private ProfileQueryService profileQueryService;

    private ProfileCommandServiceImpl service;

    @BeforeEach
    void setUp() {
        service = new ProfileCommandServiceImpl(ownerRepository, employeeRepository, customerRepository, profileQueryService);
    }

    @Test
    void updateProfileImage_WhenProfilesExist_ShouldUpdateAllMatchingProfilesAndReturnContext() {
        UUID rawUserId = UUID.randomUUID();
        UserId userId = new UserId(rawUserId);
        String imageUrl = "https://res.cloudinary.com/demo/image/upload/v1/avatar.jpg";

        Owner owner = new Owner(userId, new PersonName("Aldo", "Machacca"), new Document("DNI", "70514079"), new Phone("+51987654321"));
        Employee employee = new Employee(userId, new PersonName("Aldo", "Machacca"), new Document("DNI", "70514079"), new Phone("+51987654321"));
        Customer customer = new Customer(userId, false, new PersonName("Aldo", "Machacca"), null, new Document("DNI", "70514079"), new Phone("+51987654321"));

        when(ownerRepository.findByUserId(userId)).thenReturn(Optional.of(owner));
        when(employeeRepository.findByUserId(userId)).thenReturn(Optional.of(employee));
        when(customerRepository.findByUserId(userId)).thenReturn(Optional.of(customer));

        UserProfileContextResource expectedContext = new UserProfileContextResource(
                rawUserId, List.of("OWNER", "EMPLOYEE", "CUSTOMER"), null, null, null);
        when(profileQueryService.getUserProfileContext(userId)).thenReturn(expectedContext);

        UserProfileContextResource result = service.updateProfileImage(userId, "  " + imageUrl + "  ");

        assertSame(expectedContext, result);
        assertEquals(imageUrl, owner.getProfileImageUrl());
        assertEquals(imageUrl, employee.getProfileImageUrl());
        assertEquals(imageUrl, customer.getProfileImageUrl());
        verify(ownerRepository).save(owner);
        verify(employeeRepository).save(employee);
        verify(customerRepository).save(customer);
    }

    @Test
    void updateProfileImage_WhenNoProfileExists_ShouldThrowIllegalArgumentException() {
        UserId userId = new UserId(UUID.randomUUID());
        when(ownerRepository.findByUserId(userId)).thenReturn(Optional.empty());
        when(employeeRepository.findByUserId(userId)).thenReturn(Optional.empty());
        when(customerRepository.findByUserId(userId)).thenReturn(Optional.empty());

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> service.updateProfileImage(userId, "https://example.com/avatar.jpg"));

        assertEquals("core.error.profile.notFound", ex.getMessage());
        verifyNoInteractions(profileQueryService);
    }
}
