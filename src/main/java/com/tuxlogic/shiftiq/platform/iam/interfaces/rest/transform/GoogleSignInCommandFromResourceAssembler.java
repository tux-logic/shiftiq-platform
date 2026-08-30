package com.tuxlogic.shiftiq.platform.iam.interfaces.rest.transform;

import com.tuxlogic.shiftiq.platform.iam.domain.model.commands.GoogleSignInCommand;
import com.tuxlogic.shiftiq.platform.iam.interfaces.rest.resources.GoogleSignInResource;

public class GoogleSignInCommandFromResourceAssembler {
    public static GoogleSignInCommand toCommandFromResource(GoogleSignInResource resource) {
        return new GoogleSignInCommand(resource.idToken());
    }
}
