package com.stockflow.modules.users.domain.repository;

import com.stockflow.modules.users.domain.model.RoleEnum;
import com.stockflow.modules.users.domain.model.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    /**
     * Busca user por email e tenant
     */
    Optional<User> findByEmailAndTenantId(String email, Long tenantId);

    /**
     * Busca user por email (global)
     */
    Optional<User> findByEmail(String email);

    /**
     * Verifica se email existe no tenant
     */
    boolean existsByEmailAndTenantId(String email, Long tenantId);

    /**
     * Verifica se email existe ignorando um id (para update)
     */
    @Query("SELECT CASE WHEN COUNT(u) > 0 THEN true ELSE false END FROM User u " +
           "WHERE u.email = :email AND u.tenantId = :tenantId AND u.id != :id")
    boolean existsByEmailAndTenantIdAndIdNot(
        @Param("email") String email,
        @Param("tenantId") Long tenantId,
        @Param("id") Long id
    );

    /**
     * Busca todos os users do tenant
     */
    List<User> findByTenantId(Long tenantId);

    /**
     * Busca users ativos do tenant
     */
    List<User> findByTenantIdAndIsActiveTrue(Long tenantId);

    /**
     * Busca users ativos do tenant com paginação
     */
    Page<User> findByTenantIdAndIsActiveTrue(Long tenantId, Pageable pageable);

    /**
     * Busca users por nome (like)
     */
    List<User> findByTenantIdAndNameContainingIgnoreCase(Long tenantId, String name);

    /**
     * Busca users com role específica
     */
    @Query("SELECT DISTINCT u FROM User u " +
           "JOIN u.userRoles ur " +
           "JOIN ur.role r " +
           "WHERE u.tenantId = :tenantId " +
           "AND r.name = :role")
    List<User> findByTenantIdAndRole(
        @Param("tenantId") Long tenantId,
        @Param("role") RoleEnum role
    );

    /**
     * Busca users com role específica e ativos
     */
    @Query("SELECT DISTINCT u FROM User u " +
           "JOIN u.userRoles ur " +
           "JOIN ur.role r " +
           "WHERE u.tenantId = :tenantId " +
           "AND r.name = :role " +
           "AND u.isActive = true")
    List<User> findByTenantIdAndRoleAndIsActive(
        @Param("tenantId") Long tenantId,
        @Param("role") RoleEnum role
    );

    /**
     * Conta users ativos do tenant
     */
    long countByTenantIdAndIsActiveTrue(Long tenantId);

    /**
     * Conta users por role
     */
    @Query("SELECT COUNT(DISTINCT u) FROM User u " +
           "JOIN u.userRoles ur " +
           "JOIN ur.role r " +
           "WHERE u.tenantId = :tenantId " +
           "AND r.name = :role")
    long countByTenantIdAndRole(
        @Param("tenantId") Long tenantId,
        @Param("role") RoleEnum role
    );

    /**
     * Busca users com contas bloqueadas
     */
    List<User> findByTenantIdAndIsAccountLockedTrue(Long tenantId);
}
