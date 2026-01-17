package com.stockflow.modules.inventory.application.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

/**
 * Request DTO for transferring stock between branches.
 *
 * <p>Contains the data required to transfer a product from one branch to another.</p>
 *
 * <p><b>Validation:</b></p>
 * <ul>
 *   <li>Source and destination branches must be different</li>
 *   <li>Product ID is required</li>
 *   <li>Quantity must be positive</li>
 *   <li>Optional note can explain the transfer reason</li>
 * </ul>
 */
@Schema(description = "Transfer stock request payload")
public record TransferStockRequest(

    @Schema(description = "Source branch ID", example = "1", required = true)
    @NotNull(message = "Source branch ID is required")
    Long sourceBranchId,

    @Schema(description = "Destination branch ID", example = "2", required = true)
    @NotNull(message = "Destination branch ID is required")
    Long destinationBranchId,

    @Schema(description = "Product ID to transfer", example = "1", required = true)
    @NotNull(message = "Product ID is required")
    Long productId,

    @Schema(description = "Quantity to transfer (must be positive)", example = "10", required = true)
    @NotNull(message = "Quantity is required")
    @Positive(message = "Quantity must be positive")
    Integer quantity,

    @Schema(description = "Optional note explaining the transfer", example = "Transfer to fulfill customer order")
    String note
) {
}
