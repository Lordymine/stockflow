package com.stockflow.modules.dashboard.infrastructure.persistence;

import com.stockflow.modules.catalog.domain.model.Product;
import com.stockflow.modules.dashboard.domain.model.DashboardMetrics;
import com.stockflow.modules.dashboard.domain.model.TopProductMovement;
import com.stockflow.modules.dashboard.domain.repository.DashboardRepository;
import com.stockflow.modules.inventory.domain.model.BranchProductStock;
import com.stockflow.modules.inventory.domain.model.StockMovement;
import jakarta.persistence.EntityManager;
import jakarta.persistence.Tuple;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

/**
 * JPA implementation of DashboardRepository.
 *
 * <p>This repository performs complex aggregations across multiple tables
 * to generate dashboard metrics. All queries are optimized with proper indexes.</p>
 *
 * <p><strong>Performance Considerations:</strong></p>
 * <ul>
 *   <li>Uses native SQL for optimal performance</li>
 *   <li>Leverages indexes defined in ADR-0001</li>
 *   <li>Results should be cached to avoid repeated expensive queries</li>
 * </ul>
 */
@Repository
public class DashboardRepositoryImpl implements DashboardRepository {

    private static final Logger log = LoggerFactory.getLogger(DashboardRepositoryImpl.class);

    private final EntityManager entityManager;
    private final JdbcTemplate jdbcTemplate;

    public DashboardRepositoryImpl(EntityManager entityManager, JdbcTemplate jdbcTemplate) {
        this.entityManager = entityManager;
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public DashboardMetrics getMetrics(Long tenantId) {
        log.debug("Fetching dashboard metrics for tenant: {}", tenantId);

        // Query total active products
        Integer totalActiveProducts = entityManager.createQuery(
                        "SELECT COUNT(p) FROM Product p WHERE p.tenantId = :tenantId AND p.isActive = true",
                        Long.class
                )
                .setParameter("tenantId", tenantId)
                .getSingleResult()
                .intValue();

        // Query low stock items (stock below minimum)
        Integer lowStockItems = ((Number) entityManager.createNativeQuery(
                        """
                        SELECT COUNT(DISTINCT s.product_id)
                        FROM branch_product_stock s
                        INNER JOIN products p ON s.product_id = p.id
                        WHERE s.tenant_id = :tenantId
                          AND s.quantity <= p.min_stock
                          AND p.is_active = true
                        """
                )
                .setParameter("tenantId", tenantId)
                .getSingleResult()).intValue();

        // Query total movements
        Integer totalMovements = ((Number) entityManager.createNativeQuery(
                        """
                        SELECT COUNT(*)
                        FROM stock_movements
                        WHERE tenant_id = :tenantId
                        """
                )
                .setParameter("tenantId", tenantId)
                .getSingleResult()).intValue();

        // Query recent movements (last 7 days)
        LocalDateTime sevenDaysAgo = LocalDateTime.now().minusDays(7);
        Integer recentMovements = ((Number) entityManager.createNativeQuery(
                        """
                        SELECT COUNT(*)
                        FROM stock_movements
                        WHERE tenant_id = :tenantId
                          AND created_at >= :since
                        """
                )
                .setParameter("tenantId", tenantId)
                .setParameter("since", sevenDaysAgo)
                .getSingleResult()).intValue();

        log.debug("Dashboard metrics for tenant {}: activeProducts={}, lowStock={}, totalMovements={}, recentMovements={}",
                tenantId, totalActiveProducts, lowStockItems, totalMovements, recentMovements);

        return new DashboardMetrics(totalActiveProducts, lowStockItems, totalMovements, recentMovements);
    }

    @Override
    public DashboardMetrics getMetricsByBranch(Long tenantId, Long branchId) {
        log.debug("Fetching dashboard metrics for tenant: {}, branch: {}", tenantId, branchId);

        // Query total active products (tenant-wide)
        Integer totalActiveProducts = entityManager.createQuery(
                        "SELECT COUNT(p) FROM Product p WHERE p.tenantId = :tenantId AND p.isActive = true",
                        Long.class
                )
                .setParameter("tenantId", tenantId)
                .getSingleResult()
                .intValue();

        // Query low stock items for this branch
        Integer lowStockItems = ((Number) entityManager.createNativeQuery(
                        """
                        SELECT COUNT(DISTINCT s.product_id)
                        FROM branch_product_stock s
                        INNER JOIN products p ON s.product_id = p.id
                        WHERE s.tenant_id = :tenantId
                          AND s.branch_id = :branchId
                          AND s.quantity <= p.min_stock
                          AND p.is_active = true
                        """
                )
                .setParameter("tenantId", tenantId)
                .setParameter("branchId", branchId)
                .getSingleResult()).intValue();

        // Query total movements for this branch
        Integer totalMovements = ((Number) entityManager.createNativeQuery(
                        """
                        SELECT COUNT(*)
                        FROM stock_movements
                        WHERE tenant_id = :tenantId
                          AND branch_id = :branchId
                        """
                )
                .setParameter("tenantId", tenantId)
                .setParameter("branchId", branchId)
                .getSingleResult()).intValue();

        // Query recent movements for this branch (last 7 days)
        LocalDateTime sevenDaysAgo = LocalDateTime.now().minusDays(7);
        Integer recentMovements = ((Number) entityManager.createNativeQuery(
                        """
                        SELECT COUNT(*)
                        FROM stock_movements
                        WHERE tenant_id = :tenantId
                          AND branch_id = :branchId
                          AND created_at >= :since
                        """
                )
                .setParameter("tenantId", tenantId)
                .setParameter("branchId", branchId)
                .setParameter("since", sevenDaysAgo)
                .getSingleResult()).intValue();

        log.debug("Dashboard metrics for tenant:{}, branch {}: activeProducts={}, lowStock={}, totalMovements={}, recentMovements={}",
                tenantId, branchId, totalActiveProducts, lowStockItems, totalMovements, recentMovements);

        return new DashboardMetrics(totalActiveProducts, lowStockItems, totalMovements, recentMovements);
    }

    @Override
    public List<TopProductMovement> getTopProductsByMovement(Long tenantId, int limit) {
        log.debug("Fetching top {} products by movement for tenant: {}", limit, tenantId);

        List<Tuple> results = entityManager.createNativeQuery(
                        """
                        SELECT
                            p.id as product_id,
                            p.name as product_name,
                            p.sku as product_sku,
                            COUNT(m.id) as movement_count,
                            COALESCE(SUM(m.quantity), 0) as total_quantity
                        FROM products p
                        LEFT JOIN stock_movements m ON p.id = m.product_id AND p.tenant_id = m.tenant_id
                        WHERE p.tenant_id = :tenantId
                          AND p.is_active = true
                        GROUP BY p.id, p.name, p.sku
                        ORDER BY movement_count DESC
                        LIMIT :limit
                        """,
                        Tuple.class
                )
                .setParameter("tenantId", tenantId)
                .setParameter("limit", limit)
                .getResultList();

        return results.stream()
                .map(tuple -> new TopProductMovement(
                        tuple.get("product_id", Number.class).longValue(),
                        tuple.get("product_name", String.class),
                        tuple.get("product_sku", String.class),
                        tuple.get("movement_count", Number.class).intValue(),
                        tuple.get("total_quantity", Number.class).intValue()
                ))
                .toList();
    }

    @Override
    public List<TopProductMovement> getTopProductsByMovementForBranch(Long tenantId, Long branchId, int limit) {
        log.debug("Fetching top {} products by movement for tenant: {}, branch: {}", limit, tenantId, branchId);

        List<Tuple> results = entityManager.createNativeQuery(
                        """
                        SELECT
                            p.id as product_id,
                            p.name as product_name,
                            p.sku as product_sku,
                            COUNT(m.id) as movement_count,
                            COALESCE(SUM(m.quantity), 0) as total_quantity
                        FROM products p
                        LEFT JOIN stock_movements m ON p.id = m.product_id
                            AND p.tenant_id = m.tenant_id
                            AND m.branch_id = :branchId
                        WHERE p.tenant_id = :tenantId
                          AND p.is_active = true
                        GROUP BY p.id, p.name, p.sku
                        ORDER BY movement_count DESC
                        LIMIT :limit
                        """,
                        Tuple.class
                )
                .setParameter("tenantId", tenantId)
                .setParameter("branchId", branchId)
                .setParameter("limit", limit)
                .getResultList();

        return results.stream()
                .map(tuple -> new TopProductMovement(
                        tuple.get("product_id", Number.class).longValue(),
                        tuple.get("product_name", String.class),
                        tuple.get("product_sku", String.class),
                        tuple.get("movement_count", Number.class).intValue(),
                        tuple.get("total_quantity", Number.class).intValue()
                ))
                .toList();
    }
}
