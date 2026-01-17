package com.stockflow.modules.catalog.application.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;

/**
 * Response DTO for category operations.
 *
 * <p>Contains category data returned from the API.</p>
 */
@Schema(description = "Category response payload")
public record CategoryResponse(

    @Schema(description = "Category ID", example = "1")
    Long id,

    @Schema(description = "Tenant ID", example = "1")
    Long tenantId,

    @Schema(description = "Category name", example = "Electronics")
    String name,

    @Schema(description = "Creation timestamp", example = "2024-01-01T10:00:00")
    LocalDateTime createdAt,

    @Schema(description = "Last update timestamp", example = "2024-01-01T10:00:00")
    LocalDateTime updatedAt,

    @Schema(description = "Entity version for optimistic locking", example = "0")
    Long version,

    @Schema(description = "Indicates if the category is active", example = "true")
    Boolean isActive
) {
}
