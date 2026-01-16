package com.stockflow.shared.domain.exception;

/**
 * Exception thrown when a request conflicts with the current state of the server.
 *
 * <p>This exception typically indicates:</p>
 * <ul>
 *   <li>Concurrent modification conflicts (optimistic locking failures)</li>
 *   <li>Duplicate resource creation attempts</li>
 *   <li>Business rule violations that prevent the operation</li>
 *   <li>State inconsistencies</li>
 * </ul>
 *
 * <p>Maps to HTTP 409 Conflict.</p>
 */
public class ConflictException extends BaseDomainException {

    /**
     * Constructs a new conflict exception with the specified error code and message.
     *
     * @param errorCode the error code (e.g., "RESOURCE_ALREADY_EXISTS", "CONCURRENT_MODIFICATION")
     * @param message   the detail message explaining the conflict
     */
    public ConflictException(String errorCode, String message) {
        super(errorCode, message);
    }

    /**
     * Constructs a new conflict exception with the specified error code, message, and cause.
     *
     * @param errorCode the error code
     * @param message   the detail message
     * @param cause     the cause of the exception
     */
    public ConflictException(String errorCode, String message, Throwable cause) {
        super(errorCode, message, cause);
    }
}
