package com.tuxlogic.shiftiq.platform.operations.application.commandservices;

public sealed interface ServiceCommandFailure {
    record NotFound(String message) implements ServiceCommandFailure {}
    record InvalidData(String message) implements ServiceCommandFailure {}
}
