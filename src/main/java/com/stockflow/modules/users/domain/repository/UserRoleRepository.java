package com.stockflow.modules.users.domain.repository;

import com.stockflow.modules.users.domain.model.UserRole;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repository interface for UserRole entity.
 *
 * <p>Provides data access operations for user-role associations.</p>
 */
@Repository
public interface UserRoleRepository extends JpaRepository<UserRole, Long> {

    /**
     * Finds all roles for a specific user.
     *
     * @param userId the user ID
     * @return list of user-role associations
     */
    List<UserRole> findByUserId(Long userId);

    /**
     * Deletes all role associations for a user.
     *
     * @param userId the user ID
     */
    void deleteByUserId(Long userId);
}
