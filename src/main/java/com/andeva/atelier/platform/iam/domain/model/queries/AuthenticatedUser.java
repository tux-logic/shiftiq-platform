package com.andeva.atelier.platform.iam.domain.model.queries;

import com.andeva.atelier.platform.iam.domain.model.aggregates.User;

public record AuthenticatedUser(User user, String token) {
}
