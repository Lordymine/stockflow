package com.stockflow.modules.dashboard.application.service;

import com.stockflow.modules.dashboard.domain.model.DashboardMetrics;
import com.stockflow.modules.dashboard.domain.model.TopProductMovement;
import com.stockflow.modules.dashboard.domain.repository.DashboardRepository;
import com.stockflow.shared.infrastructure.cache.CacheConfig;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Cache facade for dashboard queries with per-segment TTLs.
 */
@Service
@Transactional(readOnly = true)
public class DashboardCacheService {

    private static final int TOP_PRODUCTS_LIMIT = 10;

    private final DashboardRepository dashboardRepository;

    public DashboardCacheService(DashboardRepository dashboardRepository) {
        this.dashboardRepository = dashboardRepository;
    }

    @Cacheable(value = CacheConfig.DASHBOARD_OVERVIEW, key = "#tenantId")
    public DashboardMetrics getMetrics(Long tenantId) {
        return dashboardRepository.getMetrics(tenantId);
    }

    @Cacheable(value = CacheConfig.DASHBOARD_BRANCH, key = "#tenantId + ':' + #branchId")
    public DashboardMetrics getMetricsByBranch(Long tenantId, Long branchId) {
        return dashboardRepository.getMetricsByBranch(tenantId, branchId);
    }

    @Cacheable(value = CacheConfig.TOP_PRODUCTS, key = "#tenantId")
    public List<TopProductMovement> getTopProducts(Long tenantId) {
        return dashboardRepository.getTopProductsByMovement(tenantId, TOP_PRODUCTS_LIMIT);
    }

    @Cacheable(value = CacheConfig.TOP_PRODUCTS, key = "#tenantId + ':' + #branchId")
    public List<TopProductMovement> getTopProductsByBranch(Long tenantId, Long branchId) {
        return dashboardRepository.getTopProductsByMovementForBranch(tenantId, branchId, TOP_PRODUCTS_LIMIT);
    }
}
