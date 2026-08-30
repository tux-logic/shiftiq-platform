package com.tuxlogic.shiftiq.platform.iam.interfaces.rest.transform;

import com.tuxlogic.shiftiq.platform.iam.domain.model.commands.GeneratePasswordRecoveryTokenCommand;
import com.tuxlogic.shiftiq.platform.iam.interfaces.rest.resources.PasswordRecoveryResource;

public class GeneratePasswordRecoveryTokenCommandFromResourceAssembler {
    public static GeneratePasswordRecoveryTokenCommand toCommandFromResource(PasswordRecoveryResource resource) {
        return new GeneratePasswordRecoveryTokenCommand(new com.tuxlogic.shiftiq.platform.iam.domain.model.valueobjects.EmailAddress(resource.email()));
    }
}
