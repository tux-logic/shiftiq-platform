package com.tuxlogic.shiftiq.platform.fleet.infrastructure.outboundservices;

import com.tuxlogic.shiftiq.platform.fleet.application.outboundservices.ExternalIamService;
import com.tuxlogic.shiftiq.platform.iam.application.commandservices.UserCommandService;
import com.tuxlogic.shiftiq.platform.iam.domain.model.commands.AssignRoleToUserCommand;
import com.tuxlogic.shiftiq.platform.iam.domain.model.commands.SetUserBranchesCommand;
import com.tuxlogic.shiftiq.platform.iam.domain.model.valueobjects.Roles;
import com.tuxlogic.shiftiq.platform.iam.domain.model.valueobjects.UserId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.Set;
import java.util.UUID;

/**
 * ACL adapter delegating staff account updates to the IAM context through its
 * application layer, so fleet never couples to IAM repositories.
 */
@Service
public class ExternalIamServiceImpl implements ExternalIamService {

    private static final Logger log = LoggerFactory.getLogger(ExternalIamServiceImpl.class);

    private final UserCommandService userCommandService;

    public ExternalIamServiceImpl(UserCommandService userCommandService) {
        this.userCommandService = userCommandService;
    }

    @Override
    public boolean assignRole(UUID userId, String roleName) {
        if (userId == null) {
            return false;
        }
        var role = Roles.fromName(roleName);
        if (role == null) {
            log.warn("Unknown role '{}' ignored for user ID {}", roleName, userId);
            return false;
        }
        try {
            var updated = userCommandService.handle(new AssignRoleToUserCommand(new UserId(userId), role));
            if (updated.isEmpty()) {
                log.warn("Role '{}' not applied to user ID {}: existing role has higher privileges", role, userId);
                return false;
            }
            return updated.get().getRole() == role;
        } catch (IllegalArgumentException ex) {
            log.warn("Could not assign role '{}' to user ID {}: {}", role, userId, ex.getMessage());
            return false;
        }
    }

    @Override
    public void setBranches(UUID userId, Set<UUID> branchIds) {
        if (userId == null) {
            return;
        }
        try {
            userCommandService.handle(new SetUserBranchesCommand(new UserId(userId), branchIds));
        } catch (IllegalArgumentException ex) {
            log.warn("Could not replace branches of user ID {}: {}", userId, ex.getMessage());
        }
    }
}
