package com.stockflow.modules.tenant.domain.repository;

import com.stockflow.modules.tenant.domain.model.Tenant;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository Spring Data JPA para Tenant.
 */
@Repository
public interface TenantRepository extends JpaRepository<Tenant, Long> {

    /**
     * Busca tenant por slug
     */
    Optional<Tenant> findBySlug(String slug);

    /**
     * Busca todos os tenants ativos
     */
    List<Tenant> findByIsActiveTrue();

    /**
     * Busca todos os tenants ativos com paginação
     */
    Page<Tenant> findByIsActiveTrue(Pageable pageable);

    /**
     * Verifica se slug existe
     */
    boolean existsBySlug(String slug);

    /**
     * Verifica se slug existe ignorando um id (para update)
     */
    @Query("SELECT CASE WHEN COUNT(t) > 0 THEN true ELSE false END FROM Tenant t " +
           "WHERE t.slug = :slug AND t.id != :id")
    boolean existsBySlugAndIdNot(@Param("slug") String slug, @Param("id") Long id);

    /**
     * Busca tenants por nome (like)
     */
    List<Tenant> findByNameContainingIgnoreCase(String name);

    /**
     * Conta tenants ativos
     */
    long countByIsActiveTrue();
}
