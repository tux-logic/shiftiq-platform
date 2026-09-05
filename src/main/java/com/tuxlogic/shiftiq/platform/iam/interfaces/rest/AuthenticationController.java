package com.tuxlogic.shiftiq.platform.iam.interfaces.rest;

import com.tuxlogic.shiftiq.platform.iam.application.commandservices.PasswordRecoveryCommandService;
import com.tuxlogic.shiftiq.platform.iam.application.commandservices.UserCommandService;
import com.tuxlogic.shiftiq.platform.iam.interfaces.rest.resources.*;
import com.tuxlogic.shiftiq.platform.iam.interfaces.rest.transform.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping(value = "/api/v1/authentication", produces = MediaType.APPLICATION_JSON_VALUE)
@Tag(name = "Authentication", description = "Authentication Management Endpoints")
public class AuthenticationController {

    private final UserCommandService userCommandService;
    private final PasswordRecoveryCommandService passwordRecoveryCommandService;

    public AuthenticationController(UserCommandService userCommandService, PasswordRecoveryCommandService passwordRecoveryCommandService) {
        this.userCommandService = userCommandService;
        this.passwordRecoveryCommandService = passwordRecoveryCommandService;
    }

    @PostMapping("/sessions")
    @Operation(summary = "Sign in", description = "Authenticate a user and return a token")
    public ResponseEntity<AuthenticatedUserResource> signIn(@Valid @RequestBody SignInResource signInResource) {
        var signInCommand = SignInCommandFromResourceAssembler.toCommandFromResource(signInResource);
        var authenticatedUser = userCommandService.handle(signInCommand);
        if (authenticatedUser.isEmpty()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        var authenticatedUserResource = AuthenticatedUserResourceFromEntityAssembler.toResourceFromEntity(authenticatedUser.get());
        return ResponseEntity.ok(authenticatedUserResource);
    }

    @PostMapping("/sessions/google")
    @Operation(summary = "Google sign in", description = "Authenticate a user using Google and return a token")
    public ResponseEntity<AuthenticatedUserResource> googleSignIn(@Valid @RequestBody GoogleSignInResource resource) {
        var googleSignInCommand = GoogleSignInCommandFromResourceAssembler.toCommandFromResource(resource);
        var authenticatedUser = userCommandService.handle(googleSignInCommand);
        if (authenticatedUser.isEmpty()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        var authenticatedUserResource = AuthenticatedUserResourceFromEntityAssembler.toResourceFromEntity(authenticatedUser.get());
        return ResponseEntity.ok(authenticatedUserResource);
    }

    @PostMapping("/password-recoveries")
    @Operation(summary = "Forgot password", description = "Send a password recovery email")
    public ResponseEntity<Void> forgotPassword(@Valid @RequestBody PasswordRecoveryResource resource) {
        var command = GeneratePasswordRecoveryTokenCommandFromResourceAssembler.toCommandFromResource(resource);
        passwordRecoveryCommandService.handle(command);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/password-resets")
    @Operation(summary = "Reset password", description = "Reset user password using recovery token")
    public ResponseEntity<Void> resetPassword(@Valid @RequestBody ResetPasswordResource resource) {
        var command = ResetPasswordCommandFromResourceAssembler.toCommandFromResource(resource);
        passwordRecoveryCommandService.handle(command);
        return ResponseEntity.ok().build();
    }
}
