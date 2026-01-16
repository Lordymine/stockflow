package com.stockflow.shared.domain.exception;

/**
 * Exception thrown when a requested resource cannot be found.
 *
 * <p>This exception indicates that the resource identified by the request
 * does not exist or is not accessible to the current user.</p>
 *
 * <p>Common scenarios:</p>
 * <ul>
 *   <li>Entity with given ID does not exist</li>
 *   <li>User attempts to access a resource from another tenant</li>
 *   <li>Referenced entity in a relationship does not exist</li>
 * </ul>
 *
 * <p>Maps to HTTP 404 Not Found.</p>
 */
public class NotFoundException extends BaseDomainException {

    /**
     * Constructs a new not found exception with the specified error code and message.
     *
     * @param errorCode the error code (e.g., "RESOURCE_NOT_FOUND", "USER_NOT_FOUND")
     * @param message   the detail message explaining what resource was not found
     */
    public NotFoundException(String errorCode, String message) {
        super(errorCode, message);
    }

    /**
     * Constructs a new not found exception with the specified error code, message, and cause.
     *
     * @param errorCode the error code
     * @param message   the detail message
     * @param cause     the cause of the exception
     */
    public NotFoundException(String errorCode, String message, Throwable cause) {
        super(errorCode, message, cause);
    }

    /**
     * Creates a generic resource not found exception.
     *
     * @param resourceName the name of the resource type (e.g., "Product", "User")
     * @param id           the ID of the resource that was not found
     * @return a new NotFoundException instance
     */
    public static NotFoundException of(String resourceName, Long id) {
        return new NotFoundException(
            resourceName.toUpperCase() + "_NOT_FOUND",
            String.format("%s with id %d not found", resourceName, id)
        );
    }
}
