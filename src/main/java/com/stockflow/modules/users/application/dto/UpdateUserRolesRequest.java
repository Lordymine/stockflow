package com.stockflow.modules.users.application.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;

import java.util.List;

/**
 * Request DTO for updating user roles.
 */
@Schema(description = "Update user roles request")
public record UpdateUserRolesRequest(

    @Schema(description = "List of role names (ADMIN, MANAGER, STAFF)", example = "[\"ADMIN\", \"MANAGER\"]")
    @NotEmpty(message = "At least one role is required")
    List<String> roles
) {
}
