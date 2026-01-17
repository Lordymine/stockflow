package com.stockflow.modules.users.domain.model;

import jakarta.persistence.*;
import java.io.Serializable;
import java.util.Objects;

/**
 * Primary key class for UserRole entity.
 *
 * <p>This class represents the composite primary key for the user_roles junction table,
 * which has a primary key of (user_id, role_id).</p>
 */
@Embeddable
public class UserRoleId implements Serializable {

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "role_id", nullable = false)
    private Long roleId;

    /**
     * Default constructor for JPA.
     */
    public UserRoleId() {
    }

    /**
     * Constructor for creating a composite key.
     *
     * @param userId  the user ID
     * @param roleId  the role ID
     */
    public UserRoleId(Long userId, Long roleId) {
        this.userId = userId;
        this.roleId = roleId;
    }

    // Getters and Setters

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public Long getRoleId() {
        return roleId;
    }

    public void setRoleId(Long roleId) {
        this.roleId = roleId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof UserRoleId)) return false;
        UserRoleId that = (UserRoleId) o;
        return Objects.equals(userId, that.userId) &&
               Objects.equals(roleId, that.roleId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(userId, roleId);
    }

    @Override
    public String toString() {
        return String.format("UserRoleId[userId=%d, roleId=%d]", userId, roleId);
    }
}
