package com.tuxlogic.shiftiq.platform.iam.application.commandservices;

import com.tuxlogic.shiftiq.platform.iam.domain.model.aggregates.User;
import com.tuxlogic.shiftiq.platform.iam.domain.model.commands.SignInCommand;
import com.tuxlogic.shiftiq.platform.iam.domain.model.commands.SignUpCommand;
import com.tuxlogic.shiftiq.platform.iam.domain.model.commands.UpdateUserEmailCommand;
import com.tuxlogic.shiftiq.platform.iam.domain.model.commands.UpdateUserPasswordCommand;
import com.tuxlogic.shiftiq.platform.iam.domain.model.commands.GoogleSignInCommand;
import com.tuxlogic.shiftiq.platform.iam.domain.model.commands.AssignRoleToUserCommand;
import com.tuxlogic.shiftiq.platform.iam.domain.model.commands.SetUserBranchesCommand;
import com.tuxlogic.shiftiq.platform.iam.domain.model.queries.AuthenticatedUser;

import com.tuxlogic.shiftiq.platform.iam.domain.model.commands.AssignBranchToUserCommand;

import java.util.Optional;

public interface UserCommandService {
    Optional<User> handle(SignUpCommand command);
    Optional<AuthenticatedUser> handle(SignInCommand command);
    Optional<AuthenticatedUser> handle(GoogleSignInCommand command);
    Optional<AuthenticatedUser> handle(UpdateUserEmailCommand command);
    Optional<User> handle(UpdateUserPasswordCommand command);
    Optional<User> handle(AssignBranchToUserCommand command);
    Optional<User> handle(AssignRoleToUserCommand command);
    Optional<User> handle(SetUserBranchesCommand command);
}
