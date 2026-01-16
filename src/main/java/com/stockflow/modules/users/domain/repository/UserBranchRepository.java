package com.stockflow.modules.users.domain.repository;

import com.stockflow.modules.users.domain.model.UserBranch;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repository interface for UserBranch entity.
 *
 * <p>Provides data access operations for user-branch associations.</p>
 */
@Repository
public interface UserBranchRepository extends JpaRepository<UserBranch, Long> {

    /**
     * Finds all branches for a specific user.
     *
     * @param userId the user ID
     * @return list of user-branch associations
     */
    List<UserBranch> findByUserId(Long userId);

    /**
     * Checks if a user has access to a specific branch.
     *
     * @param userId  the user ID
     * @param branchId the branch ID
     * @return true if the user has access
     */
    @Query("SELECT COUNT(ub) > 0 FROM UserBranch ub WHERE ub.userId = :userId AND ub.branchId = :branchId")
    boolean existsByUserIdAndBranchId(@Param("userId") Long userId, @Param("branchId") Long branchId);

    /**
     * Deletes all branch associations for a user.
     *
     * @param userId the user ID
     */
    void deleteByUserId(Long userId);

    /**
     * Finds all users with access to a specific branch.
     *
     * @param branchId the branch ID
     * @return list of user-branch associations
     */
    List<UserBranch> findByBranchId(Long branchId);
}
