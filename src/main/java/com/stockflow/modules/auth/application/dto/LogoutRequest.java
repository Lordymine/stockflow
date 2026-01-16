package com.stockflow.modules.auth.application.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

/**
 * Request DTO for user logout.
 *
 * <p>Contains the refresh token to be revoked.</p>
 */
@Schema(description = "Logout request")
public record LogoutRequest(

    @Schema(description = "Refresh token to revoke", required = true)
    @NotBlank(message = "Refresh token is required")
    String refreshToken
) {
}
