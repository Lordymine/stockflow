package com.stockflow.modules.users.domain.repository;

import com.stockflow.modules.users.domain.model.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Repository interface for User entity.
 *
 * <p>Provides data access operations for users with tenant isolation.</p>
 */
@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    /**
     * Finds a user by email within a specific tenant.
     *
     * @param email    the user's email
     * @param tenantId the tenant ID
     * @return Optional containing the user if found
     */
    @Query("SELECT u FROM User u WHERE u.email = :email AND u.tenantId = :tenantId AND u.isActive = true")
    Optional<User> findByEmailAndTenantId(@Param("email") String email, @Param("tenantId") Long tenantId);

    /**
     * Finds a user by email across all tenants.
     * Used during login when tenant context is not yet available.
     *
     * @param email the user's email
     * @return Optional containing the user if found
     */
    @Query("SELECT u FROM User u WHERE u.email = :email AND u.isActive = true")
    Optional<User> findByEmail(@Param("email") String email);

    /**
     * Checks if an active user with the given email exists across all tenants.
     *
     * @param email the email to check
     * @return true if an active user with the email exists
     */
    @Query("SELECT COUNT(u) > 0 FROM User u WHERE u.email = :email AND u.isActive = true")
    boolean existsActiveByEmail(@Param("email") String email);

    /**
     * Checks if a user with the given email exists in the tenant.
     *
     * @param email    the email to check
     * @param tenantId the tenant ID
     * @return true if a user with the email exists
     */
    @Query("SELECT COUNT(u) > 0 FROM User u WHERE u.email = :email AND u.tenantId = :tenantId")
    boolean existsByEmailAndTenantId(@Param("email") String email, @Param("tenantId") Long tenantId);

    /**
     * Finds a user by ID ensuring they belong to the tenant.
     *
     * @param id       the user ID
     * @param tenantId the tenant ID
     * @return Optional containing the user if found
     */
    @Query("SELECT u FROM User u WHERE u.id = :id AND u.tenantId = :tenantId AND u.isActive = true")
    Optional<User> findByIdAndTenantId(@Param("id") Long id, @Param("tenantId") Long tenantId);

    /**
     * Finds a user by ID regardless of active status.
     *
     * @param id       the user ID
     * @param tenantId the tenant ID
     * @return Optional containing the user if found
     */
    @Query("SELECT u FROM User u WHERE u.id = :id AND u.tenantId = :tenantId")
    Optional<User> findByIdAndTenantIdIncludingInactive(@Param("id") Long id, @Param("tenantId") Long tenantId);

    /**
     * Finds all active users for a tenant with pagination.
     *
     * @param tenantId the tenant ID
     * @param pageable pagination parameters
     * @return page of users
     */
    @Query("SELECT u FROM User u WHERE u.tenantId = :tenantId AND u.isActive = true")
    Page<User> findAllByTenantId(@Param("tenantId") Long tenantId, Pageable pageable);

    /**
     * Finds users by name containing the search string within a tenant.
     *
     * @param tenantId the tenant ID
     * @param name     the name to search for
     * @param pageable pagination parameters
     * @return page of matching users
     */
    @Query("SELECT u FROM User u WHERE u.tenantId = :tenantId AND u.isActive = true AND LOWER(u.name) LIKE LOWER(CONCAT('%', :name, '%'))")
    Page<User> findByTenantIdAndNameContaining(@Param("tenantId") Long tenantId, @Param("name") String name, Pageable pageable);

    /**
     * Counts active users in a tenant.
     *
     * @param tenantId the tenant ID
     * @return count of active users
     */
    @Query("SELECT COUNT(u) FROM User u WHERE u.tenantId = :tenantId AND u.isActive = true")
    long countActiveByTenantId(@Param("tenantId") Long tenantId);

    /**
     * Finds a user with roles and branches eagerly loaded.
     *
     * @param id       the user ID
     * @param tenantId the tenant ID
     * @return Optional containing the user with relationships loaded
     */
    @Query("SELECT u FROM User u LEFT JOIN FETCH u.roles LEFT JOIN FETCH u.branches WHERE u.id = :id AND u.tenantId = :tenantId AND u.isActive = true")
    Optional<User> findByIdWithRolesAndBranches(@Param("id") Long id, @Param("tenantId") Long tenantId);
}
