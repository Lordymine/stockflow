package com.stockflow.shared.application.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;

import java.util.Map;

/**
 * Standard API response envelope for successful operations.
 *
 * <p>All successful API responses should follow this structure to maintain
 * consistency across the entire application.</p>
 *
 * <p>Example:</p>
 * <pre>
 * {
 *   "success": true,
 *   "data": { ... },
 *   "meta": {
 *     "timestamp": "2024-01-16T10:30:00",
 *     "path": "/api/v1/products"
 *   }
 * }
 * </pre>
 *
 * @param <T> the type of data being returned
 */
@Schema(description = "Standard API response for successful operations")
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ApiResponse<T> {

    @Schema(description = "Indicates if the request was successful", example = "true")
    private final boolean success;

    @Schema(description = "Response data")
    private final T data;

    @Schema(description = "Additional metadata")
    private final Map<String, Object> meta;

    /**
     * Creates a new API response with success flag set to true.
     *
     * @param data the response data
     * @param <T>  the type of data
     * @return a new ApiResponse instance
     */
    public static <T> ApiResponse<T> of(T data) {
        return new ApiResponse<>(true, data, null);
    }

    /**
     * Creates a new API response with metadata.
     *
     * @param data the response data
     * @param meta additional metadata
     * @param <T>  the type of data
     * @return a new ApiResponse instance
     */
    public static <T> ApiResponse<T> of(T data, Map<String, Object> meta) {
        return new ApiResponse<>(true, data, meta);
    }

    /**
     * Creates a new empty API response.
     *
     * @param <T> the type of data
     * @return a new ApiResponse instance with null data
     */
    public static <T> ApiResponse<T> empty() {
        return new ApiResponse<>(true, null, null);
    }

    private ApiResponse(boolean success, T data, Map<String, Object> meta) {
        this.success = success;
        this.data = data;
        this.meta = meta;
    }

    public boolean isSuccess() {
        return success;
    }

    public T getData() {
        return data;
    }

    public Map<String, Object> getMeta() {
        return meta;
    }
}
