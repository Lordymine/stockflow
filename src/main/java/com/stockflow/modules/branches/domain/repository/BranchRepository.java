package com.stockflow.modules.branches.domain.repository;

import com.stockflow.modules.branches.domain.model.Branch;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface BranchRepository extends JpaRepository<Branch, Long> {

    Optional<Branch> findByIdAndTenantId(Long id, Long tenantId);

    List<Branch> findByTenantId(Long tenantId);

    Page<Branch> findByTenantId(Long tenantId, Pageable pageable);

    List<Branch> findByTenantIdAndIsActiveTrue(Long tenantId);

    Page<Branch> findByTenantIdAndIsActiveTrue(Long tenantId, Pageable pageable);

    boolean existsByCodeAndTenantId(String code, Long tenantId);

    Optional<Branch> findByCodeAndTenantId(String code, Long tenantId);

    @Query("SELECT b FROM Branch b WHERE b.tenantId = :tenantId AND " +
           "b.isActive = true AND " +
           "(LOWER(b.name) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "LOWER(b.code) LIKE LOWER(CONCAT('%', :search, '%')))")
    Page<Branch> searchActiveBranches(@Param("tenantId") Long tenantId,
                                      @Param("search") String search,
                                      Pageable pageable);

    @Query("SELECT b FROM Branch b WHERE b.tenantId = :tenantId AND " +
           "(LOWER(b.name) LIKE LOWER(CONCAT('%', :name, '%')))")
    Page<Branch> findByNameContaining(@Param("tenantId") Long tenantId,
                                      @Param("name") String name,
                                      Pageable pageable);

    long countByTenantId(Long tenantId);

    long countByTenantIdAndIsActiveTrue(Long tenantId);

    @Query("SELECT b FROM Branch b WHERE b.id IN :ids AND b.tenantId = :tenantId")
    List<Branch> findByIdInAndTenantId(@Param("ids") List<Long> ids,
                                        @Param("tenantId") Long tenantId);
}
