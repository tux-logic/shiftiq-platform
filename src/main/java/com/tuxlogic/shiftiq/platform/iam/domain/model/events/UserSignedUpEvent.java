package com.tuxlogic.shiftiq.platform.iam.domain.model.events;

import org.springframework.context.ApplicationEvent;
import java.util.UUID;

/**
 * Domain event published when a new user account is registered.
 */
public class UserSignedUpEvent extends ApplicationEvent {
    private final UUID userId;
    private final String email;

    public UserSignedUpEvent(Object source, UUID userId, String email) {
        super(source);
        this.userId = userId;
        this.email = email;
    }

    public UUID getUserId() {
        return userId;
    }

    public String getEmail() {
        return email;
    }
}
