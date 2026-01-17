package com.stockflow.modules.branches.application.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;

/**
 * Response DTO for branch operations.
 */
@Schema(description = "Branch response payload")
public record BranchResponse(

    @Schema(description = "Branch ID", example = "1")
    Long id,

    @Schema(description = "Tenant ID", example = "1")
    Long tenantId,

    @Schema(description = "Branch name", example = "Main Branch")
    String name,

    @Schema(description = "Branch code", example = "MAIN")
    String code,

    @Schema(description = "Branch address", example = "123 Main St")
    String address,

    @Schema(description = "Contact phone number", example = "+1-555-0100")
    String phone,

    @Schema(description = "Branch manager name", example = "Alex Smith")
    String managerName,

    @Schema(description = "Creation timestamp", example = "2024-01-01T10:00:00")
    LocalDateTime createdAt,

    @Schema(description = "Last update timestamp", example = "2024-01-01T10:00:00")
    LocalDateTime updatedAt,

    @Schema(description = "Entity version for optimistic locking", example = "0")
    Long version,

    @Schema(description = "Indicates if the branch is active", example = "true")
    Boolean isActive
) {
}
