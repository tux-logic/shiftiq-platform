package com.tuxlogic.shiftiq.platform.core.domain.model.events;

import org.springframework.context.ApplicationEvent;
import java.util.UUID;

/**
 * Domain event published when an Owner profile is updated.
 */
public class OwnerUpdatedEvent extends ApplicationEvent {
    private final UUID ownerId;

    public OwnerUpdatedEvent(Object source, UUID ownerId) {
        super(source);
        this.ownerId = ownerId;
    }

    public UUID getOwnerId() {
        return ownerId;
    }
}
