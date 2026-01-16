package com.stockflow.shared.infrastructure.web;

import com.stockflow.shared.application.dto.ApiErrorResponse;
import com.stockflow.shared.domain.exception.*;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;

import java.util.ArrayList;
import java.util.List;

/**
 * Global exception handler for all REST controllers.
 *
 * <p>This class handles exceptions thrown by any controller and converts them
 * into standardized error responses following the API contract.</p>
 *
 * <p>Handles:</p>
 * <ul>
 *   <li>Domain exceptions (NotFoundException, ConflictException, etc.)</li>
 *   <li>Validation exceptions (MethodArgumentNotValidException)</li>
 *   <li>Security exceptions (AccessDeniedException)</li>
 *   <li>Concurrency exceptions (OptimisticLockingFailureException)</li>
 *   <li>Generic server errors</li>
 * </ul>
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * Handles NotFoundException.
     */
    @ExceptionHandler(NotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ResponseEntity<ApiErrorResponse> handleNotFoundException(
        NotFoundException ex,
        WebRequest request
    ) {
        ApiErrorResponse response = ApiErrorResponse.of(
            ex.getErrorCode(),
            ex.getMessage()
        );
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
    }

    /**
     * Handles ConflictException.
     */
    @ExceptionHandler(ConflictException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    public ResponseEntity<ApiErrorResponse> handleConflictException(
        ConflictException ex,
        WebRequest request
    ) {
        ApiErrorResponse response = ApiErrorResponse.of(
            ex.getErrorCode(),
            ex.getMessage()
        );
        return ResponseEntity.status(HttpStatus.CONFLICT).body(response);
    }

    /**
     * Handles ForbiddenException.
     */
    @ExceptionHandler(ForbiddenException.class)
    @ResponseStatus(HttpStatus.FORBIDDEN)
    public ResponseEntity<ApiErrorResponse> handleForbiddenException(
        ForbiddenException ex,
        WebRequest request
    ) {
        ApiErrorResponse response = ApiErrorResponse.of(
            ex.getErrorCode(),
            ex.getMessage()
        );
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(response);
    }

    /**
     * Handles BadRequestException.
     */
    @ExceptionHandler(BadRequestException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ResponseEntity<ApiErrorResponse> handleBadRequestException(
        BadRequestException ex,
        WebRequest request
    ) {
        ApiErrorResponse response = ApiErrorResponse.of(
            ex.getErrorCode(),
            ex.getMessage()
        );
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    /**
     * Handles ValidationException.
     */
    @ExceptionHandler(ValidationException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ResponseEntity<ApiErrorResponse> handleValidationException(
        ValidationException ex,
        WebRequest request
    ) {
        ApiErrorResponse response = ApiErrorResponse.of(
            ex.getErrorCode(),
            ex.getMessage(),
            ex.hasErrors() ? ex.getValidationErrors() : null
        );
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    /**
     * Handles Bean Validation errors.
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ResponseEntity<ApiErrorResponse> handleValidationException(
        MethodArgumentNotValidException ex,
        WebRequest request
    ) {
        ApiErrorResponse response = ApiErrorResponse.fromFieldErrors(ex.getBindingResult().getFieldErrors());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    /**
     * Handles AccessDeniedException (Spring Security).
     */
    @ExceptionHandler(AccessDeniedException.class)
    @ResponseStatus(HttpStatus.FORBIDDEN)
    public ResponseEntity<ApiErrorResponse> handleAccessDeniedException(
        AccessDeniedException ex,
        WebRequest request
    ) {
        ApiErrorResponse response = ApiErrorResponse.of(
            "ACCESS_DENIED",
            "You do not have permission to access this resource"
        );
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(response);
    }

    /**
     * Handles OptimisticLockingFailureException (concurrent modification).
     */
    @ExceptionHandler(OptimisticLockingFailureException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    public ResponseEntity<ApiErrorResponse> handleOptimisticLockingFailureException(
        OptimisticLockingFailureException ex,
        WebRequest request
    ) {
        ApiErrorResponse response = ApiErrorResponse.of(
            "CONCURRENT_MODIFICATION",
            "This resource was modified by another user. Please refresh and try again."
        );
        return ResponseEntity.status(HttpStatus.CONFLICT).body(response);
    }

    /**
     * Handles all other exceptions (fallback).
     */
    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ResponseEntity<ApiErrorResponse> handleGenericException(
        Exception ex,
        WebRequest request
    ) {
        // Log the full exception for debugging
        ex.printStackTrace();

        ApiErrorResponse response = ApiErrorResponse.of(
            "INTERNAL_SERVER_ERROR",
            "An unexpected error occurred. Please try again later."
        );
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
    }
}
