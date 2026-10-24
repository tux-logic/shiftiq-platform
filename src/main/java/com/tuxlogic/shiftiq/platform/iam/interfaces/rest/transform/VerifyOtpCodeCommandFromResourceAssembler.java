package com.tuxlogic.shiftiq.platform.iam.interfaces.rest.transform;

import com.tuxlogic.shiftiq.platform.iam.domain.model.commands.VerifyOtpCodeCommand;
import com.tuxlogic.shiftiq.platform.iam.domain.model.valueobjects.EmailAddress;
import com.tuxlogic.shiftiq.platform.iam.interfaces.rest.resources.VerifyOtpResource;

public class VerifyOtpCodeCommandFromResourceAssembler {
    public static VerifyOtpCodeCommand toCommandFromResource(VerifyOtpResource resource) {
        return new VerifyOtpCodeCommand(new EmailAddress(resource.email()), resource.code());
    }
}
