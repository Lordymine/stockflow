package com.stockflow.modules.dashboard.application.service;

import com.stockflow.modules.dashboard.application.dto.DashboardOverviewResponse;
import com.stockflow.modules.branches.domain.model.Branch;
import com.stockflow.modules.branches.domain.repository.BranchRepository;
import com.stockflow.modules.dashboard.domain.model.DashboardMetrics;
import com.stockflow.modules.dashboard.domain.model.TopProductMovement;
import com.stockflow.shared.domain.exception.NotFoundException;
import com.stockflow.shared.domain.exception.ValidationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Implementation of DashboardService with Redis caching.
 *
 * <p>This service provides dashboard metrics with aggressive caching
 * to improve performance for frequently accessed data (ADR-0005).</p>
 *
 * <p><strong>Cache Configuration:</strong></p>
 * <ul>
 *   <li>Dashboard overview: 5 minutes TTL (300 seconds)</li>
 *   <li>Top products: 10 minutes TTL (600 seconds)</li>
 *   <li>Cache keys include tenantId and branchId for proper isolation</li>
 * </ul>
 *
 * <p><strong>Cache Invalidation:</strong></p>
 * <ul>
 *   <li>Cache is evicted when stock movements occur</li>
 *   <li>InventoryService triggers @CacheEvict on createMovement and transferStock</li>
 *   <li>Ensures dashboard shows near real-time data after operations</li>
 * </ul>
 *
 * @see DashboardRepository
 * @see CacheConfig
 */
@Service
@Transactional(readOnly = true)
public class DashboardServiceImpl implements DashboardService {

    private static final Logger log = LoggerFactory.getLogger(DashboardServiceImpl.class);

    private final DashboardCacheService dashboardCacheService;
    private final BranchRepository branchRepository;

    public DashboardServiceImpl(DashboardCacheService dashboardCacheService,
                                BranchRepository branchRepository) {
        this.dashboardCacheService = dashboardCacheService;
        this.branchRepository = branchRepository;
    }

    @Override
    public DashboardOverviewResponse getOverview(Long tenantId) {
        log.info("Fetching dashboard overview for tenant: {}", tenantId);

        // Get dashboard metrics
        DashboardMetrics metrics = dashboardCacheService.getMetrics(tenantId);

        // Get top products by movement
        List<TopProductMovement> topProducts = dashboardCacheService.getTopProducts(tenantId);

        log.info("Dashboard overview for tenant {}: {} active products, {} low stock items, {} total movements, {} recent movements",
                tenantId, metrics.getTotalActiveProducts(), metrics.getLowStockItems(),
                metrics.getTotalMovements(), metrics.getRecentMovements());

        return DashboardOverviewResponse.fromDomain(metrics, topProducts);
    }

    @Override
    public DashboardOverviewResponse getOverviewByBranch(Long tenantId, Long branchId) {
        log.info("Fetching dashboard overview for tenant: {}, branch: {}", tenantId, branchId);

        validateBranchExists(branchId, tenantId);

        // Get dashboard metrics for the branch
        DashboardMetrics metrics = dashboardCacheService.getMetricsByBranch(tenantId, branchId);

        // Get top products by movement for the branch
        List<TopProductMovement> topProducts = dashboardCacheService.getTopProductsByBranch(tenantId, branchId);

        log.info("Dashboard overview for tenant:{}, branch {}: {} active products, {} low stock items, {} total movements, {} recent movements",
                tenantId, branchId, metrics.getTotalActiveProducts(), metrics.getLowStockItems(),
                metrics.getTotalMovements(), metrics.getRecentMovements());

        return DashboardOverviewResponse.fromDomain(metrics, topProducts);
    }

    private void validateBranchExists(Long branchId, Long tenantId) {
        Branch branch = branchRepository.findByIdAndTenantIdIncludingInactive(branchId, tenantId)
            .orElseThrow(() -> new NotFoundException("BRANCH_NOT_FOUND",
                "Branch not found with ID: " + branchId));

        if (!branch.isActive()) {
            throw new ValidationException("BRANCH_ACTIVE_REQUIRED",
                "Branch must be active for this operation");
        }
    }
}
