package com.tuxlogic.shiftiq.platform.fleet.application.outboundservices;

import java.util.Set;
import java.util.UUID;

/**
 * Anti-Corruption Layer (ACL) Outbound Port to manage user accounts (roles and branch
 * memberships) in the IAM context when staff registrations are created, approved or
 * removed.
 */
public interface ExternalIamService {

    /**
     * Assigns a staff role to a user account. Downgrades are ignored by IAM.
     *
     * @param userId   target user account
     * @param roleName role name, with or without the {@code ROLE_} prefix
     * @return {@code true} when the account now holds the requested role
     */
    boolean assignRole(UUID userId, String roleName);

    /**
     * Replaces the branch memberships of a user account.
     *
     * @param userId    target user account
     * @param branchIds the new set of branch ids, empty clears the memberships
     */
    void setBranches(UUID userId, Set<UUID> branchIds);
}
