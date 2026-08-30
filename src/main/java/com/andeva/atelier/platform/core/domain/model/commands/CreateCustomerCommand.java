package com.andeva.atelier.platform.core.domain.model.commands;

import com.andeva.atelier.platform.core.domain.model.valueobjects.Document;
import com.andeva.atelier.platform.core.domain.model.valueobjects.PersonName;
import com.andeva.atelier.platform.core.domain.model.valueobjects.Phone;
import com.andeva.atelier.platform.core.domain.model.valueobjects.UserId;

public record CreateCustomerCommand(
        UserId userId,
        boolean isCorporate,
        PersonName name,
        String businessName,
        Document document,
        Phone phone
) {
}
