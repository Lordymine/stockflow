package com.stockflow.modules.dashboard.domain.repository;

import com.stockflow.modules.dashboard.domain.model.DashboardMetrics;
import com.stockflow.modules.dashboard.domain.model.TopProductMovement;

import java.util.List;

/**
 * Repository interface for dashboard data aggregation.
 *
 * <p>This repository provides methods to query aggregated data for dashboard metrics.
 * All queries are scoped to the current tenant.</p>
 *
 * <p><strong>Cache Considerations:</strong></p>
 * <ul>
 *   <li>All methods are computationally expensive (aggregations)</li>
 *   <li>Results should be cached with appropriate TTL</li>
 *   <li>Cache must be invalidated on stock movements</li>
 * </ul>
 */
public interface DashboardRepository {

    /**
     * Gets dashboard metrics for the current tenant.
     *
     * <p>This method performs several aggregations:</p>
     * <ul>
     *   <li>Counts total active products</li>
     *   <li>Counts products with stock below minimum</li>
     *   <li>Counts total and recent movements (7 days)</li>
     * </ul>
     *
     * @param tenantId the tenant ID
     * @return dashboard metrics
     */
    DashboardMetrics getMetrics(Long tenantId);

    /**
     * Gets dashboard metrics for a specific branch.
     *
     * <p>This method performs several aggregations scoped to a branch:</p>
     * <ul>
     *   <li>Counts products with stock below minimum in this branch</li>
     *   <li>Counts total and recent movements for this branch</li>
     *   <li>Note: totalActiveProducts is tenant-wide, not branch-specific</li>
     * </ul>
     *
     * @param tenantId the tenant ID
     * @param branchId the branch ID
     * @return dashboard metrics for the branch
     */
    DashboardMetrics getMetricsByBranch(Long tenantId, Long branchId);

    /**
     * Gets the top products by movement count for the current tenant.
     *
     * <p>Returns products sorted by total movement count (descending).
     * Useful for identifying the most frequently moved products.</p>
     *
     * @param tenantId the tenant ID
     * @param limit    maximum number of products to return
     * @return list of top product movements
     */
    List<TopProductMovement> getTopProductsByMovement(Long tenantId, int limit);

    /**
     * Gets the top products by movement count for a specific branch.
     *
     * <p>Returns products sorted by total movement count in the branch (descending).
     * Useful for identifying the most frequently moved products in a specific branch.</p>
     *
     * @param tenantId the tenant ID
     * @param branchId the branch ID
     * @param limit    maximum number of products to return
     * @return list of top product movements for the branch
     */
    List<TopProductMovement> getTopProductsByMovementForBranch(Long tenantId, Long branchId, int limit);
}
