package com.andeva.atelier.platform.iam.interfaces.rest.transform;

import com.andeva.atelier.platform.iam.domain.model.commands.SignUpCommand;
import com.andeva.atelier.platform.iam.interfaces.rest.resources.SignUpResource;

import com.andeva.atelier.platform.iam.domain.model.valueobjects.EmailAddress;
import com.andeva.atelier.platform.iam.domain.model.valueobjects.Password;

public class SignUpCommandFromResourceAssembler {
    public static SignUpCommand toCommandFromResource(SignUpResource resource) {
        return new SignUpCommand(
                new EmailAddress(resource.email()),
                new Password(resource.password())
        );
    }
}
