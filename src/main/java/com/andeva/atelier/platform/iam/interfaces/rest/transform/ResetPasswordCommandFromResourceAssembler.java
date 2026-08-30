package com.andeva.atelier.platform.iam.interfaces.rest.transform;

import com.andeva.atelier.platform.iam.domain.model.commands.ResetPasswordCommand;
import com.andeva.atelier.platform.iam.interfaces.rest.resources.ResetPasswordResource;

public class ResetPasswordCommandFromResourceAssembler {
    public static ResetPasswordCommand toCommandFromResource(ResetPasswordResource resource) {
        return new ResetPasswordCommand(resource.token(), new com.andeva.atelier.platform.iam.domain.model.valueobjects.Password(resource.newPassword()));
    }
}
