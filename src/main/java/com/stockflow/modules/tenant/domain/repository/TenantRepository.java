package com.stockflow.modules.tenant.domain.repository;

import com.stockflow.modules.tenant.domain.model.Tenant;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Repository interface for Tenant entity.
 *
 * <p>Provides data access operations for tenants.</p>
 */
@Repository
public interface TenantRepository extends JpaRepository<Tenant, Long> {

    /**
     * Finds a tenant by its slug.
     *
     * @param slug the unique slug identifier
     * @return Optional containing the tenant if found
     */
    Optional<Tenant> findBySlug(String slug);

    /**
     * Checks if a tenant with the given slug exists.
     *
     * @param slug the slug to check
     * @return true if a tenant with the slug exists
     */
    boolean existsBySlug(String slug);

    /**
     * Finds the first active tenant.
     * Used during bootstrap to verify if initialization is needed.
     *
     * @return Optional containing the first active tenant if any exists
     */
    @Query("SELECT t FROM Tenant t WHERE t.isActive = true ORDER BY t.id ASC")
    Optional<Tenant> findFirstActiveTenant();

    /**
     * Counts the total number of active tenants.
     *
     * @return the count of active tenants
     */
    @Query("SELECT COUNT(t) FROM Tenant t WHERE t.isActive = true")
    long countActiveTenants();

    /**
     * Finds a tenant by ID ensuring it is active.
     *
     * @param id the tenant ID
     * @return Optional containing the active tenant if found
     */
    @Query("SELECT t FROM Tenant t WHERE t.id = :id AND t.isActive = true")
    Optional<Tenant> findActiveById(@Param("id") Long id);
}
