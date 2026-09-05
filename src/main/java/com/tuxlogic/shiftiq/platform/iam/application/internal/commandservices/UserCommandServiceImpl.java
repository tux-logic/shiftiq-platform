package com.tuxlogic.shiftiq.platform.iam.application.internal.commandservices;

import com.tuxlogic.shiftiq.platform.iam.application.internal.outboundservices.hashing.HashingService;
import com.tuxlogic.shiftiq.platform.iam.application.internal.outboundservices.tokens.TokenService;
import com.tuxlogic.shiftiq.platform.iam.domain.model.aggregates.User;
import com.tuxlogic.shiftiq.platform.iam.domain.model.commands.SignInCommand;
import com.tuxlogic.shiftiq.platform.iam.domain.model.commands.SignUpCommand;
import com.tuxlogic.shiftiq.platform.iam.domain.model.commands.UpdateUserEmailCommand;
import com.tuxlogic.shiftiq.platform.iam.domain.model.commands.UpdateUserPasswordCommand;
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
    private final TokenService tokenService;
    private final GoogleIdTokenVerifier googleVerifier;

    public UserCommandServiceImpl(
            UserRepository userRepository,
            HashingService hashingService,
            TokenService tokenService,
            @Value("${google.client.id:default-google-client-id}") String googleClientId) {
        this.userRepository = userRepository;
        this.hashingService = hashingService;
        this.tokenService = tokenService;
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
        var user = new User(command.email(), new com.tuxlogic.shiftiq.platform.iam.domain.model.valueobjects.Password(hashingService.encode(command.password().value())));
        userRepository.save(user);
        LOGGER.info("User registered successfully with email: {}", command.email().value());
        return userRepository.findByEmail(command.email().value());
    }

    @Override
    public Optional<AuthenticatedUser> handle(SignInCommand command) {
        var user = userRepository.findByEmail(command.email().value())
                .orElseThrow(() -> {
                    LOGGER.warn("Sign-in failed: user with email {} not found", command.email().value());
                    return new IllegalArgumentException("iam.error.credentials.invalid");
                });

        if (!hashingService.matches(command.password().value(), user.getPassword().value())) {
            LOGGER.warn("Sign-in failed: invalid password for email {}", command.email().value());
            throw new IllegalArgumentException("iam.error.credentials.invalid");
        }

        var token = tokenService.generateToken(user.getEmail().value());
        LOGGER.info("User authenticated successfully: {}", command.email().value());
        return Optional.of(new AuthenticatedUser(user, token));
    }

    @Override
    public Optional<AuthenticatedUser> handle(com.tuxlogic.shiftiq.platform.iam.domain.model.commands.GoogleSignInCommand command) {
        try {
            GoogleIdToken idToken = googleVerifier.verify(command.idToken());
            if (idToken == null) {
                LOGGER.warn("Google sign-in failed: invalid ID token");
                throw new IllegalArgumentException("iam.error.googleToken.invalid");
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

            var token = tokenService.generateToken(user.getEmail().value());
            LOGGER.info("Google sign-in successful for user email: {}", email);
            return Optional.of(new AuthenticatedUser(user, token));

        } catch (Exception e) {
            LOGGER.error("Google sign-in exception: {}", e.getMessage());
            throw new IllegalArgumentException("iam.error.googleToken.invalid");
        }
    }

    @Override
    public Optional<AuthenticatedUser> handle(UpdateUserEmailCommand command) {
        var user = userRepository.findById(command.userId().value())
                .orElseThrow(() -> new IllegalArgumentException("iam.error.user.notFound"));

        if (!user.getEmail().value().equals(command.newEmail().value()) && userRepository.existsByEmail(command.newEmail().value())) {
            LOGGER.warn("Update email failed: email already in use {}", command.newEmail().value());
            throw new IllegalArgumentException("iam.error.email.alreadyInUse");
        }

        user.changeEmail(command.newEmail());
        userRepository.save(user);
        LOGGER.info("User ID {} updated email to {}", command.userId().value(), command.newEmail().value());
        
        var token = tokenService.generateToken(user.getEmail().value());
        return Optional.of(new AuthenticatedUser(user, token));
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
}

