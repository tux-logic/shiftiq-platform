package com.tuxlogic.shiftiq.platform.iam.interfaces.rest;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.tuxlogic.shiftiq.platform.iam.application.commandservices.OtpCommandService;
import com.tuxlogic.shiftiq.platform.iam.application.commandservices.PasswordRecoveryCommandService;
import com.tuxlogic.shiftiq.platform.iam.application.commandservices.SessionCommandService;
import com.tuxlogic.shiftiq.platform.iam.application.commandservices.UserCommandService;
import com.tuxlogic.shiftiq.platform.iam.domain.model.commands.SendOtpEmailCommand;
import com.tuxlogic.shiftiq.platform.iam.domain.model.commands.VerifyOtpCodeCommand;
import com.tuxlogic.shiftiq.platform.iam.interfaces.rest.resources.SendOtpResource;
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
}
