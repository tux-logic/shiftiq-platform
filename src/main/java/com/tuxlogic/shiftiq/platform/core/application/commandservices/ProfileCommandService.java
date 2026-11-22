package com.tuxlogic.shiftiq.platform.core.application.commandservices;

import com.tuxlogic.shiftiq.platform.core.domain.model.valueobjects.UserId;
import com.tuxlogic.shiftiq.platform.core.interfaces.rest.resources.UserProfileContextResource;

/**
 * Application service for updating profile-level attributes across a user's Core profiles.
 */
public interface ProfileCommandService {

    /**
     * Updates the profile image URL on all Core profiles (Owner, Employee, Customer) associated with the user.
     *
     * @param userId The authenticated user's identifier.
     * @param profileImageUrl The new profile image URL.
     * @return The updated {@link UserProfileContextResource}.
     */
    UserProfileContextResource updateProfileImage(UserId userId, String profileImageUrl);
}
