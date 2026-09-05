package com.tuxlogic.shiftiq.platform.iam.application.internal.commandservices;

import com.tuxlogic.shiftiq.platform.iam.application.internal.outboundservices.hashing.HashingService;
import com.tuxlogic.shiftiq.platform.iam.domain.model.commands.GeneratePasswordRecoveryTokenCommand;
import com.tuxlogic.shiftiq.platform.iam.domain.model.commands.ResetPasswordCommand;
import com.tuxlogic.shiftiq.platform.iam.domain.model.entities.PasswordRecoveryToken;
import com.tuxlogic.shiftiq.platform.iam.domain.repositories.PasswordRecoveryTokenRepository;
import com.tuxlogic.shiftiq.platform.iam.domain.repositories.UserRepository;
import com.tuxlogic.shiftiq.platform.iam.application.commandservices.PasswordRecoveryCommandService;
import com.tuxlogic.shiftiq.platform.iam.application.internal.outboundservices.email.EmailService;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.UUID;

import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class PasswordRecoveryCommandServiceImpl implements PasswordRecoveryCommandService {

    private final UserRepository userRepository;
    private final PasswordRecoveryTokenRepository tokenRepository;
    private final EmailService emailService;
    private final HashingService hashingService;

    public PasswordRecoveryCommandServiceImpl(UserRepository userRepository, PasswordRecoveryTokenRepository tokenRepository, EmailService emailService, HashingService hashingService) {
        this.userRepository = userRepository;
        this.tokenRepository = tokenRepository;
        this.emailService = emailService;
        this.hashingService = hashingService;
    }

    @Override
    public void handle(GeneratePasswordRecoveryTokenCommand command) {
        var user = userRepository.findByEmail(command.email().value())
                .orElseThrow(() -> new IllegalArgumentException("iam.error.user.notFound"));

        String rawToken = UUID.randomUUID().toString();
        String tokenHash = hashToken(rawToken);
        var token = new PasswordRecoveryToken(tokenHash, user.getId().value(), 60); // 60 minutes
        tokenRepository.save(token);

        emailService.sendPasswordRecoveryEmail(user.getEmail().value(), rawToken);
    }

    @Override
    public void handle(ResetPasswordCommand command) {
        String tokenHash = hashToken(command.token());
        var tokenEntity = tokenRepository.findByTokenHash(tokenHash)
                .orElseThrow(() -> new IllegalArgumentException("iam.error.token.invalidOrExpired"));

        if (!tokenEntity.isValid()) {
            throw new IllegalArgumentException("iam.error.token.expiredOrUsed");
        }

        var user = userRepository.findById(tokenEntity.getUserId())
                .orElseThrow(() -> new IllegalArgumentException("iam.error.user.notFound"));

        user.changePassword(new com.tuxlogic.shiftiq.platform.iam.domain.model.valueobjects.Password(hashingService.encode(command.newPassword().value())));
        userRepository.save(user);

        tokenEntity.markAsUsed();
        tokenRepository.save(tokenEntity);
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
