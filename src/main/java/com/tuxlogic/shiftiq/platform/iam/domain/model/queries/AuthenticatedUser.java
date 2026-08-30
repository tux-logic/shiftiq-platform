package com.tuxlogic.shiftiq.platform.iam.domain.model.queries;

import com.tuxlogic.shiftiq.platform.iam.domain.model.aggregates.User;

public record AuthenticatedUser(User user, String token) {
}
