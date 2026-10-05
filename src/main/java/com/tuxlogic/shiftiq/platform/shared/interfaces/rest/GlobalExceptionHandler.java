package com.tuxlogic.shiftiq.platform.shared.interfaces.rest;

import com.tuxlogic.shiftiq.platform.shared.application.result.ApplicationError;
import com.tuxlogic.shiftiq.platform.shared.interfaces.rest.transform.ErrorResponseAssembler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

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
