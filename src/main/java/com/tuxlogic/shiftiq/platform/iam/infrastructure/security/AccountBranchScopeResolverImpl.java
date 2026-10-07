package com.tuxlogic.shiftiq.platform.iam.infrastructure.security;

import com.tuxlogic.shiftiq.platform.iam.infrastructure.persistence.jpa.repositories.UserPersistenceRepository;
import com.tuxlogic.shiftiq.platform.shared.infrastructure.security.AccountBranchScopeResolver;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.Set;
import java.util.UUID;

/**
 * IAM implementation of {@link AccountBranchScopeResolver}: reads the branch
 * memberships persisted on the user account.
 */
@Component
public class AccountBranchScopeResolverImpl implements AccountBranchScopeResolver {

    private final UserPersistenceRepository userRepository;

    public AccountBranchScopeResolverImpl(UserPersistenceRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public Set<UUID> findBranchIdsForAccount(UUID accountId) {
        if (accountId == null) {
            return Set.of();
        }
        return userRepository.findByIdAndNotDeleted(accountId)
                .map(user -> user.getBranchIds() != null ? Set.copyOf(user.getBranchIds()) : Set.<UUID>of())
                .orElse(Collections.emptySet());
    }
}
