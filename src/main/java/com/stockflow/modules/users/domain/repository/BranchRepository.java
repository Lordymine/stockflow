package com.stockflow.modules.users.domain.repository;

import com.stockflow.modules.users.domain.model.Branch;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository interface for Branch entity.
 *
 * <p>Provides data access operations for branches with tenant isolation.</p>
 */
@Repository
public interface BranchRepository extends JpaRepository<Branch, Long> {

    /**
     * Finds a branch by code within a tenant.
     *
     * @param code     the branch code
     * @param tenantId the tenant ID
     * @return Optional containing the branch if found
     */
    @Query("SELECT b FROM Branch b WHERE b.code = :code AND b.tenantId = :tenantId")
    Optional<Branch> findByCodeAndTenantId(@Param("code") String code, @Param("tenantId") Long tenantId);

    /**
     * Checks if a branch with the code exists in the tenant.
     *
     * @param code     the code to check
     * @param tenantId the tenant ID
     * @return true if a branch with the code exists
     */
    @Query("SELECT COUNT(b) > 0 FROM Branch b WHERE b.code = :code AND b.tenantId = :tenantId")
    boolean existsByCodeAndTenantId(@Param("code") String code, @Param("tenantId") Long tenantId);

    /**
     * Finds all active branches for a tenant with pagination.
     *
     * @param tenantId the tenant ID
     * @param pageable pagination parameters
     * @return page of branches
     */
    @Query("SELECT b FROM Branch b WHERE b.tenantId = :tenantId AND b.isActive = true")
    Page<Branch> findAllByTenantId(@Param("tenantId") Long tenantId, Pageable pageable);

    /**
     * Finds all active branches for a tenant (no pagination).
     *
     * @param tenantId the tenant ID
     * @return list of active branches
     */
    @Query("SELECT b FROM Branch b WHERE b.tenantId = :tenantId AND b.isActive = true ORDER BY b.name ASC")
    List<Branch> findAllActiveByTenantId(@Param("tenantId") Long tenantId);

    /**
     * Finds a branch by ID ensuring it belongs to the tenant.
     *
     * @param id       the branch ID
     * @param tenantId the tenant ID
     * @return Optional containing the branch if found
     */
    @Query("SELECT b FROM Branch b WHERE b.id = :id AND b.tenantId = :tenantId AND b.isActive = true")
    Optional<Branch> findByIdAndTenantId(@Param("id") Long id, @Param("tenantId") Long tenantId);

    /**
     * Finds branches by name containing the search string within a tenant.
     *
     * @param tenantId the tenant ID
     * @param name     the name to search for
     * @param pageable pagination parameters
     * @return page of matching branches
     */
    @Query("SELECT b FROM Branch b WHERE b.tenantId = :tenantId AND b.isActive = true AND LOWER(b.name) LIKE LOWER(CONCAT('%', :name, '%'))")
    Page<Branch> findByTenantIdAndNameContaining(@Param("tenantId") Long tenantId, @Param("name") String name, Pageable pageable);

    /**
     * Counts active branches in a tenant.
     *
     * @param tenantId the tenant ID
     * @return count of active branches
     */
    @Query("SELECT COUNT(b) FROM Branch b WHERE b.tenantId = :tenantId AND b.isActive = true")
    long countActiveByTenantId(@Param("tenantId") Long tenantId);
}
