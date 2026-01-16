package com.stockflow.modules.users.domain.repository;

import com.stockflow.modules.users.domain.model.UserBranch;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserBranchRepository extends JpaRepository<UserBranch, Long> {

    /**
     * Busca todos os branches de um user
     */
    List<UserBranch> findByUserId(Long userId);

    /**
     * Busca user-branch específico
     */
    Optional<UserBranch> findByUserIdAndBranchId(Long userId, Long branchId);

    /**
     * Deleta todos os branches de um user
     */
    void deleteByUserId(Long userId);

    /**
     * Verifica se user tem acesso a branch
     */
    @Query("SELECT CASE WHEN COUNT(ub) > 0 THEN true ELSE false END FROM UserBranch ub " +
           "WHERE ub.user.id = :userId " +
           "AND ub.branchId = :branchId")
    boolean existsByUserIdAndBranchId(
        @Param("userId") Long userId,
        @Param("branchId") Long branchId
    );

    /**
     * Conta branches de um user
     */
    long countByUserId(Long userId);

    /**
     * Busca todos os user-branch de um tenant
     */
    @Query("SELECT ub FROM UserBranch ub WHERE ub.tenantId = :tenantId")
    List<UserBranch> findByTenantId(@Param("tenantId") Long tenantId);
}
