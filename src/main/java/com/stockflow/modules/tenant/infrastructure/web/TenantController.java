package com.stockflow.modules.tenant.infrastructure.web;

import com.stockflow.modules.tenant.application.dto.TenantResponse;
import com.stockflow.modules.tenant.application.service.TenantService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * REST controller for tenant operations.
 */
@RestController
@RequestMapping("/api/v1/tenants")
@Tag(name = "Tenants", description = "Tenant management endpoints")
@SecurityRequirement(name = "bearerAuth")
public class TenantController {

    private final TenantService tenantService;

    public TenantController(TenantService tenantService) {
        this.tenantService = tenantService;
    }

    /**
     * Gets the current tenant information.
     *
     * @return the tenant response
     */
    @GetMapping("/me")
    @Operation(summary = "Get current tenant", description = "Retrieve information about the current tenant")
    public ResponseEntity<TenantResponse> getCurrentTenant() {
        TenantResponse response = tenantService.getCurrentTenant();
        return ResponseEntity.ok(response);
    }
}
