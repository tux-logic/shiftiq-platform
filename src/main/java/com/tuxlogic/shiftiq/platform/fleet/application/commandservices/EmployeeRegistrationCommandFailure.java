package com.tuxlogic.shiftiq.platform.fleet.application.commandservices;

public enum EmployeeRegistrationCommandFailure {
    REGISTRATION_ALREADY_EXISTS,
    REGISTRATION_NOT_FOUND,
    INVALID_REGISTRATION_DATA,
    INVALID_STATUS_TRANSITION,
    SPECIALTY_NOT_IN_CATALOG
}
