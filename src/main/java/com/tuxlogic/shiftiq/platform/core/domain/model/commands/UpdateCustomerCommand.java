package com.tuxlogic.shiftiq.platform.core.domain.model.commands;

import com.tuxlogic.shiftiq.platform.core.domain.model.valueobjects.Document;
import com.tuxlogic.shiftiq.platform.core.domain.model.valueobjects.PersonName;
import com.tuxlogic.shiftiq.platform.core.domain.model.valueobjects.Phone;
import com.tuxlogic.shiftiq.platform.shared.domain.model.valueobjects.CustomerId;

public record UpdateCustomerCommand(
        CustomerId customerId,
        PersonName name,
        String businessName,
        Document document,
        Phone phone
) {
    public UpdateCustomerCommand {
        if (customerId == null) throw new IllegalArgumentException("core.error.customerId.required");
        if (document == null) throw new IllegalArgumentException("core.error.document.required");
        if (phone == null) throw new IllegalArgumentException("core.error.phone.required");
    }
}
