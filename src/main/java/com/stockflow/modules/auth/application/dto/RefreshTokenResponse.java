package com.stockflow.modules.auth.application.dto;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * Response DTO for successful token refresh.
 *
 * <p>Contains new access and refresh tokens.</p>
 */
@Schema(description = "Token refresh response")
public record RefreshTokenResponse(

    @Schema(description = "New JWT access token (15 minutes expiration)")
    String accessToken,

    @Schema(description = "New refresh token (7 days expiration)")
    String refreshToken,

    @Schema(description = "Token type", example = "Bearer")
    String tokenType,

    @Schema(description = "Access token expiration in seconds", example = "900")
    Long expiresIn
) {
}
