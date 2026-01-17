package com.stockflow.shared.domain.exception;

/**
 * Exception thrown when authentication fails or is invalid.
 *
 * <p>Maps to HTTP 401 Unauthorized.</p>
 */
public class UnauthorizedException extends BaseDomainException {

    /**
     * Constructs a new unauthorized exception with the specified error code and message.
     *
     * @param errorCode the error code that identifies this type of error
     * @param message   the detail message explaining the error
     */
    public UnauthorizedException(String errorCode, String message) {
        super(errorCode, message);
    }
}
