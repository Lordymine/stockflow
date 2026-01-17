package com.stockflow.modules.branches.application.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Request DTO for branch operations.
 */
@Schema(description = "Branch request payload")
public record BranchRequest(

    @Schema(description = "Branch name", example = "Main Branch", required = true)
    @NotBlank(message = "Branch name is required")
    @Size(min = 2, max = 255, message = "Branch name must be between 2 and 255 characters")
    String name,

    @Schema(description = "Branch code (unique within tenant)", example = "MAIN", required = true)
    @NotBlank(message = "Branch code is required")
    @Size(min = 2, max = 50, message = "Branch code must be between 2 and 50 characters")
    String code,

    @Schema(description = "Branch address", example = "123 Main St")
    @Size(max = 500, message = "Branch address must be at most 500 characters")
    String address,

    @Schema(description = "Contact phone number", example = "+1-555-0100")
    @Size(max = 20, message = "Phone number must be at most 20 characters")
    String phone,

    @Schema(description = "Branch manager name", example = "Alex Smith")
    @Size(max = 100, message = "Manager name must be at most 100 characters")
    String managerName
) {
}
