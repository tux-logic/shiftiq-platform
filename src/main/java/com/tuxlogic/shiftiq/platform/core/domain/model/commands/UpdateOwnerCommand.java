package com.tuxlogic.shiftiq.platform.core.domain.model.commands;

import com.tuxlogic.shiftiq.platform.core.domain.model.valueobjects.Document;
import com.tuxlogic.shiftiq.platform.core.domain.model.valueobjects.PersonName;
import com.tuxlogic.shiftiq.platform.core.domain.model.valueobjects.Phone;
import com.tuxlogic.shiftiq.platform.core.domain.model.valueobjects.OwnerId;

public record UpdateOwnerCommand(
        OwnerId ownerId,
        PersonName name,
        Document document,
        Phone phone
) {
    public UpdateOwnerCommand {
        if (ownerId == null) throw new IllegalArgumentException("core.error.ownerId.required");
        if (name == null) throw new IllegalArgumentException("core.error.personName.required");
        if (document == null) throw new IllegalArgumentException("core.error.document.required");
        if (phone == null) throw new IllegalArgumentException("core.error.phone.required");
    }
}
