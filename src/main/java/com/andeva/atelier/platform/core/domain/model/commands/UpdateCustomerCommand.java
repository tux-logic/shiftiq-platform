package com.andeva.atelier.platform.core.domain.model.commands;

import com.andeva.atelier.platform.core.domain.model.valueobjects.Document;
import com.andeva.atelier.platform.core.domain.model.valueobjects.PersonName;
import com.andeva.atelier.platform.core.domain.model.valueobjects.Phone;
import com.andeva.atelier.platform.shared.domain.model.valueobjects.CustomerId;

public record UpdateCustomerCommand(
        CustomerId customerId,
        PersonName name,
        String businessName,
        Document document,
        Phone phone
) {
}
