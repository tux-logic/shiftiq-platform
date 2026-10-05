package com.tuxlogic.shiftiq.platform.iam.application.commandservices;

import com.tuxlogic.shiftiq.platform.iam.domain.model.commands.RefreshSessionCommand;
import com.tuxlogic.shiftiq.platform.iam.domain.model.commands.RevokeSessionCommand;
import com.tuxlogic.shiftiq.platform.iam.domain.model.queries.AuthenticatedUser;

import java.util.Optional;

public interface SessionCommandService {
    Optional<AuthenticatedUser> handle(RefreshSessionCommand command);
    boolean handle(RevokeSessionCommand command);
}
