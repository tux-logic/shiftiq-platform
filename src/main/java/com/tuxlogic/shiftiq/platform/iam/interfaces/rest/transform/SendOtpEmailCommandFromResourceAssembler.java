package com.tuxlogic.shiftiq.platform.iam.interfaces.rest.transform;

import com.tuxlogic.shiftiq.platform.iam.domain.model.commands.SendOtpEmailCommand;
import com.tuxlogic.shiftiq.platform.iam.domain.model.valueobjects.EmailAddress;
import com.tuxlogic.shiftiq.platform.iam.interfaces.rest.resources.SendOtpResource;

public class SendOtpEmailCommandFromResourceAssembler {
    public static SendOtpEmailCommand toCommandFromResource(SendOtpResource resource) {
        return new SendOtpEmailCommand(new EmailAddress(resource.email()));
    }
}
