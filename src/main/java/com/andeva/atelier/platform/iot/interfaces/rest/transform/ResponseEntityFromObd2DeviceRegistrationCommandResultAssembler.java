package com.andeva.atelier.platform.iot.interfaces.rest.transform;

import com.andeva.atelier.platform.iot.application.commandservices.Obd2DeviceRegistrationCommandFailure;
import com.andeva.atelier.platform.iot.domain.model.aggregates.Obd2DeviceRegistration;
import com.andeva.atelier.platform.shared.application.result.Result;
import org.springframework.context.MessageSource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;

import org.springframework.context.i18n.LocaleContextHolder;

/**
 * Assembler class responsible for transforming the Result of an OBD2 Device Registration command execution into an appropriate ResponseEntity for REST API responses.
 */
public final class ResponseEntityFromObd2DeviceRegistrationCommandResultAssembler {

    private ResponseEntityFromObd2DeviceRegistrationCommandResultAssembler() {}

    /**
     * Transforms the Result of an OBD2 Device Registration command execution into a ResponseEntity with a custom success status.
     * @param result The Result of the command execution
     * @param successStatus The HttpStatus to return on success
     * @param messageSource The MessageSource used to retrieve localized error messages
     * @return A ResponseEntity containing either the Obd2DeviceRegistrationResource (on success) or a ProblemDetail with localized error message (on failure)
     */
    public static ResponseEntity<?> toResponseEntityFromResult(
            Result<Obd2DeviceRegistration, Obd2DeviceRegistrationCommandFailure> result,
            HttpStatus successStatus,
            MessageSource messageSource) {

        return result.fold(
                registration -> new ResponseEntity<>(
                        Obd2DeviceRegistrationResourceFromAggregateAssembler.toResourceFromAggregate(registration),
                        successStatus
                ),
                failure -> {
                    HttpStatus status = statusFromFailure(failure);
                    String localizedMessage = messageFromFailure(failure, messageSource);
                    return ResponseEntity.status(status).body(
                            ProblemDetail.forStatusAndDetail(status, localizedMessage)
                    );
                }
        );
    }

    /**
     * Transforms the Result of an OBD2 Device Registration command execution into a ResponseEntity.
     * @param result The Result of the command execution
     * @param messageSource The MessageSource used to retrieve localized error messages
     * @return A ResponseEntity containing either the Obd2DeviceRegistrationResource (on success) or a ProblemDetail with localized error message (on failure)
     */
    public static ResponseEntity<?> toResponseEntityFromResult(
            Result<Obd2DeviceRegistration, Obd2DeviceRegistrationCommandFailure> result,
            MessageSource messageSource) {
        return toResponseEntityFromResult(result, HttpStatus.CREATED, messageSource);
    }

    private static HttpStatus statusFromFailure(Obd2DeviceRegistrationCommandFailure failure) {
        return switch (failure) {
            case Obd2DeviceRegistrationCommandFailure.NotFound(String _) -> HttpStatus.NOT_FOUND;
            case Obd2DeviceRegistrationCommandFailure.InvalidState(String _) -> HttpStatus.BAD_REQUEST;
        };
    }

    private static String messageFromFailure(Obd2DeviceRegistrationCommandFailure failure, MessageSource messageSource) {
        String messageKey = switch (failure) {
            case Obd2DeviceRegistrationCommandFailure.NotFound(String message) -> message;
            case Obd2DeviceRegistrationCommandFailure.InvalidState(String message) -> message;
        };
        try {
            return messageSource.getMessage(messageKey, null, LocaleContextHolder.getLocale());
        } catch (Exception e) {
            return messageKey;
        }
    }
}
