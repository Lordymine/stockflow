package com.stockflow.modules.dashboard.application.service;

import com.stockflow.modules.dashboard.application.dto.DashboardOverviewResponse;

/**
 * Service interface for dashboard operations.
 *
 * <p>Provides business logic for retrieving dashboard metrics and analytics.
 * All operations are cached with Redis to improve performance (ADR-0005).</p>
 *
 * <p><strong>Cache Strategy:</strong></p>
 * <ul>
 *   <li>Overview metrics: cached for 5 minutes</li>
 *   <li>Cache key includes tenantId and optionally branchId</li>
 *   <li>Cache is invalidated on any stock movement or transfer</li>
 * </ul>
 *
 * <p><strong>Multi-tenancy:</strong></p>
 * <ul>
 *   <li>All operations are scoped to the current tenant</li>
 *   <li>Tenant isolation is enforced at service and repository layers</li>
 * </ul>
 */
public interface DashboardService {

    /**
     * Gets dashboard overview for the current tenant.
     *
     * <p>This method aggregates multiple metrics including:</p>
     * <ul>
     *   <li>Total active products</li>
     *   <li>Products with stock below minimum</li>
     *   <li>Total and recent stock movements</li>
     *   <li>Top products by movement count</li>
     * </ul>
     *
     * <p><strong>Cache:</strong> Results are cached for 5 minutes with key including tenantId.</p>
     *
     * @param tenantId the tenant ID
     * @return dashboard overview response
     */
    DashboardOverviewResponse getOverview(Long tenantId);

    /**
     * Gets dashboard overview for a specific branch.
     *
     * <p>This method aggregates metrics scoped to a specific branch,
     * useful for branch managers and operational dashboards.</p>
     *
     * <p><strong>Cache:</strong> Results are cached for 5 minutes with key including
     * tenantId and branchId.</p>
     *
     * @param tenantId the tenant ID
     * @param branchId the branch ID
     * @return dashboard overview response for the branch
     */
    DashboardOverviewResponse getOverviewByBranch(Long tenantId, Long branchId);
}
