package com.stockflow.modules.tenants.application.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;

/**
 * Response DTO for tenant information.
 */
@Schema(description = "Tenant information")
public record TenantResponse(

    @Schema(description = "Tenant ID")
    Long id,

    @Schema(description = "Tenant name")
    String name,

    @Schema(description = "Tenant slug (URL-friendly)")
    String slug,

    @Schema(description = "Is tenant active")
    Boolean isActive,

    @Schema(description = "Creation timestamp")
    LocalDateTime createdAt
) {
}
