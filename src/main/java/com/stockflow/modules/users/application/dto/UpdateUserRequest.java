package com.stockflow.modules.users.application.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Size;

/**
 * Request DTO for updating user information.
 */
@Schema(description = "Update user request")
public record UpdateUserRequest(

    @Schema(description = "User name")
    @Size(min = 2, max = 255, message = "Name must be between 2 and 255 characters")
    String name
) {
}
