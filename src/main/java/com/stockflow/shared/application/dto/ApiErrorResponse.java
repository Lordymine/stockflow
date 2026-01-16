package com.stockflow.shared.application.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import org.springframework.validation.FieldError;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Standard API response envelope for error responses.
 *
 * <p>All error responses should follow this structure to maintain
 * consistency across the entire application.</p>
 *
 * <p>Example:</p>
 * <pre>
 * {
 *   "success": false,
 *   "error": {
 *     "code": "VALIDATION_ERROR",
 *     "message": "Validation failed",
 *     "details": [
 *       "name: must not be blank",
 *       "email: must be a valid email"
 *     ],
 *     "timestamp": "2024-01-16T10:30:00"
 *   }
 * }
 * </pre>
 */
@Schema(description = "Standard API response for error cases")
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ApiErrorResponse {

    @Schema(description = "Indicates if the request was successful", example = "false")
    private final boolean success;

    @Schema(description = "Error details")
    private final ErrorDetail error;

    private ApiErrorResponse(ErrorDetail error) {
        this.success = false;
        this.error = error;
    }

    /**
     * Creates an error response from error code and message.
     *
     * @param code    the error code
     * @param message the error message
     * @return a new ApiErrorResponse instance
     */
    public static ApiErrorResponse of(String code, String message) {
        ErrorDetail error = new ErrorDetail(code, message, null, LocalDateTime.now());
        return new ApiErrorResponse(error);
    }

    /**
     * Creates an error response with details.
     *
     * @param code    the error code
     * @param message the error message
     * @param details list of error detail messages
     * @return a new ApiErrorResponse instance
     */
    public static ApiErrorResponse of(String code, String message, List<String> details) {
        ErrorDetail error = new ErrorDetail(code, message, details, LocalDateTime.now());
        return new ApiErrorResponse(error);
    }

    /**
     * Creates an error response from field errors (Bean Validation).
     *
     * @param fieldErrors list of FieldError objects
     * @return a new ApiErrorResponse instance with validation details
     */
    public static ApiErrorResponse fromFieldErrors(List<FieldError> fieldErrors) {
        List<String> details = fieldErrors.stream()
            .map(error -> String.format("%s: %s", error.getField(), error.getDefaultMessage()))
            .collect(Collectors.toList());

        return of("VALIDATION_ERROR", "Validation failed", details);
    }

    /**
     * Creates an error response from a throwable.
     *
     * @param code       the error code
     * @param message    the error message
     * @param throwable  the cause
     * @return a new ApiErrorResponse instance
     */
    public static ApiErrorResponse of(String code, String message, Throwable throwable) {
        List<String> details = throwable != null
            ? List.of(throwable.getMessage())
            : null;
        return of(code, message, details);
    }

    public boolean isSuccess() {
        return success;
    }

    public ErrorDetail getError() {
        return error;
    }

    @Schema(description = "Error details")
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public record ErrorDetail(
        @Schema(description = "Error code", example = "VALIDATION_ERROR")
        String code,

        @Schema(description = "Human-readable error message", example = "Validation failed")
        String message,

        @Schema(description = "Detailed error messages")
        List<String> details,

        @Schema(description = "Timestamp when the error occurred")
        LocalDateTime timestamp
    ) {}
}
