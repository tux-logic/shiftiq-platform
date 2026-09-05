package com.tuxlogic.shiftiq.platform.iam.domain.model.events;

import org.springframework.context.ApplicationEvent;
import java.util.UUID;

/**
 * Domain event published when a user changes their email address.
 */
public class UserEmailChangedEvent extends ApplicationEvent {
    private final UUID userId;
    private final String oldEmail;
    private final String newEmail;

    public UserEmailChangedEvent(Object source, UUID userId, String oldEmail, String newEmail) {
        super(source);
        this.userId = userId;
        this.oldEmail = oldEmail;
        this.newEmail = newEmail;
    }

    public UUID getUserId() {
        return userId;
    }

    public String getOldEmail() {
        return oldEmail;
    }

    public String getNewEmail() {
        return newEmail;
    }
}
