package com.andeva.atelier.platform.operations.interfaces.rest.transform;

import com.andeva.atelier.platform.operations.application.commandservices.WorkOrderCommandFailure;
import com.andeva.atelier.platform.operations.domain.model.aggregates.WorkOrder;
import com.andeva.atelier.platform.shared.application.result.Result;
import org.springframework.context.MessageSource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;

import java.util.Locale;

/**
 * Assembler class responsible for transforming the Result of a Work Order command execution into an appropriate ResponseEntity for REST API responses. This class handles both successful outcomes (returning the Work Order resource) and various failure scenarios (mapping business errors to HTTP status codes and localized messages).
 * @author Joel Huamani Estefanero
 */
public final class ResponseEntityFromWorkOrderCommandResultAssembler {

    private ResponseEntityFromWorkOrderCommandResultAssembler() {}

    /**
     * Transforms the Result of a Work Order command execution into a ResponseEntity. On success, it returns the Work Order resource with an HTTP 200 OK status. On failure, it maps the specific command failure to an appropriate HTTP status code and retrieves a localized error message from the MessageSource, returning it in the response body as a ProblemDetail.
     * @param result The Result of the Work Order command execution, which can be either a successful Work Order aggregate or a WorkOrderCommandFailure indicating the reason for failure.
     * @param messageSource The MessageSource used to retrieve localized error messages based on the failure reason. This allows the API to return user-friendly and localized error messages in the response.
     * @return A ResponseEntity containing either the Work Order resource (on success) or a ProblemDetail with an appropriate HTTP status and localized error message (on failure).
     */
    public static ResponseEntity<?> toResponseEntityFromResult(
            Result<WorkOrder, WorkOrderCommandFailure> result,
            MessageSource messageSource,
            String branchCode) {
        return toResponseEntityFromResult(result, messageSource, branchCode, HttpStatus.OK);
    }

    public static ResponseEntity<?> toResponseEntityFromResult(
            Result<WorkOrder, WorkOrderCommandFailure> result,
            MessageSource messageSource,
            String branchCode,
            HttpStatus successStatus) {

        return result.fold(
                workOrder -> new ResponseEntity<>(
                        WorkOrderResourceFromAggregateAssembler.toResourceFromAggregate(workOrder, branchCode),
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
     * Maps a specific WorkOrderCommandFailure to an appropriate HTTP status code. This method uses a switch expression with pattern matching to determine the correct status code based on the type of failure (e.g., Duplicate, NotFound, InvalidState).
     * @param failure The WorkOrderCommandFailure instance representing the reason for failure during the execution of a Work Order command. This can be one of several specific failure types, each indicating a different error scenario.
     * @return The corresponding HttpStatus that should be returned in the API response based on the type of failure. For example, a Duplicate failure might map to HttpStatus.CONFLICT, while a NotFound failure might map to HttpStatus.NOT_FOUND.
     */
    private static HttpStatus statusFromFailure(WorkOrderCommandFailure failure) {
        return switch (failure) {
            case WorkOrderCommandFailure.Duplicate(String _) -> HttpStatus.CONFLICT;
            case WorkOrderCommandFailure.NotFound(String _) -> HttpStatus.NOT_FOUND;
            case WorkOrderCommandFailure.InvalidState(String _) -> HttpStatus.BAD_REQUEST;
        };
    }

    /**
     * Retrieves a localized error message from the MessageSource based on the specific WorkOrderCommandFailure. This method uses a switch expression with pattern matching to extract the message key from the failure instance and then attempts to retrieve the corresponding localized message from the MessageSource. If the message key is not found in the MessageSource, it falls back to returning the message key itself.
     * @param failure The WorkOrderCommandFailure instance representing the reason for failure during the execution of a Work Order command. This can be one of several specific failure types, each containing a message key that describes the error scenario.
     * @param messageSource The MessageSource used to retrieve localized error messages based on the message key extracted from the WorkOrderCommandFailure. This allows the API to return user-friendly and localized error messages in the response.
     * @return A localized error message corresponding to the specific failure reason. If the message key is not found in the MessageSource, it returns the message key itself as a fallback.
     */
    private static String messageFromFailure(WorkOrderCommandFailure failure, MessageSource messageSource) {
        String messageKey = switch (failure) {
            case WorkOrderCommandFailure.Duplicate(String message) -> message;
            case WorkOrderCommandFailure.NotFound(String message) -> message;
            case WorkOrderCommandFailure.InvalidState(String message) -> message;
        };
        try {
            return messageSource.getMessage(messageKey, null, Locale.getDefault());
        } catch (Exception e) {
            return messageKey;
        }
    }
}