package com.tuxlogic.shiftiq.platform.iam.domain.model.events;

import org.springframework.context.ApplicationEvent;
import java.util.UUID;

/**
 * Domain event published when a user account is deactivated.
 */
public class UserDeactivatedEvent extends ApplicationEvent {
    private final UUID userId;

    public UserDeactivatedEvent(Object source, UUID userId) {
        super(source);
        this.userId = userId;
    }

    public UUID getUserId() {
        return userId;
    }
}
