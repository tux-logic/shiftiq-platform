package com.tuxlogic.shiftiq.platform.iam.application.internal.commandservices;

import com.tuxlogic.shiftiq.platform.iam.application.internal.outboundservices.hashing.HashingService;
import com.tuxlogic.shiftiq.platform.iam.domain.model.commands.GeneratePasswordRecoveryTokenCommand;
import com.tuxlogic.shiftiq.platform.iam.domain.model.commands.ResetPasswordCommand;
import com.tuxlogic.shiftiq.platform.iam.domain.model.entities.PasswordRecoveryToken;
import com.tuxlogic.shiftiq.platform.iam.domain.repositories.PasswordRecoveryTokenRepository;
import com.tuxlogic.shiftiq.platform.iam.domain.repositories.UserRepository;
import com.tuxlogic.shiftiq.platform.iam.application.commandservices.PasswordRecoveryCommandService;
import com.tuxlogic.shiftiq.platform.iam.application.internal.outboundservices.email.EmailService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.UUID;

import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class PasswordRecoveryCommandServiceImpl implements PasswordRecoveryCommandService {

    private static final Logger LOGGER = LoggerFactory.getLogger(PasswordRecoveryCommandServiceImpl.class);

    private final UserRepository userRepository;
    private final PasswordRecoveryTokenRepository tokenRepository;
    private final EmailService emailService;
    private final HashingService hashingService;
    private final int tokenExpirationMinutes;

    public PasswordRecoveryCommandServiceImpl(
            UserRepository userRepository,
            PasswordRecoveryTokenRepository tokenRepository,
            EmailService emailService,
            HashingService hashingService,
            @Value("${password.recovery.token.expiration.minutes:60}") int tokenExpirationMinutes) {
        this.userRepository = userRepository;
        this.tokenRepository = tokenRepository;
        this.emailService = emailService;
        this.hashingService = hashingService;
        this.tokenExpirationMinutes = tokenExpirationMinutes;
    }

    @Override
    public void handle(GeneratePasswordRecoveryTokenCommand command) {
        var user = userRepository.findByEmail(command.email().value())
                .orElseThrow(() -> {
                    LOGGER.warn("Password recovery requested for non-existent email: {}", command.email().value());
                    return new IllegalArgumentException("iam.error.user.notFound");
                });

        String rawToken = UUID.randomUUID().toString();
        String tokenHash = hashToken(rawToken);
        var token = new PasswordRecoveryToken(tokenHash, user.getId().value(), tokenExpirationMinutes);
        tokenRepository.save(token);

        emailService.sendPasswordRecoveryEmail(user.getEmail().value(), rawToken);
        LOGGER.info("Password recovery token generated and sent for user ID: {}", user.getId().value());
    }

    @Override
    public void handle(ResetPasswordCommand command) {
        String tokenHash = hashToken(command.token());
        var tokenEntity = tokenRepository.findByTokenHash(tokenHash)
                .orElseThrow(() -> {
                    LOGGER.warn("Reset password attempt failed: token not found");
                    return new IllegalArgumentException("iam.error.token.invalidOrExpired");
                });

        if (!tokenEntity.isValid()) {
            LOGGER.warn("Reset password attempt failed: token expired or used for user ID: {}", tokenEntity.getUserId());
            throw new IllegalArgumentException("iam.error.token.expiredOrUsed");
        }

        var user = userRepository.findById(tokenEntity.getUserId())
                .orElseThrow(() -> new IllegalArgumentException("iam.error.user.notFound"));

        user.changePassword(new com.tuxlogic.shiftiq.platform.iam.domain.model.valueobjects.Password(hashingService.encode(command.newPassword().value())));
        userRepository.save(user);

        tokenEntity.markAsUsed();
        tokenRepository.save(tokenEntity);
        LOGGER.info("Password reset successfully for user ID: {}", user.getId().value());
    }

    private String hashToken(String rawToken) {
        if (rawToken == null || rawToken.isBlank()) {
            return "";
        }
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(rawToken.getBytes(StandardCharsets.UTF_8));
            StringBuilder hexString = new StringBuilder();
            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) hexString.append('0');
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("Error hashing password recovery token", e);
        }
    }
}

