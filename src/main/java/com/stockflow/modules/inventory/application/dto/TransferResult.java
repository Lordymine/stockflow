package com.stockflow.modules.inventory.application.dto;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * Response DTO for stock transfer operations.
 */
@Schema(description = "Transfer result payload")
public record TransferResult(
    @Schema(description = "Movement ID for source branch", example = "100")
    Long sourceMovementId,
    @Schema(description = "Movement ID for destination branch", example = "101")
    Long destinationMovementId
) {
}
