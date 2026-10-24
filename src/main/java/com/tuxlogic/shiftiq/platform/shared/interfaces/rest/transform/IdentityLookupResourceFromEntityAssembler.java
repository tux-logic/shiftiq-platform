package com.tuxlogic.shiftiq.platform.shared.interfaces.rest.transform;

import com.tuxlogic.shiftiq.platform.shared.application.outboundservices.identity.CompanyIdentityResult;
import com.tuxlogic.shiftiq.platform.shared.application.outboundservices.identity.PersonIdentityResult;
import com.tuxlogic.shiftiq.platform.shared.interfaces.rest.resources.IdentityLookupResource;

public class IdentityLookupResourceFromEntityAssembler {

    public static IdentityLookupResource toResourceFromPerson(PersonIdentityResult person) {
        return new IdentityLookupResource(
                person.documentType(),
                person.documentNumber(),
                person.firstName(),
                person.lastName(),
                person.fullName(),
                null,
                null,
                null,
                person.address(),
                person.department(),
                person.province(),
                person.district(),
                null,
                person.valid(),
                null,
                null
        );
    }

    public static IdentityLookupResource toResourceFromCompany(CompanyIdentityResult company) {
        return new IdentityLookupResource(
                "RUC",
                company.ruc(),
                null,
                null,
                company.businessName(),
                company.businessName(),
                company.status(),
                company.condition(),
                company.address(),
                company.department(),
                company.province(),
                company.district(),
                company.ubigeo(),
                company.valid(),
                company.active(),
                company.habido()
        );
    }
}
