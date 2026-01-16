package com.stockflow.modules.users.domain.model;

import jakarta.persistence.*;
import java.util.Objects;

/**
 * UserRole entity representing the junction table between users and roles.
 *
 * <p>This is a many-to-many relationship table that associates users with their roles.
 * It's modeled as an entity for potential future extensions (e.g., granted_at, granted_by).</p>
 */
@Entity
@Table(name = "user_roles")
public class UserRole {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "role_id", nullable = false)
    private Long roleId;

    /**
     * Default constructor for JPA.
     */
    protected UserRole() {
    }

    /**
     * Constructor for creating a user-role association.
     *
     * @param userId the user ID
     * @param roleId the role ID
     */
    public UserRole(Long userId, Long roleId) {
        this.userId = userId;
        this.roleId = roleId;
    }

    // Getters and Setters

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

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
        if (!(o instanceof UserRole)) return false;
        UserRole userRole = (UserRole) o;
        return Objects.equals(userId, userRole.userId) &&
               Objects.equals(roleId, userRole.roleId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(userId, roleId);
    }

    @Override
    public String toString() {
        return String.format("UserRole[id=%d, userId=%d, roleId=%d]", id, userId, roleId);
    }
}
