package com.andeva.atelier.platform.core.domain.model.commands;

import com.andeva.atelier.platform.core.domain.model.valueobjects.OwnerId;

public record DeleteOwnerCommand(OwnerId ownerId) {
}
