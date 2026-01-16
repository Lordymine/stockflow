package com.stockflow.modules.users.domain.repository;

import com.stockflow.modules.users.domain.model.Role;
import com.stockflow.modules.users.domain.model.RoleEnum;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Repository interface for Role entity.
 *
 * <p>Provides data access operations for roles.</p>
 */
@Repository
public interface RoleRepository extends JpaRepository<Role, Long> {

    /**
     * Finds a role by its enum name.
     *
     * @param name the role enum
     * @return Optional containing the role if found
     */
    Optional<Role> findByName(RoleEnum name);

    /**
     * Checks if a role exists by name.
     *
     * @param name the role enum
     * @return true if the role exists
     */
    boolean existsByName(RoleEnum name);
}
