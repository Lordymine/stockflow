package com.stockflow.modules.auth.application.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

/**
 * Response DTO for successful signup.
 *
 * <p>Contains created tenant, user, and authentication tokens.</p>
 */
@Schema(description = "Signup response with tenant, user, and tokens")
public record SignupResponse(

    @Schema(description = "Created tenant information")
    TenantInfo tenant,

    @Schema(description = "Created admin user information")
    UserInfo user,

    @Schema(description = "Authentication tokens")
    TokenInfo tokens
) {

    @Schema(description = "Tenant information")
    public record TenantInfo(

        @Schema(description = "Tenant ID")
        Long id,

        @Schema(description = "Tenant name")
        String name,

        @Schema(description = "Tenant slug")
        String slug
    ) {
    }

    @Schema(description = "User information")
    public record UserInfo(

        @Schema(description = "User ID")
        Long id,

        @Schema(description = "User name")
        String name,

        @Schema(description = "User email")
        String email,

        @Schema(description = "User roles")
        List<String> roles
    ) {
    }

    @Schema(description = "Token information")
    public record TokenInfo(

        @Schema(description = "JWT access token")
        String accessToken,

        @Schema(description = "Refresh token")
        String refreshToken,

        @Schema(description = "Token type", example = "Bearer")
        String tokenType,

        @Schema(description = "Access token expiration in seconds")
        Long expiresIn
    ) {
    }
}
