package com.tuxlogic.shiftiq.platform.iam.domain.model.queries;

import com.tuxlogic.shiftiq.platform.iam.domain.model.valueobjects.EmailAddress;

public record GetUserByEmailQuery(EmailAddress email) {
}
