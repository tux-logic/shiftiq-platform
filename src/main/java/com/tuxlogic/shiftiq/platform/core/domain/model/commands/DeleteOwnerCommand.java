package com.tuxlogic.shiftiq.platform.core.domain.model.commands;

import com.tuxlogic.shiftiq.platform.core.domain.model.valueobjects.OwnerId;

public record DeleteOwnerCommand(OwnerId ownerId) {
}
