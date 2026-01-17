package com.stockflow.modules.inventory.application.dto;

import com.stockflow.modules.inventory.domain.model.MovementReason;
import com.stockflow.modules.inventory.domain.model.MovementType;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

/**
 * Request DTO for stock movement operations.
 *
 * <p>Contains the data required to create a stock movement (IN, OUT, ADJUSTMENT, or TRANSFER).</p>
 *
 * <p><b>Validation:</b></p>
 * <ul>
 *   <li>branchId and productId are required</li>
 *   <li>type and reason must be valid enum values</li>
 *   <li>quantity must be positive</li>
 * </ul>
 */
@Schema(description = "Stock movement request payload")
public record StockMovementRequest(

    @Schema(description = "Branch ID where the movement occurs", example = "1", required = true)
    @NotNull(message = "Branch ID is required")
    Long branchId,

    @Schema(description = "Product ID being moved", example = "1", required = true)
    @NotNull(message = "Product ID is required")
    Long productId,

    @Schema(description = "Movement type (IN, OUT, ADJUSTMENT, TRANSFER)", example = "IN", required = true)
    @NotNull(message = "Movement type is required")
    MovementType type,

    @Schema(description = "Reason for movement", example = "PURCHASE", required = true)
    @NotNull(message = "Movement reason is required")
    MovementReason reason,

    @Schema(description = "Quantity moved (must be positive)", example = "10", required = true)
    @NotNull(message = "Quantity is required")
    @Positive(message = "Quantity must be positive")
    Integer quantity,

    @Schema(description = "Optional note explaining the movement", example = "Stock replenishment from supplier XYZ")
    String note
) {
}
