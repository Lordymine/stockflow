package com.stockflow.modules.inventory.domain.repository;

import com.stockflow.modules.inventory.domain.model.MovementReason;
import com.stockflow.modules.inventory.domain.model.MovementType;
import com.stockflow.modules.inventory.domain.model.StockMovement;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

/**
 * Repository interface for StockMovement entity.
 *
 * <p>Provides data access operations for stock movements with tenant isolation.
 * All queries ensure proper tenant scoping to maintain multi-tenancy data isolation.</p>
 *
 * <p>Stock movements are immutable records that provide a complete audit trail
 * of all stock changes in the system.</p>
 */
@Repository
public interface StockMovementRepository extends JpaRepository<StockMovement, Long> {

    /**
     * Finds all stock movements for a tenant with pagination.
     *
     * @param tenantId the tenant ID
     * @param pageable pagination parameters
     * @return page of stock movements
     */
    @Query("SELECT m FROM StockMovement m WHERE m.tenantId = :tenantId ORDER BY m.createdAt DESC")
    Page<StockMovement> findByTenantId(@Param("tenantId") Long tenantId, Pageable pageable);

    /**
     * Finds all stock movements for a specific branch within a tenant with pagination.
     *
     * @param branchId the branch ID
     * @param tenantId the tenant ID
     * @param pageable pagination parameters
     * @return page of stock movements for the branch
     */
    @Query("SELECT m FROM StockMovement m WHERE m.branchId = :branchId AND m.tenantId = :tenantId ORDER BY m.createdAt DESC")
    Page<StockMovement> findByBranchIdAndTenantId(@Param("branchId") Long branchId,
                                                   @Param("tenantId") Long tenantId,
                                                   Pageable pageable);

    /**
     * Finds stock movements for a branch with optional filters.
     *
     * @param branchId the branch ID
     * @param tenantId the tenant ID
     * @param productId optional product ID filter
     * @param type optional movement type filter
     * @param reason optional movement reason filter
     * @param pageable pagination parameters
     * @return page of stock movements
     */
    @Query("SELECT m FROM StockMovement m WHERE m.branchId = :branchId AND m.tenantId = :tenantId " +
           "AND (:productId IS NULL OR m.productId = :productId) " +
           "AND (:type IS NULL OR m.type = :type) " +
           "AND (:reason IS NULL OR m.reason = :reason) " +
           "ORDER BY m.createdAt DESC")
    Page<StockMovement> findByBranchWithFilters(@Param("branchId") Long branchId,
                                                @Param("tenantId") Long tenantId,
                                                @Param("productId") Long productId,
                                                @Param("type") MovementType type,
                                                @Param("reason") MovementReason reason,
                                                Pageable pageable);

    /**
     * Finds all stock movements for a specific product within a tenant with pagination.
     *
     * @param productId the product ID
     * @param tenantId  the tenant ID
     * @param pageable  pagination parameters
     * @return page of stock movements for the product
     */
    @Query("SELECT m FROM StockMovement m WHERE m.productId = :productId AND m.tenantId = :tenantId ORDER BY m.createdAt DESC")
    Page<StockMovement> findByProductIdAndTenantId(@Param("productId") Long productId,
                                                   @Param("tenantId") Long tenantId,
                                                   Pageable pageable);

    /**
     * Finds all stock movements for a specific product in a specific branch with pagination.
     *
     * @param branchId  the branch ID
     * @param productId the product ID
     * @param tenantId  the tenant ID
     * @param pageable  pagination parameters
     * @return page of stock movements
     */
    @Query("SELECT m FROM StockMovement m WHERE m.branchId = :branchId AND m.productId = :productId AND m.tenantId = :tenantId ORDER BY m.createdAt DESC")
    Page<StockMovement> findByTenantIdAndBranchIdAndProductId(
        @Param("branchId") Long branchId,
        @Param("productId") Long productId,
        @Param("tenantId") Long tenantId,
        Pageable pageable
    );

    /**
     * Finds recent stock movements for a specific product in a specific branch.
     *
     * @param branchId  the branch ID
     * @param productId the product ID
     * @param tenantId  the tenant ID
     * @param pageable  pagination parameters (limit for recent entries)
     * @return page of recent stock movements
     */
    @Query("SELECT m FROM StockMovement m WHERE m.branchId = :branchId AND m.productId = :productId AND m.tenantId = :tenantId ORDER BY m.createdAt DESC")
    Page<StockMovement> findRecentByTenantIdAndBranchIdAndProductId(
        @Param("branchId") Long branchId,
        @Param("productId") Long productId,
        @Param("tenantId") Long tenantId,
        Pageable pageable
    );

    /**
     * Counts all stock movements for a specific product in a specific branch.
     *
     * @param branchId  the branch ID
     * @param productId the product ID
     * @param tenantId  the tenant ID
     * @return count of stock movements
     */
    @Query("SELECT COUNT(m) FROM StockMovement m WHERE m.branchId = :branchId AND m.productId = :productId AND m.tenantId = :tenantId")
    long countByTenantIdAndBranchIdAndProductId(
        @Param("branchId") Long branchId,
        @Param("productId") Long productId,
        @Param("tenantId") Long tenantId
    );

    /**
     * Counts all stock movements for a tenant.
     *
     * @param tenantId the tenant ID
     * @return count of stock movements
     */
    @Query("SELECT COUNT(m) FROM StockMovement m WHERE m.tenantId = :tenantId")
    long countByTenantId(@Param("tenantId") Long tenantId);
}
