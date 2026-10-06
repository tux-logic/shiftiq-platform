package com.tuxlogic.shiftiq.platform.shared.interfaces.rest;

import com.tuxlogic.shiftiq.platform.shared.application.result.ApplicationError;
import com.tuxlogic.shiftiq.platform.shared.interfaces.rest.transform.ErrorResponseAssembler;
import jakarta.persistence.EntityNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.text.MessageFormat;
import java.util.MissingResourceException;
import java.util.ResourceBundle;
import java.util.regex.Pattern;

/**
 * Global exception handler for REST API.
 * Provides centralized exception handling for the entire application,
 * ensuring all unhandled exceptions are translated to consistent
 * HTTP responses via the shared error assembly pattern.
 *
 * <p>Responses are intentionally free of internal information: unexpected
 * failures are logged in full on the server and rendered to the client as a
 * generic localized message, so stack traces, SQL text and exception messages
 * never reach the API consumer.</p>
 */
@RestControllerAdvice
public class GlobalExceptionHandler {
    private static final Logger LOGGER = LoggerFactory.getLogger(GlobalExceptionHandler.class);
    private static final String MESSAGES_BASENAME = "messages";

    /**
     * Matches i18n message keys such as {@code core.error.branch.notFound}.
     * Anything else is treated as a literal, user-facing message.
     */
    private static final Pattern MESSAGE_KEY_PATTERN = Pattern.compile("^[a-zA-Z0-9_]+(\\.[a-zA-Z0-9_]+)+$");

    /**
     * Handles validation exceptions from Spring's request body validation.
     * Maps validation failure to a standardized error response.
     *
     * @param ex the validation exception from @Valid binding
     * @return error response with BAD_REQUEST status
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<?> handleMethodArgumentNotValid(MethodArgumentNotValidException ex) {
        var fieldErrors = ex.getBindingResult().getFieldErrors();
        var validationPrefix = resolveMessageOrDefault("validation.field.prefix", "Field");
        var errorDetails = fieldErrors.isEmpty()
                ? resolveMessageOrDefault("validation.request.failed", "Request validation failed")
                : fieldErrors.stream()
                        .map(error -> "%s %s: %s".formatted(
                                validationPrefix,
                                error.getField(),
                                resolveMessageOrDefault(error.getDefaultMessage(), error.getDefaultMessage())
                        ))
                        .reduce((a, b) -> a + "; " + b)
                        .orElse(resolveMessageOrDefault("validation.request.failed", "Request validation failed"));

        var applicationError = ApplicationError.validationError("request-body", errorDetails);
        return ErrorResponseAssembler.toErrorResponseFromApplicationError(applicationError);
    }

    /**
     * Handles invalid request arguments such as malformed UUID path or payload values.
     *
     * @param ex the illegal argument exception
     * @return error response with BAD_REQUEST status
     */
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<?> handleIllegalArgumentException(IllegalArgumentException ex) {
        var detailMessage = resolveClientSafeMessage(ex.getMessage());
        var applicationError = ApplicationError.validationError(
                resolveMessageOrDefault("validation.request.argument", "request-argument"),
                detailMessage
        );
        return ErrorResponseAssembler.toErrorResponseFromApplicationError(applicationError);
    }

    /**
     * Handles request bodies that cannot be parsed (malformed JSON or an
     * unexpected payload structure). The parsing detail is only logged, so
     * that Jackson internals never reach the client.
     *
     * @param ex the unreadable message exception
     * @return error response with BAD_REQUEST status
     */
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<?> handleHttpMessageNotReadable(HttpMessageNotReadableException ex) {
        LOGGER.debug("Unreadable request body: {}", ex.getMessage());
        var applicationError = ApplicationError.validationError(
                "request-body",
                resolveMessageOrDefault("error.validation.malformed-body", "The request body could not be parsed.")
        );
        return ErrorResponseAssembler.toErrorResponseFromApplicationError(applicationError);
    }

    /**
     * Handles values that do not match the expected type, such as a path
     * variable that is not a valid UUID.
     *
     * @param ex the type mismatch exception
     * @return error response with BAD_REQUEST status
     */
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<?> handleMethodArgumentTypeMismatch(MethodArgumentTypeMismatchException ex) {
        LOGGER.debug("Invalid value '{}' for parameter '{}'", ex.getValue(), ex.getName());
        var applicationError = ApplicationError.validationError(
                ex.getName() != null ? ex.getName() : "request-argument",
                resolveMessageOrDefault("error.validation.invalid-argument", "The provided value is not valid for this parameter.")
        );
        return ErrorResponseAssembler.toErrorResponseFromApplicationError(applicationError);
    }

    /**
     * Handles constraint violations reported by the database (duplicate unique
     * values, foreign keys, check constraints). The SQL detail is only logged.
     *
     * @param ex the data integrity exception
     * @return error response with CONFLICT status
     */
    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<?> handleDataIntegrityViolation(DataIntegrityViolationException ex) {
        LOGGER.warn("Data integrity violation: {}", ex.getMessage());
        var applicationError = ApplicationError.conflict(
                "resource",
                resolveMessageOrDefault("error.conflict.integrity", "The change conflicts with an existing record and was not applied.")
        );
        return ErrorResponseAssembler.toErrorResponseFromApplicationError(applicationError);
    }

    /**
     * Handles optimistic locking conflicts, raised when the aggregate was
     * modified concurrently by another operation.
     *
     * @param ex the optimistic locking exception
     * @return error response with CONFLICT status
     */
    @ExceptionHandler(OptimisticLockingFailureException.class)
    public ResponseEntity<?> handleOptimisticLockingFailure(OptimisticLockingFailureException ex) {
        LOGGER.warn("Optimistic locking conflict: {}", ex.getMessage());
        var applicationError = ApplicationError.conflict(
                "resource",
                resolveMessageOrDefault("error.conflict.optimistic", "The record was modified by another operation. Reload it and try again.")
        );
        return ErrorResponseAssembler.toErrorResponseFromApplicationError(applicationError);
    }

    /**
     * Handles entities requested from the persistence layer that do not exist.
     *
     * @param ex the not found exception
     * @return error response with NOT_FOUND status
     */
    @ExceptionHandler({EntityNotFoundException.class, EmptyResultDataAccessException.class})
    public ResponseEntity<?> handleEntityNotFound(Exception ex) {
        LOGGER.debug("Entity not found: {}", ex.getMessage());
        var applicationError = ApplicationError.notFound("resource", null);
        return ErrorResponseAssembler.toErrorResponseFromApplicationError(applicationError);
    }

    /**
     * Handles requests using an HTTP method the endpoint does not support.
     *
     * @param ex the method not supported exception
     * @return error response with METHOD_NOT_ALLOWED status
     */
    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<?> handleRequestMethodNotSupported(HttpRequestMethodNotSupportedException ex) {
        LOGGER.debug("HTTP method {} not supported", ex.getMethod());
        var applicationError = new ApplicationError(
                "METHOD_NOT_ALLOWED",
                resolveMessageOrDefault("error.method-not-allowed.message", "The HTTP method is not supported for this endpoint.")
        );
        return ErrorResponseAssembler.toErrorResponseFromApplicationError(applicationError);
    }

    /**
     * Handles request payloads declared with an unsupported content type.
     *
     * @param ex the media type not supported exception
     * @return error response with UNSUPPORTED_MEDIA_TYPE status
     */
    @ExceptionHandler(HttpMediaTypeNotSupportedException.class)
    public ResponseEntity<?> handleHttpMediaTypeNotSupported(HttpMediaTypeNotSupportedException ex) {
        LOGGER.debug("Unsupported content type: {}", ex.getContentType());
        var applicationError = new ApplicationError(
                "MEDIA_TYPE_NOT_SUPPORTED",
                resolveMessageOrDefault("error.media-type-not-supported.message", "The content type of the request is not supported.")
        );
        return ErrorResponseAssembler.toErrorResponseFromApplicationError(applicationError);
    }

    /**
     * Handles access denied exceptions when authenticated user lacks permissions.
     * Maps to a 403 FORBIDDEN response.
     *
     * @param ex the access denied exception
     * @return error response with FORBIDDEN status
     */
    @ExceptionHandler(org.springframework.security.access.AccessDeniedException.class)
    public ResponseEntity<?> handleAccessDeniedException(org.springframework.security.access.AccessDeniedException ex) {
        LOGGER.debug("Access denied: {}", ex.getMessage());
        var applicationError = new ApplicationError(
                "ACCESS_DENIED",
                resolveMessageOrDefault("error.access-denied.message", "Access denied: insufficient permissions")
        );
        return ErrorResponseAssembler.toErrorResponseFromApplicationError(applicationError);
    }

    /**
     * Handles unexpected runtime exceptions not caught by specific handlers.
     * Maps to a generic unexpected error response; the cause is only logged.
     *
     * @param ex the unhandled runtime exception
     * @return error response with INTERNAL_SERVER_ERROR status
     */
    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<?> handleRuntimeException(RuntimeException ex) {
        LOGGER.error("Unhandled runtime exception", ex);
        return toUnexpectedErrorResponse();
    }

    /**
     * Handles all other exceptions not matched by specific handlers.
     * Provides a final fallback for any unexpected exception type.
     *
     * @param ex the exception
     * @return error response with INTERNAL_SERVER_ERROR status
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<?> handleException(Exception ex) {
        LOGGER.error("Unhandled exception", ex);
        return toUnexpectedErrorResponse();
    }

    private ResponseEntity<?> toUnexpectedErrorResponse() {
        var applicationError = ApplicationError.unexpected(
                resolveMessageOrDefault("error.unexpected.context", "global-exception-handler"),
                null
        );
        return ErrorResponseAssembler.toErrorResponseFromApplicationError(applicationError);
    }

    /**
     * Makes an exception message safe to render.
     *
     * <p>Messages are either i18n keys (dotted tokens such as
     * {@code core.error.branch.notFound}) or literals written for the API
     * consumer ("Branch ID is required"). Keys are resolved through the
     * resource bundle and, when unknown, replaced by a generic message so a
     * raw key never reaches the client. Literals are returned as they are,
     * because in this codebase they are validation text written on purpose
     * for the caller. Stack traces and other unexpected messages are handled
     * by the runtime/exception handlers above and never reach this method
     * with internal content.</p>
     */
    private String resolveClientSafeMessage(String message) {
        var genericMessage = resolveMessageOrDefault("validation.request.failed", "Request validation failed");
        if (message == null || message.isBlank()) {
            return genericMessage;
        }
        if (MESSAGE_KEY_PATTERN.matcher(message).matches()) {
            var resolved = resolveMessageOrNull(message);
            if (resolved != null) {
                return resolved;
            }
            LOGGER.warn("Unknown message key raised as IllegalArgumentException: {}", message);
            return genericMessage;
        }
        return message;
    }

    private String resolveMessageOrNull(String key) {
        if (key == null) {
            return null;
        }
        try {
            var bundle = ResourceBundle.getBundle(MESSAGES_BASENAME, LocaleContextHolder.getLocale());
            if (!bundle.containsKey(key)) {
                return null;
            }
            return bundle.getString(key);
        } catch (MissingResourceException ex) {
            return null;
        }
    }

    private String resolveMessageOrDefault(String key, String defaultValue, Object... args) {
        try {
            var bundle = ResourceBundle.getBundle(MESSAGES_BASENAME, LocaleContextHolder.getLocale());
            if (!bundle.containsKey(key)) {
                return defaultValue;
            }
            return MessageFormat.format(bundle.getString(key), args);
        } catch (MissingResourceException ex) {
            return defaultValue;
        }
    }
}
