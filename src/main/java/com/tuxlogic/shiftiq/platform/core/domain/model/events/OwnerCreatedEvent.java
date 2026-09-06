package com.tuxlogic.shiftiq.platform.core.domain.model.events;

import org.springframework.context.ApplicationEvent;
import java.util.UUID;

/**
 * Domain event published when a new Owner profile is created.
 */
public class OwnerCreatedEvent extends ApplicationEvent {
    private final UUID ownerId;
    private final UUID userId;

    public OwnerCreatedEvent(Object source, UUID ownerId, UUID userId) {
        super(source);
        this.ownerId = ownerId;
        this.userId = userId;
    }

    public UUID getOwnerId() {
        return ownerId;
    }

    public UUID getUserId() {
        return userId;
    }
}
