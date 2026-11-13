package com.tuxlogic.shiftiq.platform.iam.infrastructure.authorization.sfs.model;

import com.tuxlogic.shiftiq.platform.iam.domain.model.aggregates.User;
import com.tuxlogic.shiftiq.platform.iam.domain.model.aggregates.WorkshopRole;
import com.tuxlogic.shiftiq.platform.iam.domain.model.valueobjects.*;
import com.tuxlogic.shiftiq.platform.iam.domain.repositories.UserRepository;
import com.tuxlogic.shiftiq.platform.iam.domain.repositories.WorkshopRoleRepository;
import com.tuxlogic.shiftiq.platform.iam.infrastructure.authorization.sfs.services.UserDetailsServiceImpl;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserDetailsImplTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private WorkshopRoleRepository workshopRoleRepository;

    @Test
    @DisplayName("UserDetailsImpl.build(user) exposes default role permissions when workshopRoleId is absent")
    void buildUsesDefaultRolePermissions() {
        User user = new User(
                new UserId(UUID.randomUUID()),
                new EmailAddress("advisor@workshop.com"),
                new Password("hashed"),
                null,
                UserStatus.ACTIVE,
                Roles.ROLE_ASSISTANT,
                Set.of(UUID.randomUUID()),
                Instant.now(),
                Instant.now(),
                null,
                0L
        );

        UserDetailsImpl details = UserDetailsImpl.build(user);
        assertThat(details.hasRole("ROLE_ASSISTANT")).isTrue();
        assertThat(details.hasPermission("QUOTE_APPROVE")).isTrue();
        assertThat(details.hasPermission("QUOTE_CANCEL")).isFalse();
        assertThat(details.getPermissions()).contains("QUOTE_APPROVE", "CHECKOUT_PROCESS");
    }

    @Test
    @DisplayName("UserDetailsServiceImpl loads custom workshop role permissions when workshopRoleId is present")
    void userDetailsServiceLoadsCustomWorkshopRolePermissions() {
        UUID workshopId = UUID.randomUUID();
        UUID customRoleId = UUID.randomUUID();
        User user = new User(
                new UserId(UUID.randomUUID()),
                new EmailAddress("cashier@dealership.com"),
                new Password("hashed"),
                null,
                UserStatus.ACTIVE,
                Roles.ROLE_EMPLOYEE,
                Set.of(UUID.randomUUID()),
                Instant.now(),
                Instant.now(),
                null,
                0L
        );
        user.assignWorkshopRole(customRoleId);

        WorkshopRole customRole = new WorkshopRole(
                customRoleId,
                workshopId,
                "Cashier",
                "Dedicated cashier",
                false,
                true,
                Set.of(Permission.VOUCHER_READ, Permission.CHECKOUT_PROCESS),
                Instant.now(),
                Instant.now()
        );

        when(userRepository.findByEmail("cashier@dealership.com")).thenReturn(Optional.of(user));
        when(workshopRoleRepository.findById(customRoleId)).thenReturn(Optional.of(customRole));

        var service = new UserDetailsServiceImpl(userRepository, workshopRoleRepository);
        UserDetailsImpl details = (UserDetailsImpl) service.loadUserByUsername("cashier@dealership.com");

        assertThat(details.hasPermission("CHECKOUT_PROCESS")).isTrue();
        assertThat(details.hasPermission("VOUCHER_READ")).isTrue();
        assertThat(details.hasPermission("WORKORDER_EXECUTE")).isFalse();
    }
}
