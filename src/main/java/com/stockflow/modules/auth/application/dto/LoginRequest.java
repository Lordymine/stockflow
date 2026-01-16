package com.stockflow.modules.auth.application.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

/**
 * Request DTO for user login.
 *
 * <p>Contains the user's credentials for authentication.</p>
 */
@Schema(description = "User login request")
public record LoginRequest(

    @Schema(description = "User email address", example = "admin@company.com", required = true)
    @NotBlank(message = "Email is required")
    @Email(message = "Email must be valid")
    String email,

    @Schema(description = "User password", example = "SecurePassword123!", required = true)
    @NotBlank(message = "Password is required")
    String password
) {
}
