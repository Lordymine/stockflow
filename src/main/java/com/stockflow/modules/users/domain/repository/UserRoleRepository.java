package com.stockflow.modules.users.domain.repository;

import com.stockflow.modules.users.domain.model.UserRole;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRoleRepository extends JpaRepository<UserRole, Long> {

    /**
     * Busca todos os roles de um user
     */
    List<UserRole> findByUserId(Long userId);

    /**
     * Busca user-role específico
     */
    Optional<UserRole> findByUserIdAndRoleId(Long userId, Long roleId);

    /**
     * Deleta todos os roles de um user
     */
    void deleteByUserId(Long userId);

    /**
     * Verifica se user tem role específica
     */
    @Query("SELECT CASE WHEN COUNT(ur) > 0 THEN true ELSE false END FROM UserRole ur " +
           "WHERE ur.user.id = :userId " +
           "AND ur.role.id = :roleId")
    boolean existsByUserIdAndRoleId(
        @Param("userId") Long userId,
        @Param("roleId") Long roleId
    );

    /**
     * Conta roles de um user
     */
    long countByUserId(Long userId);
}
