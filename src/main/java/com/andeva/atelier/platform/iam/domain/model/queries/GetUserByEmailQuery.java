package com.andeva.atelier.platform.iam.domain.model.queries;

import com.andeva.atelier.platform.iam.domain.model.valueobjects.EmailAddress;

public record GetUserByEmailQuery(EmailAddress email) {
}
