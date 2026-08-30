package com.andeva.atelier.platform.iam.interfaces.rest.transform;

import com.andeva.atelier.platform.iam.domain.model.commands.UpdateUserPasswordCommand;
import com.andeva.atelier.platform.iam.interfaces.rest.resources.UpdateUserPasswordResource;

import java.util.UUID;
import com.andeva.atelier.platform.iam.domain.model.valueobjects.Password;
import com.andeva.atelier.platform.iam.domain.model.valueobjects.UserId;

public class UpdateUserPasswordCommandFromResourceAssembler {
    public static UpdateUserPasswordCommand toCommandFromResource(UUID userId, UpdateUserPasswordResource resource) {
        return new UpdateUserPasswordCommand(
                new UserId(userId),
                new Password(resource.currentPassword()),
                new Password(resource.newPassword())
        );
    }
}
