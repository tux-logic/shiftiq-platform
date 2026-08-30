package com.andeva.atelier.platform.iam.application.commandservices;

import com.andeva.atelier.platform.iam.domain.model.aggregates.User;
import com.andeva.atelier.platform.iam.domain.model.commands.SignInCommand;
import com.andeva.atelier.platform.iam.domain.model.commands.SignUpCommand;
import com.andeva.atelier.platform.iam.domain.model.commands.UpdateUserEmailCommand;
import com.andeva.atelier.platform.iam.domain.model.commands.UpdateUserPasswordCommand;
import com.andeva.atelier.platform.iam.domain.model.commands.GoogleSignInCommand;
import com.andeva.atelier.platform.iam.domain.model.queries.AuthenticatedUser;

import java.util.Optional;

public interface UserCommandService {
    Optional<User> handle(SignUpCommand command);
    Optional<AuthenticatedUser> handle(SignInCommand command);
    Optional<AuthenticatedUser> handle(GoogleSignInCommand command);
    Optional<AuthenticatedUser> handle(UpdateUserEmailCommand command);
    Optional<User> handle(UpdateUserPasswordCommand command);
}
