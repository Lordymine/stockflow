package com.stockflow.modules.inventory.application.dto;

import com.stockflow.modules.inventory.domain.model.MovementReason;
import com.stockflow.modules.inventory.domain.model.MovementType;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;

/**
 * Response DTO for stock movement operations.
 *
 * <p>Contains complete stock movement data returned from the API.</p>
 */
@Schema(description = "Stock movement response payload")
public record StockMovementResponse(

    @Schema(description = "Movement ID", example = "1")
    Long id,

    @Schema(description = "Tenant ID", example = "1")
    Long tenantId,

    @Schema(description = "Branch ID where movement occurred", example = "1")
    Long branchId,

    @Schema(description = "Product ID being moved", example = "1")
    Long productId,

    @Schema(description = "Movement type", example = "IN")
    MovementType type,

    @Schema(description = "Reason for movement", example = "PURCHASE")
    MovementReason reason,

    @Schema(description = "Quantity moved", example = "10")
    Integer quantity,

    @Schema(description = "Optional note explaining the movement", example = "Stock replenishment from supplier XYZ")
    String note,

    @Schema(description = "ID of user who created the movement", example = "1")
    Long createdByUserId,

    @Schema(description = "Creation timestamp", example = "2024-01-01T10:00:00")
    LocalDateTime createdAt
) {
}
