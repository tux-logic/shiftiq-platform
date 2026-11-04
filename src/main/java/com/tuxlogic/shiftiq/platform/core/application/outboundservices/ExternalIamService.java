package com.tuxlogic.shiftiq.platform.core.application.outboundservices;

import java.util.UUID;

/**
 * Anti-Corruption Layer (ACL) Outbound Port to manage user account roles in IAM
 * when owner profiles are registered.
 */
public interface ExternalIamService {

    /**
     * Assigns a role to a user account. Downgrades are ignored by IAM.
     *
     * @param userId   target user account
     * @param roleName role name, with or without the {@code ROLE_} prefix
     * @return {@code true} when the account holds the requested role
     */
    boolean assignRole(UUID userId, String roleName);
}
