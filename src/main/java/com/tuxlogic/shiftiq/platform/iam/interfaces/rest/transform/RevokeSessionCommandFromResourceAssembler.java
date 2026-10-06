package com.tuxlogic.shiftiq.platform.iam.interfaces.rest.transform;

import com.tuxlogic.shiftiq.platform.iam.domain.model.commands.RevokeSessionCommand;
import com.tuxlogic.shiftiq.platform.iam.interfaces.rest.resources.RevokeSessionResource;

public class RevokeSessionCommandFromResourceAssembler {
    public static RevokeSessionCommand toCommandFromResource(RevokeSessionResource resource) {
        return new RevokeSessionCommand(resource.refreshToken());
    }
}
