package com.stockflow.modules.catalog.domain.repository;

import com.stockflow.modules.catalog.domain.model.Category;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Repository interface for Category entity.
 *
 * <p>Provides data access operations for categories with tenant isolation.
 * All queries ensure proper tenant scoping to maintain multi-tenancy data isolation.</p>
 */
@Repository
public interface CategoryRepository extends JpaRepository<Category, Long> {

    /**
     * Finds a category by ID ensuring it belongs to the tenant.
     *
     * @param id       the category ID
     * @param tenantId the tenant ID
     * @return Optional containing the category if found
     */
    @Query("SELECT c FROM Category c WHERE c.id = :id AND c.tenantId = :tenantId AND c.isActive = true")
    Optional<Category> findByIdAndTenantId(@Param("id") Long id, @Param("tenantId") Long tenantId);

    /**
     * Finds all active categories for a tenant with pagination.
     *
     * @param tenantId the tenant ID
     * @param pageable pagination parameters
     * @return page of categories
     */
    @Query("SELECT c FROM Category c WHERE c.tenantId = :tenantId AND c.isActive = true")
    Page<Category> findAllByTenantId(@Param("tenantId") Long tenantId, Pageable pageable);

    /**
     * Finds a category by name within a specific tenant.
     *
     * @param name     the category name
     * @param tenantId the tenant ID
     * @return Optional containing the category if found
     */
    @Query("SELECT c FROM Category c WHERE c.name = :name AND c.tenantId = :tenantId AND c.isActive = true")
    Optional<Category> findByNameAndTenantId(@Param("name") String name, @Param("tenantId") Long tenantId);

    /**
     * Checks if a category with the given name exists in the tenant.
     *
     * @param name     the name to check
     * @param tenantId the tenant ID
     * @return true if a category with the name exists
     */
    @Query("SELECT COUNT(c) > 0 FROM Category c WHERE c.name = :name AND c.tenantId = :tenantId")
    boolean existsByNameAndTenantId(@Param("name") String name, @Param("tenantId") Long tenantId);

    /**
     * Counts active categories in a tenant.
     *
     * @param tenantId the tenant ID
     * @return count of active categories
     */
    @Query("SELECT COUNT(c) FROM Category c WHERE c.tenantId = :tenantId AND c.isActive = true")
    long countActiveByTenantId(@Param("tenantId") Long tenantId);
}
