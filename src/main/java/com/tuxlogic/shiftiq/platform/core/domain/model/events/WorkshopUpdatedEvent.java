package com.tuxlogic.shiftiq.platform.core.domain.model.events;

import org.springframework.context.ApplicationEvent;
import java.util.UUID;

/**
 * Domain event published when a Workshop aggregate is updated.
 */
public class WorkshopUpdatedEvent extends ApplicationEvent {
    private final UUID workshopId;

    public WorkshopUpdatedEvent(Object source, UUID workshopId) {
        super(source);
        this.workshopId = workshopId;
    }

    public UUID getWorkshopId() {
        return workshopId;
    }
}
