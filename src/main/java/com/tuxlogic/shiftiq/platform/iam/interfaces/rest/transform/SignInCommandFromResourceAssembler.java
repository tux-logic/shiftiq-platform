package com.tuxlogic.shiftiq.platform.iam.interfaces.rest.transform;

import com.tuxlogic.shiftiq.platform.iam.domain.model.commands.SignInCommand;
import com.tuxlogic.shiftiq.platform.iam.interfaces.rest.resources.SignInResource;

import com.tuxlogic.shiftiq.platform.iam.domain.model.valueobjects.EmailAddress;
import com.tuxlogic.shiftiq.platform.iam.domain.model.valueobjects.Password;

public class SignInCommandFromResourceAssembler {
    public static SignInCommand toCommandFromResource(SignInResource resource) {
        return new SignInCommand(
                new EmailAddress(resource.email()),
                new Password(resource.password())
        );
    }
}
