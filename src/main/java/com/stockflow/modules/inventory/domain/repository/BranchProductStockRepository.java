package com.stockflow.modules.inventory.domain.repository;

import com.stockflow.modules.inventory.domain.model.BranchProductStock;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Repository interface for BranchProductStock entity.
 *
 * <p>Provides data access operations for branch stock with tenant isolation.
 * All queries ensure proper tenant scoping to maintain multi-tenancy data isolation.</p>
 */
@Repository
public interface BranchProductStockRepository extends JpaRepository<BranchProductStock, Long> {

    /**
     * Finds a stock entry by ID ensuring it belongs to the tenant.
     *
     * @param id       the stock entry ID
     * @param tenantId the tenant ID
     * @return Optional containing the stock entry if found
     */
    @Query("SELECT s FROM BranchProductStock s WHERE s.id = :id AND s.tenantId = :tenantId")
    Optional<BranchProductStock> findByIdAndTenantId(@Param("id") Long id, @Param("tenantId") Long tenantId);

    /**
     * Finds all stock entries for a tenant with pagination.
     *
     * @param tenantId the tenant ID
     * @param pageable pagination parameters
     * @return page of stock entries
     */
    @Query("SELECT s FROM BranchProductStock s WHERE s.tenantId = :tenantId")
    Page<BranchProductStock> findByTenantId(@Param("tenantId") Long tenantId, Pageable pageable);

    /**
     * Finds all stock entries for a specific branch within a tenant with pagination.
     *
     * @param branchId the branch ID
     * @param tenantId the tenant ID
     * @param pageable pagination parameters
     * @return page of stock entries for the branch
     */
    @Query("SELECT s FROM BranchProductStock s WHERE s.branchId = :branchId AND s.tenantId = :tenantId")
    Page<BranchProductStock> findByTenantIdAndBranchId(@Param("branchId") Long branchId,
                                                       @Param("tenantId") Long tenantId,
                                                       Pageable pageable);

    /**
     * Finds the stock entry for a specific product in a specific branch.
     *
     * @param branchId  the branch ID
     * @param productId the product ID
     * @param tenantId  the tenant ID
     * @return Optional containing the stock entry if found
     */
    @Query("SELECT s FROM BranchProductStock s WHERE s.branchId = :branchId AND s.productId = :productId AND s.tenantId = :tenantId")
    Optional<BranchProductStock> findByTenantIdAndBranchIdAndProductId(
        @Param("branchId") Long branchId,
        @Param("productId") Long productId,
        @Param("tenantId") Long tenantId
    );

    /**
     * Finds all stock entries for a specific product across all branches in a tenant with pagination.
     *
     * @param productId the product ID
     * @param tenantId  the tenant ID
     * @param pageable  pagination parameters
     * @return page of stock entries for the product
     */
    @Query("SELECT s FROM BranchProductStock s WHERE s.productId = :productId AND s.tenantId = :tenantId")
    Page<BranchProductStock> findByTenantIdAndProductId(
        @Param("productId") Long productId,
        @Param("tenantId") Long tenantId,
        Pageable pageable
    );

    /**
     * Checks if a stock entry exists for a specific product in a specific branch.
     *
     * @param branchId  the branch ID
     * @param productId the product ID
     * @param tenantId  the tenant ID
     * @return true if a stock entry exists
     */
    @Query("SELECT COUNT(s) > 0 FROM BranchProductStock s WHERE s.branchId = :branchId AND s.productId = :productId AND s.tenantId = :tenantId")
    boolean existsByTenantIdAndBranchIdAndProductId(
        @Param("branchId") Long branchId,
        @Param("productId") Long productId,
        @Param("tenantId") Long tenantId
    );

    /**
     * Counts all stock entries for a tenant.
     *
     * @param tenantId the tenant ID
     * @return count of stock entries
     */
    @Query("SELECT COUNT(s) FROM BranchProductStock s WHERE s.tenantId = :tenantId")
    long countByTenantId(@Param("tenantId") Long tenantId);

    /**
     * Counts stock entries for a specific branch.
     *
     * @param branchId the branch ID
     * @param tenantId the tenant ID
     * @return count of stock entries
     */
    @Query("SELECT COUNT(s) FROM BranchProductStock s WHERE s.branchId = :branchId AND s.tenantId = :tenantId")
    long countByTenantIdAndBranchId(@Param("branchId") Long branchId, @Param("tenantId") Long tenantId);
}
