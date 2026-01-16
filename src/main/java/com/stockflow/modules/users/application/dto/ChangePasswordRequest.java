package com.stockflow.modules.users.application.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Request DTO for changing user password.
 */
@Schema(description = "Change password request")
public record ChangePasswordRequest(

    @Schema(description = "Current password")
    @NotBlank(message = "Current password is required")
    String currentPassword,

    @Schema(description = "New password (min 8 characters)")
    @NotBlank(message = "New password is required")
    @Size(min = 8, message = "Password must be at least 8 characters")
    String newPassword
) {
}
