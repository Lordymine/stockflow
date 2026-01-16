package com.stockflow.shared.domain.exception;

/**
 * Base exception class for all domain-related exceptions.
 *
 * <p>All custom exceptions should extend this class to maintain
 * consistency across the domain layer and provide standard error handling.</p>
 *
 * <p>Domain exceptions represent business rule violations or invariant breaches
 * that occur during the execution of use cases.</p>
 */
public abstract class BaseDomainException extends RuntimeException {

    private final String errorCode;

    /**
     * Constructs a new domain exception with the specified error code and message.
     *
     * @param errorCode the error code that identifies this type of error
     * @param message   the detail message explaining the error
     */
    protected BaseDomainException(String errorCode, String message) {
        super(message);
        this.errorCode = errorCode;
    }

    /**
     * Constructs a new domain exception with the specified error code, message, and cause.
     *
     * @param errorCode the error code that identifies this type of error
     * @param message   the detail message explaining the error
     * @param cause     the cause of the exception
     */
    protected BaseDomainException(String errorCode, String message, Throwable cause) {
        super(message, cause);
        this.errorCode = errorCode;
    }

    /**
     * Returns the error code associated with this exception.
     * Error codes are used to identify specific error types across the API.
     *
     * @return the error code
     */
    public String getErrorCode() {
        return errorCode;
    }
}
