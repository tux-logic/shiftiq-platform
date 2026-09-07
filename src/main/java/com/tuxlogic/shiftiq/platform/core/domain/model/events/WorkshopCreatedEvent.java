package com.tuxlogic.shiftiq.platform.core.domain.model.events;

import org.springframework.context.ApplicationEvent;
import java.util.UUID;

/**
 * Domain event published when a new Workshop aggregate is created.
 */
public class WorkshopCreatedEvent extends ApplicationEvent {
    private final UUID workshopId;
    private final UUID ownerId;

    public WorkshopCreatedEvent(Object source, UUID workshopId, UUID ownerId) {
        super(source);
        this.workshopId = workshopId;
        this.ownerId = ownerId;
    }

    public UUID getWorkshopId() {
        return workshopId;
    }

    public UUID getOwnerId() {
        return ownerId;
    }
}
