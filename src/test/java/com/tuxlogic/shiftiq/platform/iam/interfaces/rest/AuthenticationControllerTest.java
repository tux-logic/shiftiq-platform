package com.tuxlogic.shiftiq.platform.iam.interfaces.rest;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.tuxlogic.shiftiq.platform.iam.application.commandservices.OtpCommandService;
import com.tuxlogic.shiftiq.platform.iam.application.commandservices.PasswordRecoveryCommandService;
import com.tuxlogic.shiftiq.platform.iam.application.commandservices.SessionCommandService;
import com.tuxlogic.shiftiq.platform.iam.application.commandservices.UserCommandService;
import com.tuxlogic.shiftiq.platform.iam.domain.model.aggregates.User;
import com.tuxlogic.shiftiq.platform.iam.domain.model.commands.SendOtpEmailCommand;
import com.tuxlogic.shiftiq.platform.iam.domain.model.commands.SignInCommand;
import com.tuxlogic.shiftiq.platform.iam.domain.model.commands.VerifyOtpCodeCommand;
import com.tuxlogic.shiftiq.platform.iam.domain.model.queries.AuthenticatedUser;
import com.tuxlogic.shiftiq.platform.iam.domain.model.valueobjects.EmailAddress;
import com.tuxlogic.shiftiq.platform.iam.domain.model.valueobjects.Password;
import com.tuxlogic.shiftiq.platform.iam.interfaces.rest.resources.SendOtpResource;
import com.tuxlogic.shiftiq.platform.iam.interfaces.rest.resources.SignInResource;
import com.tuxlogic.shiftiq.platform.iam.interfaces.rest.resources.VerifyOtpResource;
import com.tuxlogic.shiftiq.platform.shared.interfaces.rest.GlobalExceptionHandler;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class AuthenticationControllerTest {

    @Mock
    private UserCommandService userCommandService;

    @Mock
    private PasswordRecoveryCommandService passwordRecoveryCommandService;

    @Mock
    private SessionCommandService sessionCommandService;

    @Mock
    private OtpCommandService otpCommandService;

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        var controller = new AuthenticationController(
                userCommandService,
                passwordRecoveryCommandService,
                sessionCommandService,
                otpCommandService
        );
        this.mockMvc = MockMvcBuilders.standaloneSetup(controller)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
        this.objectMapper = new ObjectMapper();
    }

    @Test
    @DisplayName("POST /otp/send with valid email returns 200 OK")
    void sendOtp_WithValidEmail_ReturnsOk() throws Exception {
        var resource = new SendOtpResource("user@example.com");

        doNothing().when(otpCommandService).handle(any(SendOtpEmailCommand.class));

        mockMvc.perform(post("/api/v1/authentication/otp/send")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(resource)))
                .andExpect(status().isOk());

        verify(otpCommandService, times(1)).handle(any(SendOtpEmailCommand.class));
    }

    @Test
    @DisplayName("POST /otp/send with invalid email format returns 400 Bad Request")
    void sendOtp_WithInvalidEmail_ReturnsBadRequest() throws Exception {
        var resource = new SendOtpResource("not-an-email");

        mockMvc.perform(post("/api/v1/authentication/otp/send")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(resource)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("VALIDATION_ERROR"));

        verifyNoInteractions(otpCommandService);
    }

    @Test
    @DisplayName("POST /otp/send with blank email returns 400 Bad Request")
    void sendOtp_WithBlankEmail_ReturnsBadRequest() throws Exception {
        var resource = new SendOtpResource("");

        mockMvc.perform(post("/api/v1/authentication/otp/send")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(resource)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("VALIDATION_ERROR"));

        verifyNoInteractions(otpCommandService);
    }

    @Test
    @DisplayName("POST /otp/send when cooldown active returns 400 Bad Request")
    void sendOtp_WhenCooldownActive_ReturnsBadRequest() throws Exception {
        var resource = new SendOtpResource("user@example.com");

        doThrow(new IllegalArgumentException("iam.error.otp.cooldownActive"))
                .when(otpCommandService).handle(any(SendOtpEmailCommand.class));

        mockMvc.perform(post("/api/v1/authentication/otp/send")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(resource)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("VALIDATION_ERROR"));

        verify(otpCommandService, times(1)).handle(any(SendOtpEmailCommand.class));
    }

    @Test
    @DisplayName("POST /otp/send when dispatch fails returns 400 Bad Request")
    void sendOtp_WhenDispatchFails_ReturnsBadRequest() throws Exception {
        var resource = new SendOtpResource("user@example.com");

        doThrow(new IllegalStateException("iam.error.otp.sendFailed"))
                .when(otpCommandService).handle(any(SendOtpEmailCommand.class));

        mockMvc.perform(post("/api/v1/authentication/otp/send")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(resource)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("VALIDATION_ERROR"));

        verify(otpCommandService, times(1)).handle(any(SendOtpEmailCommand.class));
    }

    @Test
    @DisplayName("POST /otp/verify with valid 6-digit code returns 200 OK")
    void verifyOtp_WithValidCode_ReturnsOk() throws Exception {
        var resource = new VerifyOtpResource("user@example.com", "123456");

        when(otpCommandService.handle(any(VerifyOtpCodeCommand.class))).thenReturn(true);

        mockMvc.perform(post("/api/v1/authentication/otp/verify")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(resource)))
                .andExpect(status().isOk());

        verify(otpCommandService, times(1)).handle(any(VerifyOtpCodeCommand.class));
    }

    @Test
    @DisplayName("POST /otp/verify with non-numeric code returns 400 Bad Request")
    void verifyOtp_WithNonNumericCode_ReturnsBadRequest() throws Exception {
        var resource = new VerifyOtpResource("user@example.com", "abcdef");

        mockMvc.perform(post("/api/v1/authentication/otp/verify")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(resource)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("VALIDATION_ERROR"));

        verifyNoInteractions(otpCommandService);
    }

    @Test
    @DisplayName("POST /otp/verify with code not 6 digits returns 400 Bad Request")
    void verifyOtp_WithInvalidLengthCode_ReturnsBadRequest() throws Exception {
        var resource = new VerifyOtpResource("user@example.com", "12345");

        mockMvc.perform(post("/api/v1/authentication/otp/verify")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(resource)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("VALIDATION_ERROR"));

        verifyNoInteractions(otpCommandService);
    }

    @Test
    @DisplayName("POST /otp/verify when code is invalid returns 400 Bad Request")
    void verifyOtp_WhenCodeInvalid_ReturnsBadRequest() throws Exception {
        var resource = new VerifyOtpResource("user@example.com", "999999");

        doThrow(new IllegalArgumentException("iam.error.otp.invalid"))
                .when(otpCommandService).handle(any(VerifyOtpCodeCommand.class));

        mockMvc.perform(post("/api/v1/authentication/otp/verify")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(resource)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("VALIDATION_ERROR"));

        verify(otpCommandService, times(1)).handle(any(VerifyOtpCodeCommand.class));
    }

    @Test
    @DisplayName("POST /otp/verify when code is expired returns 400 Bad Request")
    void verifyOtp_WhenCodeExpired_ReturnsBadRequest() throws Exception {
        var resource = new VerifyOtpResource("user@example.com", "123456");

        doThrow(new IllegalArgumentException("iam.error.otp.expired"))
                .when(otpCommandService).handle(any(VerifyOtpCodeCommand.class));

        mockMvc.perform(post("/api/v1/authentication/otp/verify")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(resource)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("VALIDATION_ERROR"));

        verify(otpCommandService, times(1)).handle(any(VerifyOtpCodeCommand.class));
    }

    @Test
    @DisplayName("POST /sessions with valid credentials returns 200 OK and token resource")
    void signIn_WithValidCredentials_ReturnsOk() throws Exception {
        var resource = new SignInResource("user@example.com", "Password123!");
        User mockUser = new User(new EmailAddress("user@example.com"), new Password("hashed-pwd"));
        var authenticatedUser = new AuthenticatedUser(mockUser, "access-token-xyz", "refresh-token-abc", 900L);

        when(userCommandService.handle(any(SignInCommand.class))).thenReturn(Optional.of(authenticatedUser));

        mockMvc.perform(post("/api/v1/authentication/sessions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(resource)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value("user@example.com"))
                .andExpect(jsonPath("$.token").value("access-token-xyz"))
                .andExpect(jsonPath("$.refreshToken").value("refresh-token-abc"))
                .andExpect(jsonPath("$.accessTokenExpiresInSeconds").value(900));

        verify(userCommandService, times(1)).handle(any(SignInCommand.class));
    }

    @Test
    @DisplayName("POST /sessions with invalid credentials returns 401 Unauthorized")
    void signIn_WithInvalidCredentials_ReturnsUnauthorized() throws Exception {
        var resource = new SignInResource("user@example.com", "WrongPassword!");

        when(userCommandService.handle(any(SignInCommand.class))).thenReturn(Optional.empty());

        mockMvc.perform(post("/api/v1/authentication/sessions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(resource)))
                .andExpect(status().isUnauthorized());

        verify(userCommandService, times(1)).handle(any(SignInCommand.class));
    }

    @Test
    @DisplayName("POST /login alias with valid credentials returns 200 OK")
    void loginAlias_WithValidCredentials_ReturnsOk() throws Exception {
        var resource = new SignInResource("user@example.com", "Password123!");
        User mockUser = new User(new EmailAddress("user@example.com"), new Password("hashed-pwd"));
        var authenticatedUser = new AuthenticatedUser(mockUser, "access-token-xyz", "refresh-token-abc", 900L);

        when(userCommandService.handle(any(SignInCommand.class))).thenReturn(Optional.of(authenticatedUser));

        mockMvc.perform(post("/api/v1/authentication/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(resource)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").value("access-token-xyz"));

        verify(userCommandService, times(1)).handle(any(SignInCommand.class));
    }

    @Test
    @DisplayName("POST /login alias with invalid credentials returns 401 Unauthorized")
    void loginAlias_WithInvalidCredentials_ReturnsUnauthorized() throws Exception {
        var resource = new SignInResource("user@example.com", "WrongPassword!");

        when(userCommandService.handle(any(SignInCommand.class))).thenReturn(Optional.empty());

        mockMvc.perform(post("/api/v1/authentication/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(resource)))
                .andExpect(status().isUnauthorized());

        verify(userCommandService, times(1)).handle(any(SignInCommand.class));
    }

    @Test
    @DisplayName("POST /sessions with malformed email returns 400 Bad Request")
    void signIn_WithMalformedEmail_ReturnsBadRequest() throws Exception {
        var resource = new SignInResource("not-an-email", "Password123!");

        mockMvc.perform(post("/api/v1/authentication/sessions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(resource)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("VALIDATION_ERROR"));

        verifyNoInteractions(userCommandService);
    }
}
