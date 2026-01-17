package com.stockflow.modules.users.domain.model;

import jakarta.persistence.*;
import java.io.Serializable;
import java.util.Objects;

/**
 * Primary key class for UserBranch entity.
 *
 * <p>This class represents the composite primary key for the user_branches junction table,
 * which has a primary key of (user_id, branch_id).</p>
 */
@Embeddable
public class UserBranchId implements Serializable {

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "branch_id", nullable = false)
    private Long branchId;

    /**
     * Default constructor for JPA.
     */
    public UserBranchId() {
    }

    /**
     * Constructor for creating a composite key.
     *
     * @param userId   the user ID
     * @param branchId the branch ID
     */
    public UserBranchId(Long userId, Long branchId) {
        this.userId = userId;
        this.branchId = branchId;
    }

    // Getters and Setters

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
        if (!(o instanceof UserBranchId)) return false;
        UserBranchId that = (UserBranchId) o;
        return Objects.equals(userId, that.userId) &&
               Objects.equals(branchId, that.branchId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(userId, branchId);
    }

    @Override
    public String toString() {
        return String.format("UserBranchId[userId=%d, branchId=%d]", userId, branchId);
    }
}
