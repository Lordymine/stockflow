package com.stockflow.modules.auth.application.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Request DTO for bootstrap (initial tenant setup).
 *
 * <p>Used to create the first tenant with admin user.
 * This endpoint is only valid when no tenants exist yet.</p>
 */
@Schema(description = "Bootstrap request for initial tenant setup")
public record BootstrapRequest(

    @Schema(description = "Tenant/company name", example = "Acme Corporation", required = true)
    @NotBlank(message = "Tenant name is required")
    @Size(min = 2, max = 255, message = "Tenant name must be between 2 and 255 characters")
    String tenantName,

    @Schema(description = "Tenant slug (URL-friendly)", example = "acme-corp", required = true)
    @NotBlank(message = "Tenant slug is required")
    @Size(min = 2, max = 100, message = "Tenant slug must be between 2 and 100 characters")
    String tenantSlug,

    @Schema(description = "Admin user name", example = "John Doe", required = true)
    @NotBlank(message = "Admin name is required")
    @Size(min = 2, max = 255, message = "Admin name must be between 2 and 255 characters")
    String adminName,

    @Schema(description = "Admin email", example = "admin@acme.com", required = true)
    @NotBlank(message = "Admin email is required")
    @jakarta.validation.constraints.Email(message = "Email must be valid")
    String adminEmail,

    @Schema(description = "Admin password (min 8 characters)", example = "SecurePassword123!", required = true)
    @NotBlank(message = "Admin password is required")
    @Size(min = 8, message = "Password must be at least 8 characters")
    String adminPassword
) {
}
