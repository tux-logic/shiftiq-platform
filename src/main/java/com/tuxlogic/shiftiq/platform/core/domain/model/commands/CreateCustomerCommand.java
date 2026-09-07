package com.tuxlogic.shiftiq.platform.core.domain.model.commands;

import com.tuxlogic.shiftiq.platform.core.domain.model.valueobjects.Document;
import com.tuxlogic.shiftiq.platform.core.domain.model.valueobjects.PersonName;
import com.tuxlogic.shiftiq.platform.core.domain.model.valueobjects.Phone;
import com.tuxlogic.shiftiq.platform.core.domain.model.valueobjects.UserId;

public record CreateCustomerCommand(
        UserId userId,
        boolean isCorporate,
        PersonName name,
        String businessName,
        Document document,
        Phone phone
) {
    public CreateCustomerCommand {
        if (userId == null) throw new IllegalArgumentException("core.error.userId.required");
        if (document == null) throw new IllegalArgumentException("core.error.document.required");
        if (phone == null) throw new IllegalArgumentException("core.error.phone.required");
    }
}
