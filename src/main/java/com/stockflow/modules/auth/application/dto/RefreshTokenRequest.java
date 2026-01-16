package com.stockflow.modules.auth.application.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

/**
 * Request DTO for token refresh.
 *
 * <p>Contains the refresh token to obtain a new access token.</p>
 */
@Schema(description = "Refresh token request")
public record RefreshTokenRequest(

    @Schema(description = "Refresh token", required = true)
    @NotBlank(message = "Refresh token is required")
    String refreshToken
) {
}
