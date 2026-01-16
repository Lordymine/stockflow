package com.stockflow.shared.domain.exception;

/**
 * Exception thrown when a request contains invalid data or parameters.
 *
 * <p>This exception typically indicates client errors such as:</p>
 * <ul>
 *   <li>Malformed request syntax</li>
 *   <li>Invalid request parameters</li>
 *   <li>Missing required fields</li>
 *   <li>Invalid data format</li>
 * </ul>
 *
 * <p>Maps to HTTP 400 Bad Request.</p>
 */
public class BadRequestException extends BaseDomainException {

    /**
     * Constructs a new bad request exception with the specified error code and message.
     *
     * @param errorCode the error code (e.g., "INVALID_REQUEST", "MALFORMED_DATA")
     * @param message   the detail message explaining the validation error
     */
    public BadRequestException(String errorCode, String message) {
        super(errorCode, message);
    }

    /**
     * Constructs a new bad request exception with the specified error code, message, and cause.
     *
     * @param errorCode the error code
     * @param message   the detail message
     * @param cause     the cause of the exception
     */
    public BadRequestException(String errorCode, String message, Throwable cause) {
        super(errorCode, message, cause);
    }
}
