package com.tuxlogic.shiftiq.platform.fleet.application.queryservices;

public sealed interface EmployeeRegistrationQueryFailure {
    record NotFound(String message) implements EmployeeRegistrationQueryFailure {}
}
