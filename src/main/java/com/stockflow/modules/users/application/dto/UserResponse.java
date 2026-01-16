package com.stockflow.modules.users.application.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Response DTO for user information.
 */
@Schema(description = "User information")
public record UserResponse(

    @Schema(description = "User ID")
    Long id,

    @Schema(description = "User name")
    String name,

    @Schema(description = "User email")
    String email,

    @Schema(description = "Is user active")
    Boolean isActive,

    @Schema(description = "User roles")
    List<String> roles,

    @Schema(description = "Accessible branch IDs")
    List<Long> branchIds,

    @Schema(description = "Creation timestamp")
    LocalDateTime createdAt
) {
}
