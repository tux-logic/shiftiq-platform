package com.andeva.atelier.platform.iam.interfaces.rest.transform;

import com.andeva.atelier.platform.iam.domain.model.commands.GoogleSignInCommand;
import com.andeva.atelier.platform.iam.interfaces.rest.resources.GoogleSignInResource;

public class GoogleSignInCommandFromResourceAssembler {
    public static GoogleSignInCommand toCommandFromResource(GoogleSignInResource resource) {
        return new GoogleSignInCommand(resource.idToken());
    }
}
