package com.tuxlogic.shiftiq.platform.core.domain.model.commands;

import com.tuxlogic.shiftiq.platform.core.domain.model.valueobjects.Document;
import com.tuxlogic.shiftiq.platform.core.domain.model.valueobjects.EmployeeId;
import com.tuxlogic.shiftiq.platform.core.domain.model.valueobjects.PersonName;
import com.tuxlogic.shiftiq.platform.core.domain.model.valueobjects.Phone;

public record UpdateEmployeeCommand(
        EmployeeId employeeId,
        PersonName name,
        Document document,
        Phone phone
) {
    public UpdateEmployeeCommand {
        if (employeeId == null) throw new IllegalArgumentException("core.error.employeeId.required");
        if (name == null) throw new IllegalArgumentException("core.error.personName.required");
        if (document == null) throw new IllegalArgumentException("core.error.document.required");
        if (phone == null) throw new IllegalArgumentException("core.error.phone.required");
    }
}
