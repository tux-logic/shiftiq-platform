package com.andeva.atelier.platform.core.interfaces.rest.transform;

import com.andeva.atelier.platform.core.domain.model.commands.CreateEmployeeCommand;
import com.andeva.atelier.platform.core.domain.model.valueobjects.Document;
import com.andeva.atelier.platform.core.domain.model.valueobjects.PersonName;
import com.andeva.atelier.platform.core.domain.model.valueobjects.Phone;
import com.andeva.atelier.platform.core.domain.model.valueobjects.UserId;
import com.andeva.atelier.platform.core.interfaces.rest.resources.CreateEmployeeResource;

public class CreateEmployeeCommandFromResourceAssembler {
    public static CreateEmployeeCommand toCommandFromResource(CreateEmployeeResource resource) {
        return new CreateEmployeeCommand(
                new UserId(resource.userId()),
                new PersonName(resource.firstName(), resource.lastName()),
                new Document(resource.documentType(), resource.documentNumber()),
                new Phone(resource.phone())
        );
    }
}
