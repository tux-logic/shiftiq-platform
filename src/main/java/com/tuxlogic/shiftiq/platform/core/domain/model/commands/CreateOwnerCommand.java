package com.tuxlogic.shiftiq.platform.core.domain.model.commands;

import com.tuxlogic.shiftiq.platform.core.domain.model.valueobjects.Document;
import com.tuxlogic.shiftiq.platform.core.domain.model.valueobjects.PersonName;
import com.tuxlogic.shiftiq.platform.core.domain.model.valueobjects.Phone;
import com.tuxlogic.shiftiq.platform.core.domain.model.valueobjects.UserId;

public record CreateOwnerCommand(
        UserId userId,
        PersonName name,
        Document document,
        Phone phone
) {
    public CreateOwnerCommand {
        if (userId == null) throw new IllegalArgumentException("core.error.userId.required");
        if (name == null) throw new IllegalArgumentException("core.error.personName.required");
        if (document == null) throw new IllegalArgumentException("core.error.document.required");
        if (phone == null) throw new IllegalArgumentException("core.error.phone.required");
    }
}
