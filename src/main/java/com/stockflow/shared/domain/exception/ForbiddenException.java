package com.stockflow.shared.domain.exception;

/**
 * Exception thrown when a user attempts to perform an operation without proper authorization.
 *
 * <p>This exception indicates that the user is authenticated but lacks
 * the necessary permissions to access the requested resource or perform the operation.</p>
 *
 * <p>Common scenarios:</p>
 * <ul>
 *   <li>User attempts to access a branch they are not authorized for</li>
 *   <li>User without ADMIN role attempts to perform admin operations</li>
 *   <li>User attempts to modify resources from another tenant</li>
 *   <li>Insufficient role or scope for the requested operation</li>
 * </ul>
 *
 * <p>Maps to HTTP 403 Forbidden.</p>
 */
public class ForbiddenException extends BaseDomainException {

    /**
     * Constructs a new forbidden exception with the specified error code and message.
     *
     * @param errorCode the error code (e.g., "FORBIDDEN_BRANCH_ACCESS", "INSUFFICIENT_PRIVILEGES")
     * @param message   the detail message explaining why access is denied
     */
    public ForbiddenException(String errorCode, String message) {
        super(errorCode, message);
    }

    /**
     * Constructs a new forbidden exception with the specified error code, message, and cause.
     *
     * @param errorCode the error code
     * @param message   the detail message
     * @param cause     the cause of the exception
     */
    public ForbiddenException(String errorCode, String message, Throwable cause) {
        super(errorCode, message, cause);
    }

    /**
     * Creates a branch access forbidden exception.
     *
     * @param branchId the branch ID the user is trying to access
     * @return a new ForbiddenException instance
     */
    public static ForbiddenException branchAccess(Long branchId) {
        return new ForbiddenException(
            "FORBIDDEN_BRANCH_ACCESS",
            String.format("Access denied to branch %d. User is not authorized for this branch.", branchId)
        );
    }
}
