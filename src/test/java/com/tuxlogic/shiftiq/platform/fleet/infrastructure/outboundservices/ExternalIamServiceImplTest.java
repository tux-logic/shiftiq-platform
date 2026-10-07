package com.tuxlogic.shiftiq.platform.fleet.infrastructure.outboundservices;

import com.tuxlogic.shiftiq.platform.iam.application.commandservices.UserCommandService;
import com.tuxlogic.shiftiq.platform.iam.domain.model.aggregates.User;
import com.tuxlogic.shiftiq.platform.iam.domain.model.commands.AssignRoleToUserCommand;
import com.tuxlogic.shiftiq.platform.iam.domain.model.commands.SetUserBranchesCommand;
import com.tuxlogic.shiftiq.platform.iam.domain.model.valueobjects.Roles;
import com.tuxlogic.shiftiq.platform.iam.domain.model.valueobjects.UserId;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ExternalIamServiceImplTest {

    @Mock
    private UserCommandService userCommandService;

    private ExternalIamServiceImpl service;

    private final UUID userId = UUID.randomUUID();
    private final UUID branchId = UUID.randomUUID();

    @BeforeEach
    void setUp() {
        service = new ExternalIamServiceImpl(userCommandService);
    }

    @Test
    @DisplayName("assignRole delegates the normalized role to the IAM command service")
    void assignRoleDelegatesToIam() {
        var user = Mockito.mock(User.class);
        when(user.getRole()).thenReturn(Roles.ROLE_ASSISTANT);
        when(userCommandService.handle(any(AssignRoleToUserCommand.class))).thenReturn(Optional.of(user));

        assertThat(service.assignRole(userId, "assistant")).isTrue();

        var captor = ArgumentCaptor.forClass(AssignRoleToUserCommand.class);
        verify(userCommandService).handle(captor.capture());
        assertThat(captor.getValue().userId()).isEqualTo(new UserId(userId));
        assertThat(captor.getValue().role()).isEqualTo(Roles.ROLE_ASSISTANT);
    }

    @Test
    @DisplayName("assignRole reports false when the IAM service rejects the downgrade")
    void assignRoleReturnsFalseOnRejectedDowngrade() {
        when(userCommandService.handle(any(AssignRoleToUserCommand.class))).thenReturn(Optional.empty());

        assertThat(service.assignRole(userId, "ROLE_EMPLOYEE")).isFalse();
    }

    @Test
    @DisplayName("unknown roles and null users never reach IAM")
    void unknownRoleAndNullUserAreIgnored() {
        assertThat(service.assignRole(userId, "ROLE_HACKER")).isFalse();
        assertThat(service.assignRole(null, "ROLE_EMPLOYEE")).isFalse();

        verifyNoInteractions(userCommandService);
    }

    @Test
    @DisplayName("setBranches replaces the branch membership of the account")
    void setBranchesDelegatesToIam() {
        service.setBranches(userId, Set.of(branchId));

        var captor = ArgumentCaptor.forClass(SetUserBranchesCommand.class);
        verify(userCommandService).handle(captor.capture());
        assertThat(captor.getValue().userId()).isEqualTo(new UserId(userId));
        assertThat(captor.getValue().branchIds()).containsExactly(branchId);
    }

    @Test
    @DisplayName("setBranches clears membership with an empty set and ignores null users")
    void setBranchesClearsMembership() {
        service.setBranches(userId, Set.of());
        service.setBranches(null, Set.of(branchId));

        var captor = ArgumentCaptor.forClass(SetUserBranchesCommand.class);
        verify(userCommandService).handle(captor.capture());
        assertThat(captor.getValue().branchIds()).isEmpty();
    }

    @Test
    @DisplayName("IAM failures do not propagate to the caller")
    void iamFailuresAreSwallowed() {
        when(userCommandService.handle(any(AssignRoleToUserCommand.class)))
                .thenThrow(new IllegalArgumentException("iam.error.userId.required"));
        when(userCommandService.handle(any(SetUserBranchesCommand.class)))
                .thenThrow(new IllegalArgumentException("iam.error.userId.required"));

        assertThat(service.assignRole(userId, "ROLE_EMPLOYEE")).isFalse();
        service.setBranches(userId, Set.of(branchId));
    }
}
