package com.stockflow.modules.inventory.application.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;

/**
 * Response DTO for branch stock information.
 *
 * <p>Contains current stock data for a product in a specific branch.</p>
 */
@Schema(description = "Branch stock response payload")
public record BranchStockResponse(

    @Schema(description = "Stock entry ID", example = "1")
    Long id,

    @Schema(description = "Tenant ID", example = "1")
    Long tenantId,

    @Schema(description = "Branch ID", example = "1")
    Long branchId,

    @Schema(description = "Product ID", example = "1")
    Long productId,

    @Schema(description = "Current quantity in stock", example = "50")
    Integer quantity,

    @Schema(description = "Last update timestamp", example = "2024-01-01T10:00:00")
    LocalDateTime updatedAt,

    @Schema(description = "Entity version for optimistic locking", example = "0")
    Long version
) {
}
