package com.tuxlogic.shiftiq.platform.core.domain.model.commands;

import com.tuxlogic.shiftiq.platform.core.domain.model.valueobjects.OwnerId;

public record DeleteOwnerCommand(OwnerId ownerId) {
    public DeleteOwnerCommand {
        if (ownerId == null) throw new IllegalArgumentException("core.error.ownerId.required");
    }
}
