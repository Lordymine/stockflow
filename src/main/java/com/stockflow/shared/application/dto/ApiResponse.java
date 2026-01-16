package com.stockflow.shared.application.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.time.Instant;
import java.util.Map;

/**
 * Resposta padrão da API usando Java 21 Records.
 *
 * Records fornecem imutabilidade garantida sem boilerplate.
 * O compact constructor define o valor padrão para timestamp.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public record ApiResponse<T>(
    Boolean success,
    T data,
    ApiError error,
    Map<String, Object> meta,
    Instant timestamp
) {
    /**
     * Compact constructor - define valor padrão para timestamp.
     */
    public ApiResponse {
        if (timestamp == null) {
            timestamp = Instant.now();
        }
    }

    // ========== Factory Methods para Sucesso ==========

    public static <T> ApiResponse<T> ok(T data) {
        return new ApiResponse<>(true, data, null, null, null);
    }

    public static <T> ApiResponse<T> ok(T data, Map<String, Object> meta) {
        return new ApiResponse<>(true, data, null, meta, null);
    }

    public static <T> ResponseEntity<ApiResponse<T>> okResponse(T data) {
        return ResponseEntity.ok(ok(data));
    }

    public static <T> ResponseEntity<ApiResponse<T>> okResponse(T data, Map<String, Object> meta) {
        return ResponseEntity.ok(ok(data, meta));
    }

    // ========== Factory Methods para Erro ==========

    public static <T> ApiResponse<T> error(String code, String message) {
        return new ApiResponse<>(false, null, new ApiError(code, message, null), null, null);
    }

    public static <T> ApiResponse<T> error(String code, String message, Object details) {
        return new ApiResponse<>(false, null, new ApiError(code, message, details), null, null);
    }

    public static <T> ResponseEntity<ApiResponse<T>> errorResponse(
        String code,
        String message,
        HttpStatus status
    ) {
        return ResponseEntity
            .status(status)
            .body(error(code, message));
    }

    public static <T> ResponseEntity<ApiResponse<T>> errorResponse(
        String code,
        String message,
        Object details,
        HttpStatus status
    ) {
        return ResponseEntity
            .status(status)
            .body(error(code, message, details));
    }

    // ========== Builder para compatibilidade ==========

    @SuppressWarnings("unchecked")
    public static <T> ApiResponseBuilder<T> builder() {
        return new ApiResponseBuilder<>();
    }

    public static class ApiResponseBuilder<T> {
        private Boolean success;
        private T data;
        private ApiError error;
        private Map<String, Object> meta;
        private Instant timestamp;

        public ApiResponseBuilder<T> success(Boolean success) {
            this.success = success;
            return this;
        }

        public ApiResponseBuilder<T> data(T data) {
            this.data = data;
            return this;
        }

        public ApiResponseBuilder<T> error(ApiError error) {
            this.error = error;
            return this;
        }

        public ApiResponseBuilder<T> meta(Map<String, Object> meta) {
            this.meta = meta;
            return this;
        }

        public ApiResponseBuilder<T> timestamp(Instant timestamp) {
            this.timestamp = timestamp;
            return this;
        }

        public ApiResponse<T> build() {
            return new ApiResponse<>(success, data, error, meta, timestamp);
        }
    }

    /**
     * Record aninhado para representar erros da API.
     * Records aninhados são perfeitos para DTOs imutáveis.
     */
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public record ApiError(
        String code,
        String message,
        Object details
    ) {
        // Builder para compatibilidade
        public static ApiErrorBuilder builder() {
            return new ApiErrorBuilder();
        }

        public static class ApiErrorBuilder {
            private String code;
            private String message;
            private Object details;

            public ApiErrorBuilder code(String code) {
                this.code = code;
                return this;
            }

            public ApiErrorBuilder message(String message) {
                this.message = message;
                return this;
            }

            public ApiErrorBuilder details(Object details) {
                this.details = details;
                return this;
            }

            public ApiError build() {
                return new ApiError(code, message, details);
            }
        }
    }
}
