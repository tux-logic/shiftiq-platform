package com.tuxlogic.shiftiq.platform.iam.interfaces.rest.resources;

import java.util.UUID;

public record AuthenticatedUserResource(UUID id, String email, String role, String token) {
}
