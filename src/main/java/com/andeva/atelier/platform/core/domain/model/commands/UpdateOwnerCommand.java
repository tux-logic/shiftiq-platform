package com.andeva.atelier.platform.core.domain.model.commands;

import com.andeva.atelier.platform.core.domain.model.valueobjects.Document;
import com.andeva.atelier.platform.core.domain.model.valueobjects.PersonName;
import com.andeva.atelier.platform.core.domain.model.valueobjects.Phone;
import com.andeva.atelier.platform.core.domain.model.valueobjects.OwnerId;

public record UpdateOwnerCommand(
        OwnerId ownerId,
        PersonName name,
        Document document,
        Phone phone
) {
}
