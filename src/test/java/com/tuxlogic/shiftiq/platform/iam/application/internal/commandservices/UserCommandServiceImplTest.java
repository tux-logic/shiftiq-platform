package com.tuxlogic.shiftiq.platform.iam.application.internal.commandservices;

import com.tuxlogic.shiftiq.platform.iam.application.internal.outboundservices.email.EmailVerificationResult;
import com.tuxlogic.shiftiq.platform.iam.application.internal.outboundservices.email.EmailVerificationService;
import com.tuxlogic.shiftiq.platform.iam.application.internal.outboundservices.hashing.HashingService;
import com.tuxlogic.shiftiq.platform.iam.application.internal.services.SessionIssuer;
import com.tuxlogic.shiftiq.platform.iam.domain.model.aggregates.User;
import com.tuxlogic.shiftiq.platform.iam.domain.model.commands.SignInCommand;
import com.tuxlogic.shiftiq.platform.iam.domain.model.commands.SignUpCommand;
import com.tuxlogic.shiftiq.platform.iam.domain.model.commands.UpdateUserEmailCommand;
import com.tuxlogic.shiftiq.platform.iam.domain.model.queries.AuthenticatedUser;
import com.tuxlogic.shiftiq.platform.iam.domain.model.valueobjects.EmailAddress;
import com.tuxlogic.shiftiq.platform.iam.domain.model.valueobjects.Password;
import com.tuxlogic.shiftiq.platform.iam.domain.model.valueobjects.UserId;
import com.tuxlogic.shiftiq.platform.iam.domain.repositories.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserCommandServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private HashingService hashingService;

    @Mock
    private SessionIssuer sessionIssuer;

    @Mock
    private EmailVerificationService emailVerificationService;

    private UserCommandServiceImpl userCommandService;

    @BeforeEach
    void setUp() {
        userCommandService = new UserCommandServiceImpl(
                userRepository,
                hashingService,
                sessionIssuer,
                emailVerificationService,
                "test-client-id"
        );
    }

    @Test
    void handleSignUp_WhenEmailIsValid_ShouldCreateAndReturnUser() {
        var command = new SignUpCommand(new EmailAddress("valid@example.com"), new Password("Password123!"));

        when(userRepository.existsByEmail("valid@example.com")).thenReturn(false);
        when(emailVerificationService.verifyEmail("valid@example.com"))
                .thenReturn(EmailVerificationResult.ofValid("valid", "permitted"));
        when(hashingService.encode("Password123!")).thenReturn("hashed-pwd");

        User savedUser = new User(command.email(), new Password("hashed-pwd"));
        when(userRepository.findByEmail("valid@example.com")).thenReturn(Optional.of(savedUser));

        Optional<User> result = userCommandService.handle(command);

        assertTrue(result.isPresent());
        assertEquals("valid@example.com", result.get().getEmail().value());
        verify(userRepository).save(any(User.class));
    }

    @Test
    void handleSignUp_WhenEmailAlreadyInUse_ShouldThrowIllegalArgumentException() {
        var command = new SignUpCommand(new EmailAddress("taken@example.com"), new Password("Password123!"));
        when(userRepository.existsByEmail("taken@example.com")).thenReturn(true);

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () ->
                userCommandService.handle(command)
        );

        assertEquals("iam.error.email.alreadyInUse", ex.getMessage());
        verifyNoInteractions(emailVerificationService);
        verify(userRepository, never()).save(any());
    }

    @Test
    void handleSignUp_WhenEmailIsInvalidAccordingToProvider_ShouldThrowInvalidAddress() {
        var command = new SignUpCommand(new EmailAddress("invalid@fake.com"), new Password("Password123!"));

        when(userRepository.existsByEmail("invalid@fake.com")).thenReturn(false);
        when(emailVerificationService.verifyEmail("invalid@fake.com"))
                .thenReturn(EmailVerificationResult.ofInvalid("invalid", "mailbox_not_found", "Invalid"));

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () ->
                userCommandService.handle(command)
        );

        assertEquals("iam.error.email.invalidAddress", ex.getMessage());
        verify(userRepository, never()).save(any());
    }

    @Test
    void handleSignUp_WhenEmailVerificationFailsWithCommunicationError_ShouldThrowUnverifiable() {
        var command = new SignUpCommand(new EmailAddress("timeout@service.com"), new Password("Password123!"));

        when(userRepository.existsByEmail("timeout@service.com")).thenReturn(false);
        when(emailVerificationService.verifyEmail("timeout@service.com"))
                .thenReturn(EmailVerificationResult.ofInvalid("error", "communication_failure", "Timeout"));

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () ->
                userCommandService.handle(command)
        );

        assertEquals("iam.error.email.unverifiable", ex.getMessage());
        verify(userRepository, never()).save(any());
    }

    @Test
    void handleUpdateEmail_WhenNewEmailIsValid_ShouldUpdateAndReturnSession() {
        UUID userId = UUID.randomUUID();
        User existingUser = new User(new EmailAddress("old@example.com"), new Password("hashed-pwd"));
        var command = new UpdateUserEmailCommand(new UserId(userId), new EmailAddress("new@example.com"));

        when(userRepository.findById(userId)).thenReturn(Optional.of(existingUser));
        when(userRepository.existsByEmail("new@example.com")).thenReturn(false);
        when(emailVerificationService.verifyEmail("new@example.com"))
                .thenReturn(EmailVerificationResult.ofValid("valid", "permitted"));
        when(sessionIssuer.issue(existingUser)).thenReturn(mock(AuthenticatedUser.class));

        Optional<AuthenticatedUser> result = userCommandService.handle(command);

        assertTrue(result.isPresent());
        assertEquals("new@example.com", existingUser.getEmail().value());
        verify(userRepository).save(existingUser);
    }

    @Test
    void handleUpdateEmail_WhenNewEmailIsInvalid_ShouldThrowInvalidAddress() {
        UUID userId = UUID.randomUUID();
        User existingUser = new User(new EmailAddress("old@example.com"), new Password("hashed-pwd"));
        var command = new UpdateUserEmailCommand(new UserId(userId), new EmailAddress("bad@fake.com"));

        when(userRepository.findById(userId)).thenReturn(Optional.of(existingUser));
        when(userRepository.existsByEmail("bad@fake.com")).thenReturn(false);
        when(emailVerificationService.verifyEmail("bad@fake.com"))
                .thenReturn(EmailVerificationResult.ofInvalid("invalid", "domain_not_found", "Domain not found"));

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () ->
                userCommandService.handle(command)
        );

        assertEquals("iam.error.email.invalidAddress", ex.getMessage());
        verify(userRepository, never()).save(any());
    }

    @Test
    void handleSignIn_WhenUserNotFound_ShouldReturnEmpty() {
        var command = new SignInCommand(new EmailAddress("nonexistent@example.com"), new Password("Password123!"));
        when(userRepository.findByEmail("nonexistent@example.com")).thenReturn(Optional.empty());

        Optional<AuthenticatedUser> result = userCommandService.handle(command);

        assertTrue(result.isEmpty());
        verify(hashingService, never()).matches(any(), any());
        verifyNoInteractions(sessionIssuer);
    }

    @Test
    void handleSignIn_WhenPasswordDoesNotMatch_ShouldReturnEmpty() {
        var command = new SignInCommand(new EmailAddress("user@example.com"), new Password("WrongPassword!"));
        User existingUser = new User(new EmailAddress("user@example.com"), new Password("hashed-pwd"));

        when(userRepository.findByEmail("user@example.com")).thenReturn(Optional.of(existingUser));
        when(hashingService.matches("WrongPassword!", "hashed-pwd")).thenReturn(false);

        Optional<AuthenticatedUser> result = userCommandService.handle(command);

        assertTrue(result.isEmpty());
        verifyNoInteractions(sessionIssuer);
    }

    @Test
    void handleSignIn_WhenCredentialsAreValid_ShouldReturnAuthenticatedUser() {
        var command = new SignInCommand(new EmailAddress("user@example.com"), new Password("Password123!"));
        User existingUser = new User(new EmailAddress("user@example.com"), new Password("hashed-pwd"));
        AuthenticatedUser mockSession = mock(AuthenticatedUser.class);

        when(userRepository.findByEmail("user@example.com")).thenReturn(Optional.of(existingUser));
        when(hashingService.matches("Password123!", "hashed-pwd")).thenReturn(true);
        when(sessionIssuer.issue(existingUser)).thenReturn(mockSession);

        Optional<AuthenticatedUser> result = userCommandService.handle(command);

        assertTrue(result.isPresent());
        assertSame(mockSession, result.get());
    }
}
