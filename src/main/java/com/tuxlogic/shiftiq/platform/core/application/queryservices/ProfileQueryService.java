package com.tuxlogic.shiftiq.platform.core.application.queryservices;

import com.tuxlogic.shiftiq.platform.core.domain.model.queries.GetProfileRolesByUserIdQuery;
import com.tuxlogic.shiftiq.platform.core.domain.model.valueobjects.UserId;
import com.tuxlogic.shiftiq.platform.core.interfaces.rest.resources.UserProfileContextResource;

import java.util.List;

import com.tuxlogic.shiftiq.platform.core.domain.model.queries.GetProfileByDocumentNumberQuery;
import com.tuxlogic.shiftiq.platform.core.domain.model.queries.responses.ProfileSummary;
import java.util.Optional;

public interface ProfileQueryService {
    List<String> handle(GetProfileRolesByUserIdQuery query);
    Optional<ProfileSummary> handle(GetProfileByDocumentNumberQuery query);
    UserProfileContextResource getUserProfileContext(UserId userId);
}
