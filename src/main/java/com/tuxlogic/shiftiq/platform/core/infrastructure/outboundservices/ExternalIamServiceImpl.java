package com.tuxlogic.shiftiq.platform.core.infrastructure.outboundservices;

import com.tuxlogic.shiftiq.platform.core.application.outboundservices.ExternalIamService;
import com.tuxlogic.shiftiq.platform.iam.application.commandservices.UserCommandService;
import com.tuxlogic.shiftiq.platform.iam.domain.model.commands.AssignRoleToUserCommand;
import com.tuxlogic.shiftiq.platform.iam.domain.model.valueobjects.Roles;
import com.tuxlogic.shiftiq.platform.iam.domain.model.valueobjects.UserId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service("coreExternalIamService")
public class ExternalIamServiceImpl implements ExternalIamService {

    private static final Logger log = LoggerFactory.getLogger(ExternalIamServiceImpl.class);

    private final UserCommandService userCommandService;

    public ExternalIamServiceImpl(UserCommandService userCommandService) {
        this.userCommandService = userCommandService;
    }

    @Override
    public boolean assignRole(UUID userId, String roleName) {
        if (userId == null || roleName == null || roleName.isBlank()) {
            return false;
        }
        Roles role = Roles.fromName(roleName);
        if (role == null) {
            log.warn("Cannot assign unknown role '{}' to user {}", roleName, userId);
            return false;
        }
        try {
            var updated = userCommandService.handle(new AssignRoleToUserCommand(new UserId(userId), role));
            return updated.isPresent();
        } catch (Exception ex) {
            log.warn("Failed to assign role '{}' to user {}: {}", roleName, userId, ex.getMessage());
            return false;
        }
    }
}
