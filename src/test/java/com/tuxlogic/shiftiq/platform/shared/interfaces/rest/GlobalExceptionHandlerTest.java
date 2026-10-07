package com.tuxlogic.shiftiq.platform.shared.interfaces.rest;

import com.tuxlogic.shiftiq.platform.shared.interfaces.rest.resources.ErrorResource;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.mock.http.MockHttpInputMessage;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

/**
 * Verifies that infrastructure failures are mapped to meaningful HTTP status
 * codes instead of falling through to the generic 500 handler, and that their
 * responses do not carry internal details.
 */
class GlobalExceptionHandlerTest {

    private GlobalExceptionHandler handler;

    @BeforeEach
    void setUp() {
        handler = new GlobalExceptionHandler();
    }

    @Test
    void dataIntegrityViolationReturnsConflict() {
        var response = handler.handleDataIntegrityViolation(
                new DataIntegrityViolationException("duplicate key value violates unique constraint \"branches_code_key\""));

        var body = assertErrorResponse(response, HttpStatus.CONFLICT, "RESOURCE_CONFLICT");
        assertNotNull(body.details());
    }

    @Test
    void optimisticLockingFailureReturnsConflict() {
        var response = handler.handleOptimisticLockingFailure(
                new OptimisticLockingFailureException("Row was updated or deleted by another transaction"));

        assertErrorResponse(response, HttpStatus.CONFLICT, "RESOURCE_CONFLICT");
    }

    @Test
    void unexpectedRollbackExceptionReturnsConflict() {
        var response = handler.handleUnexpectedRollbackException(
                new org.springframework.transaction.UnexpectedRollbackException("Transaction rolled back"));

        assertErrorResponse(response, HttpStatus.CONFLICT, "RESOURCE_CONFLICT");
    }

    @Test
    void entityNotFoundReturnsNotFound() {
        var response = handler.handleEntityNotFound(new EntityNotFoundException("Product 123"));

        var body = assertErrorResponse(response, HttpStatus.NOT_FOUND, "RESOURCE_NOT_FOUND");
        assertNull(body.details());
    }

    @Test
    void emptyResultDataAccessReturnsNotFound() {
        var response = handler.handleEntityNotFound(new EmptyResultDataAccessException("no result", 1));

        assertErrorResponse(response, HttpStatus.NOT_FOUND, "RESOURCE_NOT_FOUND");
    }

    @Test
    void unsupportedHttpMethodReturnsMethodNotAllowed() {
        var response = handler.handleRequestMethodNotSupported(
                new HttpRequestMethodNotSupportedException("DELETE"));

        var body = assertErrorResponse(response, HttpStatus.METHOD_NOT_ALLOWED, "METHOD_NOT_ALLOWED");
        assertNull(body.details());
    }

    @Test
    void unsupportedMediaTypeReturnsUnsupportedMediaType() {
        var response = handler.handleHttpMediaTypeNotSupported(
                new HttpMediaTypeNotSupportedException("application/xml"));

        var body = assertErrorResponse(response, HttpStatus.UNSUPPORTED_MEDIA_TYPE, "MEDIA_TYPE_NOT_SUPPORTED");
        assertNull(body.details());
    }

    @Test
    void unreadableRequestBodyReturnsBadRequestWithoutParsingDetails() {
        var response = handler.handleHttpMessageNotReadable(
                new HttpMessageNotReadableException("JSON parse error: Unrecognized token 'foo'", new MockHttpInputMessage(new byte[0])));

        var body = assertErrorResponse(response, HttpStatus.BAD_REQUEST, "VALIDATION_ERROR");
        assertNotNull(body.details());
    }

    @Test
    void argumentTypeMismatchReturnsBadRequestNamingTheParameter() {
        var response = handler.handleMethodArgumentTypeMismatch(
                new MethodArgumentTypeMismatchException("not-a-uuid", UUID.class, "branchId", null, null));

        var body = assertErrorResponse(response, HttpStatus.BAD_REQUEST, "VALIDATION_ERROR");
        assertNotNull(body.details());
    }

    private ErrorResource assertErrorResponse(ResponseEntity<?> response, HttpStatus expectedStatus, String expectedCode) {
        assertEquals(expectedStatus.value(), response.getStatusCode().value());
        var body = (ErrorResource) response.getBody();
        assertNotNull(body, "error body must be present");
        assertEquals(expectedCode, body.code());
        return body;
    }
}
