package com.tuxlogic.shiftiq.platform.iam.interfaces.rest;

import com.tuxlogic.shiftiq.platform.iam.application.commandservices.UserCommandService;
import com.tuxlogic.shiftiq.platform.iam.application.queryservices.UserQueryService;
import com.tuxlogic.shiftiq.platform.iam.domain.model.aggregates.User;
import com.tuxlogic.shiftiq.platform.iam.domain.model.queries.GetUserByEmailQuery;
import com.tuxlogic.shiftiq.platform.iam.domain.model.valueobjects.EmailAddress;
import com.tuxlogic.shiftiq.platform.iam.domain.model.valueobjects.Roles;
import com.tuxlogic.shiftiq.platform.iam.domain.model.valueobjects.UserId;
import com.tuxlogic.shiftiq.platform.shared.infrastructure.security.UserSecurityService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UsersControllerTest {

    @Mock
    private UserCommandService userCommandService;

    @Mock
    private UserQueryService userQueryService;

    @Mock
    private UserSecurityService userSecurityService;

    private UsersController controller;

    @BeforeEach
    void setUp() {
        controller = new UsersController(userCommandService, userQueryService, userSecurityService);
    }

    @Test
    void getUserByEmail_WhenForbidden_ShouldReturnForbiddenStatus() {
        String email = "otheruser@example.com";
        User user = mock(User.class);
        UUID userId = UUID.randomUUID();
        when(user.getId()).thenReturn(new UserId(userId));

        when(userQueryService.handle(any(GetUserByEmailQuery.class))).thenReturn(Optional.of(user));
        when(userSecurityService.isCurrentUser(userId)).thenReturn(false);

        ResponseEntity<?> response = controller.getUserByEmail(email);

        assertNotNull(response);
        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
    }

    @Test
    void getUserByEmail_WhenCurrentUser_ShouldReturnOk() {
        String email = "currentuser@example.com";
        User user = mock(User.class);
        UUID userId = UUID.randomUUID();
        when(user.getId()).thenReturn(new UserId(userId));
        when(user.getEmail()).thenReturn(new EmailAddress(email));
        when(user.getRole()).thenReturn(Roles.ROLE_USER);

        when(userQueryService.handle(any(GetUserByEmailQuery.class))).thenReturn(Optional.of(user));
        when(userSecurityService.isCurrentUser(userId)).thenReturn(true);

        ResponseEntity<?> response = controller.getUserByEmail(email);

        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
    }
}
