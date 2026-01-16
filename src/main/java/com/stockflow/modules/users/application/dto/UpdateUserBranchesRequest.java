package com.stockflow.modules.users.application.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;

import java.util.List;

/**
 * Request DTO for updating user branch access.
 */
@Schema(description = "Update user branches request")
public record UpdateUserBranchesRequest(

    @Schema(description = "List of branch IDs the user can access", example = "[1, 2, 3]")
    @NotEmpty(message = "At least one branch is required")
    List<Long> branchIds
) {
}
