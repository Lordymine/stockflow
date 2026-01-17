package com.stockflow.modules.users.domain.model;

import jakarta.persistence.*;
import java.util.Objects;

/**
 * UserBranch entity representing the junction table between users and branches.
 *
 * <p>This entity defines which branches a user has access to.
 * Access control is enforced at the service layer using the @BranchAccess annotation.</p>
 *
 * <p><strong>NOTE:</strong> This entity is kept for explicit repository queries and management.
 * The User entity uses standard @ManyToMany mapping for relationship management.</p>
 *
 * @see com.stockflow.shared.infrastructure.security.BranchAccess
 */
@Entity
@Table(name = "user_branches")
@IdClass(UserBranchId.class)
public class UserBranch {

    @Id
    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Id
    @Column(name = "branch_id", nullable = false)
    private Long branchId;

    /**
     * Default constructor for JPA.
     */
    protected UserBranch() {
    }

    /**
     * Constructor for creating a user-branch association.
     *
     * @param userId  the user ID
     * @param branchId the branch ID
     */
    public UserBranch(Long userId, Long branchId) {
        this.userId = userId;
        this.branchId = branchId;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public Long getBranchId() {
        return branchId;
    }

    public void setBranchId(Long branchId) {
        this.branchId = branchId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof UserBranch)) return false;
        UserBranch userBranch = (UserBranch) o;
        return Objects.equals(userId, userBranch.userId) &&
               Objects.equals(branchId, userBranch.branchId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(userId, branchId);
    }

    @Override
    public String toString() {
        return String.format("UserBranch[userId=%d, branchId=%d]", userId, branchId);
    }
}
