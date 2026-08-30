package com.tuxlogic.shiftiq.platform.iam.interfaces.rest.transform;

import com.tuxlogic.shiftiq.platform.iam.domain.model.commands.UpdateUserEmailCommand;
import com.tuxlogic.shiftiq.platform.iam.interfaces.rest.resources.UpdateUserEmailResource;

import java.util.UUID;
import com.tuxlogic.shiftiq.platform.iam.domain.model.valueobjects.EmailAddress;
import com.tuxlogic.shiftiq.platform.iam.domain.model.valueobjects.UserId;

public class UpdateUserEmailCommandFromResourceAssembler {
    public static UpdateUserEmailCommand toCommandFromResource(UUID userId, UpdateUserEmailResource resource) {
        return new UpdateUserEmailCommand(
                new UserId(userId),
                new EmailAddress(resource.email())
        );
    }
}
