package com.stockflow.shared.domain.exception;

import java.util.ArrayList;
import java.util.List;

/**
 * Exception thrown when domain validation fails.
 *
 * <p>This exception represents business rule violations or validation errors
 * that occur during the execution of domain operations.</p>
 *
 * <p>Unlike BadRequestException (which handles input validation),
 * ValidationException handles domain-level validation such as:</p>
 * <ul>
 *   <li>Business rule violations</li>
 *   <li>Domain invariant breaches</li>
 *   <li>Complex validation involving multiple fields</li>
 *   <li>State-dependent validation rules</li>
 * </ul>
 *
 * <p>Maps to HTTP 400 Bad Request.</p>
 */
public class ValidationException extends BaseDomainException {

    private final List<String> validationErrors;

    /**
     * Constructs a new validation exception with the specified error code and message.
     *
     * @param errorCode the error code (e.g., "VALIDATION_ERROR", "INVALID_STATE")
     * @param message   the detail message explaining the validation error
     */
    public ValidationException(String errorCode, String message) {
        super(errorCode, message);
        this.validationErrors = new ArrayList<>();
    }

    /**
     * Constructs a new validation exception with the specified error code, message, and validation errors.
     *
     * @param errorCode        the error code
     * @param message          the detail message
     * @param validationErrors list of specific validation error messages
     */
    public ValidationException(String errorCode, String message, List<String> validationErrors) {
        super(errorCode, message);
        this.validationErrors = validationErrors != null ? validationErrors : new ArrayList<>();
    }

    /**
     * Returns the list of validation error details.
     *
     * @return the list of validation errors
     */
    public List<String> getValidationErrors() {
        return validationErrors;
    }

    /**
     * Checks if there are detailed validation errors.
     *
     * @return true if there are validation errors, false otherwise
     */
    public boolean hasErrors() {
        return !validationErrors.isEmpty();
    }
}
