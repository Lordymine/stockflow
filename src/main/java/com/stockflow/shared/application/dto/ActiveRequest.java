package com.stockflow.shared.application.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

/**
 * Generic request payload for activating or deactivating a resource.
 */
@Schema(description = "Active status request payload")
public record ActiveRequest(
    @Schema(description = "Desired active status", example = "true", required = true)
    @NotNull(message = "Active status is required")
    Boolean isActive
) {
}
