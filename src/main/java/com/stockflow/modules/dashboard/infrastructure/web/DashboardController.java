package com.stockflow.modules.dashboard.infrastructure.web;

import com.stockflow.modules.dashboard.application.dto.DashboardOverviewResponse;
import com.stockflow.modules.dashboard.application.service.DashboardService;
import com.stockflow.shared.application.dto.ApiResponse;
import com.stockflow.shared.domain.exception.ForbiddenException;
import com.stockflow.shared.infrastructure.security.CustomUserDetails;
import com.stockflow.shared.infrastructure.security.TenantContext;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * REST controller for dashboard operations.
 */
@RestController
@RequestMapping("/api/v1/dashboard")
@Tag(name = "Dashboard", description = "Dashboard metrics and analytics endpoints")
@SecurityRequirement(name = "bearerAuth")
public class DashboardController {

    private static final Logger log = LoggerFactory.getLogger(DashboardController.class);

    private final DashboardService dashboardService;

    public DashboardController(DashboardService dashboardService) {
        this.dashboardService = dashboardService;
    }

    /**
     * Gets dashboard overview for the current tenant.
     *
     * @param branchId optional branch ID to filter metrics
     * @return dashboard overview response
     */
    @GetMapping("/overview")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'STAFF')")
    @Operation(
        summary = "Get dashboard overview",
        description = "Retrieves dashboard metrics for the current tenant. Optionally filters by branch."
    )
    public ResponseEntity<ApiResponse<DashboardOverviewResponse>> getOverview(
            @Parameter(description = "Optional branch ID to filter metrics")
            @RequestParam(required = false) Long branchId) {

        Long tenantId = TenantContext.getTenantId();

        if (branchId != null) {
            validateBranchAccess(branchId);
            log.debug("GET /api/v1/dashboard/overview - tenant: {}, branch: {}", tenantId, branchId);
            DashboardOverviewResponse response = dashboardService.getOverviewByBranch(tenantId, branchId);
            return ResponseEntity.ok(ApiResponse.of(response));
        }

        log.debug("GET /api/v1/dashboard/overview - tenant: {}", tenantId);
        DashboardOverviewResponse response = dashboardService.getOverview(tenantId);
        return ResponseEntity.ok(ApiResponse.of(response));
    }

    private void validateBranchAccess(Long branchId) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !(authentication.getPrincipal() instanceof CustomUserDetails userDetails)) {
            throw new ForbiddenException("FORBIDDEN_BRANCH_ACCESS",
                "Branch access is required for this operation");
        }

        if (!userDetails.hasAccessToBranch(branchId)) {
            throw new ForbiddenException("FORBIDDEN_BRANCH_ACCESS",
                String.format("You do not have access to branch %d", branchId));
        }
    }
}
