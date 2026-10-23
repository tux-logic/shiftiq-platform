package com.tuxlogic.shiftiq.platform.iam.application.internal.commandservices;

import com.tuxlogic.shiftiq.platform.iam.application.commandservices.OtpCommandService;
import com.tuxlogic.shiftiq.platform.iam.application.internal.outboundservices.email.EmailVerificationService;
import com.tuxlogic.shiftiq.platform.iam.application.internal.outboundservices.email.TransactionalEmailService;
import com.tuxlogic.shiftiq.platform.iam.domain.model.commands.SendOtpEmailCommand;
import com.tuxlogic.shiftiq.platform.iam.domain.model.commands.VerifyOtpCodeCommand;
import com.tuxlogic.shiftiq.platform.iam.domain.model.entities.EmailOtpVerification;
import com.tuxlogic.shiftiq.platform.iam.domain.repositories.EmailOtpVerificationRepository;
import com.tuxlogic.shiftiq.platform.iam.infrastructure.email.OtpEmailTemplateBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.Instant;

@Service
@Transactional
public class OtpCommandServiceImpl implements OtpCommandService {

    private static final Logger LOGGER = LoggerFactory.getLogger(OtpCommandServiceImpl.class);

    private final EmailOtpVerificationRepository otpRepository;
    private final EmailVerificationService emailVerificationService;
    private final TransactionalEmailService transactionalEmailService;
    private final int expirationMinutes;
    private final int maxAttempts;
    private final int cooldownSeconds;
    private final SecureRandom secureRandom = new SecureRandom();

    public OtpCommandServiceImpl(
            EmailOtpVerificationRepository otpRepository,
            EmailVerificationService emailVerificationService,
            TransactionalEmailService transactionalEmailService,
            @Value("${iam.otp.expiration.minutes:10}") int expirationMinutes,
            @Value("${iam.otp.max.attempts:5}") int maxAttempts,
            @Value("${iam.otp.resend.cooldown.seconds:60}") int cooldownSeconds
    ) {
        this.otpRepository = otpRepository;
        this.emailVerificationService = emailVerificationService;
        this.transactionalEmailService = transactionalEmailService;
        this.expirationMinutes = expirationMinutes;
        this.maxAttempts = maxAttempts;
        this.cooldownSeconds = cooldownSeconds;
    }

    @Override
    public void handle(SendOtpEmailCommand command) {
        String email = command.email().value();

        // 1. Cooldown check against spamming resends
        var recentOtp = otpRepository.findTopByEmailOrderByCreatedAtDesc(email);
        if (recentOtp.isPresent()) {
            var latest = recentOtp.get();
            if (latest.getCreatedAt().isAfter(Instant.now().minusSeconds(cooldownSeconds))) {
                LOGGER.warn("OTP request throttled for {}: cooldown active", email);
                throw new IllegalStateException("iam.error.otp.cooldownActive");
            }
        }

        // 2. Deliverability pre-check via EmailVerify.io
        var verificationResult = emailVerificationService.verifyEmail(email);
        if (!verificationResult.valid()) {
            LOGGER.warn("OTP dispatch aborted for {}: email verification rejected ({})",
                    email, verificationResult.subStatus());
            if ("error".equalsIgnoreCase(verificationResult.status())) {
                throw new IllegalArgumentException("iam.error.email.unverifiable");
            }
            throw new IllegalArgumentException("iam.error.email.invalidAddress");
        }

        // 3. Generate 6-digit cryptographic code
        int codeInt = 100000 + secureRandom.nextInt(900000);
        String code = String.valueOf(codeInt);
        String codeHash = hashToken(code);

        // 4. Persist OTP entity
        var otpRecord = new EmailOtpVerification(email, codeHash, expirationMinutes, maxAttempts);
        otpRepository.save(otpRecord);

        // 5. Send email via Brevo
        String subject = "Tu código de verificación - ShiftIQ";
        String htmlContent = OtpEmailTemplateBuilder.buildOtpEmailHtml(code, expirationMinutes);
        boolean sent = transactionalEmailService.sendEmail(email, subject, htmlContent);

        if (!sent) {
            LOGGER.error("Failed to send OTP email to {}", email);
            throw new IllegalStateException("iam.error.otp.sendFailed");
        }

        LOGGER.info("OTP code issued and dispatched to {}", email);
    }

    @Override
    public boolean handle(VerifyOtpCodeCommand command) {
        String email = command.email().value();
        String submittedCode = command.code();

        var otpRecord = otpRepository.findTopByEmailOrderByCreatedAtDesc(email)
                .orElseThrow(() -> new IllegalArgumentException("iam.error.otp.notFound"));

        if (otpRecord.isConsumed() || otpRecord.isVerified()) {
            throw new IllegalArgumentException("iam.error.otp.alreadyUsed");
        }

        if (Instant.now().isAfter(otpRecord.getExpiresAt())) {
            throw new IllegalArgumentException("iam.error.otp.expired");
        }

        if (otpRecord.getAttempts() >= otpRecord.getMaxAttempts()) {
            throw new IllegalArgumentException("iam.error.otp.maxAttemptsExceeded");
        }

        String submittedHash = hashToken(submittedCode);
        boolean matches = submittedHash.equalsIgnoreCase(otpRecord.getOtpHash());

        otpRecord.incrementAttempts();

        if (!matches) {
            otpRepository.save(otpRecord);
            LOGGER.warn("Invalid OTP attempt for email {}. Attempt {} of {}", email, otpRecord.getAttempts(), otpRecord.getMaxAttempts());
            throw new IllegalArgumentException("iam.error.otp.invalid");
        }

        otpRecord.markVerified();
        otpRepository.save(otpRecord);
        LOGGER.info("OTP verified successfully for email: {}", email);
        return true;
    }

    private String hashToken(String rawToken) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] encodedHash = digest.digest(rawToken.getBytes(StandardCharsets.UTF_8));
            StringBuilder hexString = new StringBuilder();
            for (byte b : encodedHash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) {
                    hexString.append('0');
                }
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("SHA-256 algorithm not available", e);
        }
    }
}
