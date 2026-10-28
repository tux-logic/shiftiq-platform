package com.tuxlogic.shiftiq.platform.iam.application.internal.commandservices;

import com.tuxlogic.shiftiq.platform.iam.application.internal.outboundservices.email.EmailVerificationService;
import com.tuxlogic.shiftiq.platform.iam.domain.repositories.EmailOtpVerificationRepository;
import com.tuxlogic.shiftiq.platform.iam.application.internal.outboundservices.hashing.HashingService;
import com.tuxlogic.shiftiq.platform.iam.domain.model.aggregates.User;
import com.tuxlogic.shiftiq.platform.iam.domain.model.commands.SignInCommand;
import com.tuxlogic.shiftiq.platform.iam.domain.model.commands.SignUpCommand;
import com.tuxlogic.shiftiq.platform.iam.domain.model.commands.UpdateUserEmailCommand;
import com.tuxlogic.shiftiq.platform.iam.domain.model.commands.UpdateUserPasswordCommand;
import com.tuxlogic.shiftiq.platform.iam.application.internal.services.SessionIssuer;
import com.tuxlogic.shiftiq.platform.iam.domain.model.queries.AuthenticatedUser;
import com.tuxlogic.shiftiq.platform.iam.domain.repositories.UserRepository;
import com.tuxlogic.shiftiq.platform.iam.application.commandservices.UserCommandService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.Collections;
import java.util.UUID;

import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import org.springframework.beans.factory.annotation.Value;

import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class UserCommandServiceImpl implements UserCommandService {

    private static final Logger LOGGER = LoggerFactory.getLogger(UserCommandServiceImpl.class);

    private final UserRepository userRepository;
    private final HashingService hashingService;
    private final SessionIssuer sessionIssuer;
    private final GoogleIdTokenVerifier googleVerifier;
    private final EmailVerificationService emailVerificationService;
    private final EmailOtpVerificationRepository emailOtpVerificationRepository;

    public UserCommandServiceImpl(
            UserRepository userRepository,
            HashingService hashingService,
            SessionIssuer sessionIssuer,
            EmailVerificationService emailVerificationService,
            EmailOtpVerificationRepository emailOtpVerificationRepository,
            @Value("${google.client.id:default-google-client-id}") String googleClientId) {
        this.userRepository = userRepository;
        this.hashingService = hashingService;
        this.sessionIssuer = sessionIssuer;
        this.emailVerificationService = emailVerificationService;
        this.emailOtpVerificationRepository = emailOtpVerificationRepository;
        this.googleVerifier = new GoogleIdTokenVerifier.Builder(new NetHttpTransport(), new GsonFactory())
                .setAudience(Collections.singletonList(googleClientId))
                .build();
    }

    @Override
    public Optional<User> handle(SignUpCommand command) {
        if (userRepository.existsByEmail(command.email().value())) {
            LOGGER.warn("Sign-up failed: email already in use {}", command.email().value());
            throw new IllegalArgumentException("iam.error.email.alreadyInUse");
        }

        var verificationResult = emailVerificationService.verifyEmail(command.email().value());
        if (!verificationResult.valid()) {
            LOGGER.warn("Sign-up failed: email verification rejected {} (status={}, sub_status={})",
                    command.email().value(), verificationResult.status(), verificationResult.subStatus());
            if ("error".equalsIgnoreCase(verificationResult.status())) {
                throw new IllegalArgumentException("iam.error.email.unverifiable");
            }
            throw new IllegalArgumentException("iam.error.email.invalidAddress");
        }

        var otpRecord = emailOtpVerificationRepository.findTopByEmailOrderByCreatedAtDesc(command.email().value())
                .orElseThrow(() -> {
                    LOGGER.warn("Sign-up failed: no OTP verification found for email {}", command.email().value());
                    return new IllegalArgumentException("iam.error.otp.verificationRequired");
                });

        if (!otpRecord.isReadyForConsumption()) {
            LOGGER.warn("Sign-up failed: OTP not ready for consumption (verified={}, consumed={}, expired={}) for email {}",
                    otpRecord.isVerified(), otpRecord.isConsumed(), java.time.Instant.now().isAfter(otpRecord.getExpiresAt()), command.email().value());
            throw new IllegalArgumentException("iam.error.otp.verificationRequired");
        }

        var user = new User(command.email(), new com.tuxlogic.shiftiq.platform.iam.domain.model.valueobjects.Password(hashingService.encode(command.password().value())));
        userRepository.save(user);

        otpRecord.markConsumed();
        emailOtpVerificationRepository.save(otpRecord);

        LOGGER.info("User registered successfully with email: {}", command.email().value());
        return userRepository.findByEmail(command.email().value());
    }

    @Override
    public Optional<AuthenticatedUser> handle(SignInCommand command) {
        var user = userRepository.findByEmail(command.email().value()).orElse(null);
        if (user == null) {
            LOGGER.warn("Sign-in failed: user with email {} not found", command.email().value());
            return Optional.empty();
        }

        if (!hashingService.matches(command.password().value(), user.getPassword().value())) {
            LOGGER.warn("Sign-in failed: invalid password for email {}", command.email().value());
            return Optional.empty();
        }

        LOGGER.info("User authenticated successfully: {}", command.email().value());
        return Optional.of(sessionIssuer.issue(user));
    }

    @Override
    public Optional<AuthenticatedUser> handle(com.tuxlogic.shiftiq.platform.iam.domain.model.commands.GoogleSignInCommand command) {
        try {
            GoogleIdToken idToken = googleVerifier.verify(command.idToken());
            if (idToken == null) {
                LOGGER.warn("Google sign-in failed: invalid ID token");
                return Optional.empty();
            }

            GoogleIdToken.Payload payload = idToken.getPayload();
            String email = payload.getEmail();
            String googleId = payload.getSubject();

            User user = userRepository.findByEmail(email).orElse(null);

            if (user == null) {
                String randomPassword = String.format("%s%s", UUID.randomUUID(), UUID.randomUUID());
                user = new User(new com.tuxlogic.shiftiq.platform.iam.domain.model.valueobjects.EmailAddress(email), new com.tuxlogic.shiftiq.platform.iam.domain.model.valueobjects.Password(hashingService.encode(randomPassword)), new com.tuxlogic.shiftiq.platform.iam.domain.model.valueobjects.GoogleId(googleId));
                userRepository.save(user);
                LOGGER.info("New user registered via Google Sign-In with email: {}", email);
            } else {
                if (user.getGoogleId() == null || user.getGoogleId().value().isEmpty()) {
                    user.linkGoogleAccount(new com.tuxlogic.shiftiq.platform.iam.domain.model.valueobjects.GoogleId(googleId));
                    userRepository.save(user);
                    LOGGER.info("Linked Google account for user email: {}", email);
                }
            }

            LOGGER.info("Google sign-in successful for user email: {}", email);
            return Optional.of(sessionIssuer.issue(user));

        } catch (Exception e) {
            LOGGER.error("Google sign-in exception: {}", e.getMessage());
            return Optional.empty();
        }
    }

    @Override
    public Optional<AuthenticatedUser> handle(UpdateUserEmailCommand command) {
        var user = userRepository.findById(command.userId().value())
                .orElseThrow(() -> new IllegalArgumentException("iam.error.user.notFound"));

        if (!user.getEmail().value().equals(command.newEmail().value())) {
            if (userRepository.existsByEmail(command.newEmail().value())) {
                LOGGER.warn("Update email failed: email already in use {}", command.newEmail().value());
                throw new IllegalArgumentException("iam.error.email.alreadyInUse");
            }

            var verificationResult = emailVerificationService.verifyEmail(command.newEmail().value());
            if (!verificationResult.valid()) {
                LOGGER.warn("Update email failed: email verification rejected {} (status={}, sub_status={})",
                        command.newEmail().value(), verificationResult.status(), verificationResult.subStatus());
                if ("error".equalsIgnoreCase(verificationResult.status())) {
                    throw new IllegalArgumentException("iam.error.email.unverifiable");
                }
                throw new IllegalArgumentException("iam.error.email.invalidAddress");
            }
        }

        user.changeEmail(command.newEmail());
        userRepository.save(user);
        LOGGER.info("User ID {} updated email to {}", command.userId().value(), command.newEmail().value());

        return Optional.of(sessionIssuer.issue(user));
    }

    @Override
    public Optional<User> handle(UpdateUserPasswordCommand command) {
        var user = userRepository.findById(command.userId().value())
                .orElseThrow(() -> new IllegalArgumentException("iam.error.user.notFound"));

        if (!hashingService.matches(command.currentPassword().value(), user.getPassword().value())) {
            LOGGER.warn("Update password failed for User ID {}: current password invalid", command.userId().value());
            throw new IllegalArgumentException("iam.error.currentPassword.invalid");
        }

        user.changePassword(new com.tuxlogic.shiftiq.platform.iam.domain.model.valueobjects.Password(hashingService.encode(command.newPassword().value())));
        userRepository.save(user);
        LOGGER.info("Password updated successfully for User ID {}", command.userId().value());
        return Optional.of(user);
    }

    @Override
    public Optional<User> handle(com.tuxlogic.shiftiq.platform.iam.domain.model.commands.AssignBranchToUserCommand command) {
        var user = userRepository.findById(command.userId().value())
                .orElseThrow(() -> new IllegalArgumentException("iam.error.user.notFound"));
        user.assignBranch(command.branchId().value());
        userRepository.save(user);
        LOGGER.info("Assigned branch {} to user ID {}", command.branchId().value(), command.userId().value());
        return Optional.of(user);
    }

    @Override
    public Optional<User> handle(com.tuxlogic.shiftiq.platform.iam.domain.model.commands.AssignRoleToUserCommand command) {
        var user = userRepository.findById(command.userId().value())
                .orElseThrow(() -> new IllegalArgumentException("iam.error.user.notFound"));
        var currentRole = user.getRole();
        if (currentRole == command.role()) {
            return Optional.of(user);
        }
        if (!currentRole.canBeReplacedBy(command.role())) {
            LOGGER.warn("Role assignment skipped for user ID {}: {} would downgrade {}",
                    command.userId().value(), command.role(), currentRole);
            return Optional.empty();
        }
        user.assignRole(command.role());
        userRepository.save(user);
        LOGGER.info("Assigned role {} to user ID {} (previous role {})",
                command.role(), command.userId().value(), currentRole);
        return Optional.of(user);
    }

    @Override
    public Optional<User> handle(com.tuxlogic.shiftiq.platform.iam.domain.model.commands.SetUserBranchesCommand command) {
        var user = userRepository.findById(command.userId().value())
                .orElseThrow(() -> new IllegalArgumentException("iam.error.user.notFound"));
        user.replaceBranches(command.branchIds());
        userRepository.save(user);
        LOGGER.info("Replaced branches of user ID {} with {}", command.userId().value(), command.branchIds());
        return Optional.of(user);
    }
}

