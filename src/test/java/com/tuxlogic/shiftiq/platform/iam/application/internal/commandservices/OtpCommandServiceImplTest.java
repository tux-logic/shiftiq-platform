package com.tuxlogic.shiftiq.platform.iam.application.internal.commandservices;

import com.tuxlogic.shiftiq.platform.iam.application.internal.outboundservices.email.EmailVerificationResult;
import com.tuxlogic.shiftiq.platform.iam.application.internal.outboundservices.email.EmailVerificationService;
import com.tuxlogic.shiftiq.platform.iam.application.internal.outboundservices.email.TransactionalEmailService;
import com.tuxlogic.shiftiq.platform.iam.domain.model.commands.SendOtpEmailCommand;
import com.tuxlogic.shiftiq.platform.iam.domain.model.commands.VerifyOtpCodeCommand;
import com.tuxlogic.shiftiq.platform.iam.domain.model.entities.EmailOtpVerification;
import com.tuxlogic.shiftiq.platform.iam.domain.model.valueobjects.EmailAddress;
import com.tuxlogic.shiftiq.platform.iam.domain.repositories.EmailOtpVerificationRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OtpCommandServiceImplTest {

    @Mock
    private EmailOtpVerificationRepository otpRepository;

    @Mock
    private EmailVerificationService emailVerificationService;

    @Mock
    private TransactionalEmailService transactionalEmailService;

    private OtpCommandServiceImpl otpCommandService;

    private static final String TEST_EMAIL = "user@example.com";

    @BeforeEach
    void setUp() {
        otpCommandService = new OtpCommandServiceImpl(
                otpRepository,
                emailVerificationService,
                transactionalEmailService,
                10,
                5,
                60
        );
    }

    @Test
    void handleSendOtp_WhenValidEmail_ShouldIssueOtpAndSendEmail() {
        var command = new SendOtpEmailCommand(new EmailAddress(TEST_EMAIL));

        when(otpRepository.findTopByEmailOrderByCreatedAtDesc(TEST_EMAIL)).thenReturn(Optional.empty());
        when(emailVerificationService.verifyEmail(TEST_EMAIL))
                .thenReturn(EmailVerificationResult.ofValid("valid", "permitted"));
        when(transactionalEmailService.sendEmail(eq(TEST_EMAIL), anyString(), anyString())).thenReturn(true);

        assertDoesNotThrow(() -> otpCommandService.handle(command));

        ArgumentCaptor<EmailOtpVerification> captor = ArgumentCaptor.forClass(EmailOtpVerification.class);
        verify(otpRepository).save(captor.capture());
        EmailOtpVerification saved = captor.getValue();

        assertEquals(TEST_EMAIL, saved.getEmail());
        assertNotNull(saved.getOtpHash());
        assertFalse(saved.isVerified());
        assertFalse(saved.isConsumed());
    }

    @Test
    void handleSendOtp_WhenDeliverabilityFails_ShouldThrowInvalidAddress() {
        var command = new SendOtpEmailCommand(new EmailAddress("bad@fake.com"));

        when(otpRepository.findTopByEmailOrderByCreatedAtDesc("bad@fake.com")).thenReturn(Optional.empty());
        when(emailVerificationService.verifyEmail("bad@fake.com"))
                .thenReturn(EmailVerificationResult.ofInvalid("invalid", "domain_not_found", "Bad domain"));

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () ->
                otpCommandService.handle(command)
        );

        assertEquals("iam.error.email.invalidAddress", ex.getMessage());
        verify(transactionalEmailService, never()).sendEmail(any(), any(), any());
    }

    @Test
    void handleSendOtp_WhenDeliverabilityHasError_ShouldThrowUnverifiable() {
        var command = new SendOtpEmailCommand(new EmailAddress("timeout@service.com"));

        when(otpRepository.findTopByEmailOrderByCreatedAtDesc("timeout@service.com")).thenReturn(Optional.empty());
        when(emailVerificationService.verifyEmail("timeout@service.com"))
                .thenReturn(EmailVerificationResult.ofInvalid("error", "communication_failure", "Timeout"));

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () ->
                otpCommandService.handle(command)
        );

        assertEquals("iam.error.email.unverifiable", ex.getMessage());
        verify(transactionalEmailService, never()).sendEmail(any(), any(), any());
    }

    @Test
    void handleSendOtp_WhenCooldownIsActive_ShouldThrowIllegalStateException() {
        var command = new SendOtpEmailCommand(new EmailAddress(TEST_EMAIL));
        EmailOtpVerification recent = new EmailOtpVerification(TEST_EMAIL, "hash", 10, 5);

        when(otpRepository.findTopByEmailOrderByCreatedAtDesc(TEST_EMAIL)).thenReturn(Optional.of(recent));

        IllegalStateException ex = assertThrows(IllegalStateException.class, () ->
                otpCommandService.handle(command)
        );

        assertEquals("iam.error.otp.cooldownActive", ex.getMessage());
        verifyNoInteractions(emailVerificationService);
        verifyNoInteractions(transactionalEmailService);
    }

    @Test
    void handleSendOtp_WhenBrevoSendFails_ShouldThrowIllegalStateException() {
        var command = new SendOtpEmailCommand(new EmailAddress(TEST_EMAIL));

        when(otpRepository.findTopByEmailOrderByCreatedAtDesc(TEST_EMAIL)).thenReturn(Optional.empty());
        when(emailVerificationService.verifyEmail(TEST_EMAIL))
                .thenReturn(EmailVerificationResult.ofValid("valid", "permitted"));
        when(transactionalEmailService.sendEmail(eq(TEST_EMAIL), anyString(), anyString())).thenReturn(false);

        IllegalStateException ex = assertThrows(IllegalStateException.class, () ->
                otpCommandService.handle(command)
        );

        assertEquals("iam.error.otp.sendFailed", ex.getMessage());
    }

    @Test
    void handleVerifyOtp_WhenCodeMatches_ShouldMarkVerifiedAndReturnTrue() throws Exception {
        String code = "123456";
        String codeHash = sha256(code);
        EmailOtpVerification record = new EmailOtpVerification(TEST_EMAIL, codeHash, 10, 5);

        when(otpRepository.findTopByEmailOrderByCreatedAtDesc(TEST_EMAIL)).thenReturn(Optional.of(record));

        var command = new VerifyOtpCodeCommand(new EmailAddress(TEST_EMAIL), code);
        boolean result = otpCommandService.handle(command);

        assertTrue(result);
        assertTrue(record.isVerified());
        assertFalse(record.isConsumed());
        assertTrue(record.isReadyForConsumption());
        verify(otpRepository).save(record);
    }

    @Test
    void handleVerifyOtp_WhenCodeDoesNotMatch_ShouldIncrementAttemptsAndThrow() throws Exception {
        String codeHash = sha256("123456");
        EmailOtpVerification record = new EmailOtpVerification(TEST_EMAIL, codeHash, 10, 5);

        when(otpRepository.findTopByEmailOrderByCreatedAtDesc(TEST_EMAIL)).thenReturn(Optional.of(record));

        var command = new VerifyOtpCodeCommand(new EmailAddress(TEST_EMAIL), "999999");
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () ->
                otpCommandService.handle(command)
        );

        assertEquals("iam.error.otp.invalid", ex.getMessage());
        assertEquals(1, record.getAttempts());
        assertFalse(record.isVerified());
        verify(otpRepository).save(record);
    }

    @Test
    void handleVerifyOtp_WhenOtpNotFound_ShouldThrowNotFound() {
        when(otpRepository.findTopByEmailOrderByCreatedAtDesc(TEST_EMAIL)).thenReturn(Optional.empty());

        var command = new VerifyOtpCodeCommand(new EmailAddress(TEST_EMAIL), "123456");
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () ->
                otpCommandService.handle(command)
        );

        assertEquals("iam.error.otp.notFound", ex.getMessage());
    }

    @Test
    void handleVerifyOtp_WhenExpired_ShouldThrowExpired() throws Exception {
        String codeHash = sha256("123456");
        EmailOtpVerification expired = new EmailOtpVerification(
                UUID.randomUUID(),
                TEST_EMAIL,
                codeHash,
                Instant.now().minus(20, ChronoUnit.MINUTES),
                Instant.now().minus(10, ChronoUnit.MINUTES),
                0,
                5,
                false,
                false
        );

        when(otpRepository.findTopByEmailOrderByCreatedAtDesc(TEST_EMAIL)).thenReturn(Optional.of(expired));

        var command = new VerifyOtpCodeCommand(new EmailAddress(TEST_EMAIL), "123456");
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () ->
                otpCommandService.handle(command)
        );

        assertEquals("iam.error.otp.expired", ex.getMessage());
    }

    private String sha256(String raw) throws Exception {
        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        byte[] encodedHash = digest.digest(raw.getBytes(StandardCharsets.UTF_8));
        StringBuilder hexString = new StringBuilder();
        for (byte b : encodedHash) {
            String hex = Integer.toHexString(0xff & b);
            if (hex.length() == 1) hexString.append('0');
            hexString.append(hex);
        }
        return hexString.toString();
    }
}
