package com.tuxlogic.shiftiq.platform.iam.interfaces.rest.transform;

import com.tuxlogic.shiftiq.platform.iam.domain.model.commands.RefreshSessionCommand;
import com.tuxlogic.shiftiq.platform.iam.interfaces.rest.resources.RefreshSessionResource;

public class RefreshSessionCommandFromResourceAssembler {
    public static RefreshSessionCommand toCommandFromResource(RefreshSessionResource resource) {
        return new RefreshSessionCommand(resource.refreshToken());
    }
}
