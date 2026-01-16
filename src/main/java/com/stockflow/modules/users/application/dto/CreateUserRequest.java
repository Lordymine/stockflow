package com.stockflow.modules.users.application.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.util.List;

/**
 * Request DTO for creating a new user.
 */
@Schema(description = "Create user request")
public record CreateUserRequest(

    @Schema(description = "User name", example = "John Doe")
    @NotBlank(message = "Name is required")
    @Size(min = 2, max = 255, message = "Name must be between 2 and 255 characters")
    String name,

    @Schema(description = "User email", example = "john@company.com")
    @NotBlank(message = "Email is required")
    @Email(message = "Email must be valid")
    String email,

    @Schema(description = "User password (min 8 characters)", example = "SecurePassword123!")
    @NotBlank(message = "Password is required")
    @Size(min = 8, message = "Password must be at least 8 characters")
    String password,

    @Schema(description = "User roles (ADMIN, MANAGER, STAFF)", example = "[\"STAFF\"]")
    List<String> roles,

    @Schema(description = "Branch IDs the user can access", example = "[1, 2]")
    List<Long> branchIds
) {
}
