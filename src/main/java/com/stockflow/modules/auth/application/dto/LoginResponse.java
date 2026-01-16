package com.stockflow.modules.auth.application.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

/**
 * Response DTO for successful login.
 *
 * <p>Contains access token, refresh token, and user information.</p>
 */
@Schema(description = "Login response with tokens and user data")
public record LoginResponse(

    @Schema(description = "JWT access token (15 minutes expiration)")
    String accessToken,

    @Schema(description = "Refresh token (7 days expiration)")
    String refreshToken,

    @Schema(description = "Token type", example = "Bearer")
    String tokenType,

    @Schema(description = "Access token expiration in seconds", example = "900")
    Long expiresIn,

    @Schema(description = "Authenticated user information")
    UserInfo user
) {

    @Schema(description = "User information")
    public record UserInfo(

        @Schema(description = "User ID")
        Long id,

        @Schema(description = "User name")
        String name,

        @Schema(description = "User email")
        String email,

        @Schema(description = "User roles")
        List<String> roles,

        @Schema(description = "Accessible branch IDs")
        List<Long> branchIds
    ) {
    }
}
