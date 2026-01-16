package com.stockflow.modules.users.domain.model;

import com.stockflow.shared.domain.exception.ValidationException;
import com.stockflow.shared.domain.model.BaseEntity;
import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.Where;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

/**
 * User entity representing a system user.
 *
 * <p>Users are scoped to a specific tenant and can have multiple roles and branch access.</p>
 *
 * <p>Invariants:</p>
 * <ul>
 *   <li>Name must be between 2 and 255 characters</li>
 *   <li>Email must be valid and unique within the tenant</li>
 *   <li>Password hash must be set and use BCrypt format</li>
 * </ul>
 */
@Entity
@Table(name = "users")
@SQLDelete(sql = "UPDATE users SET is_active = false WHERE id = ?")
@Where(clause = "is_active = true")
public class User extends BaseEntity {

    /**
     * Full name of the user.
     */
    @Column(name = "name", nullable = false, length = 255)
    private String name;

    /**
     * Email address of the user.
     * Must be unique within the tenant (scoped by tenant_id).
     */
    @Column(name = "email", nullable = false, length = 255)
    private String email;

    /**
     * BCrypt hash of the user's password.
     * Never store plain text passwords.
     */
    @Column(name = "password_hash", nullable = false)
    private String passwordHash;

    /**
     * Indicates whether the user is active.
     * Inactive users cannot authenticate.
     */
    @Column(name = "is_active", nullable = false)
    private Boolean isActive = true;

    /**
     * Roles assigned to this user.
     * Many-to-many relationship with lazy loading.
     */
    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
        name = "user_roles",
        joinColumns = @JoinColumn(name = "user_id"),
        inverseJoinColumns = @JoinColumn(name = "role_id")
    )
    private Set<Role> roles = new HashSet<>();

    /**
     * Branches this user has access to.
     * Many-to-many relationship with lazy loading.
     */
    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
        name = "user_branches",
        joinColumns = @JoinColumn(name = "user_id"),
        inverseJoinColumns = @JoinColumn(name = "branch_id")
    )
    private Set<Branch> branches = new HashSet<>();

    /**
     * Default constructor for JPA.
     */
    protected User() {
    }

    /**
     * Constructor for creating a new user.
     *
     * @param tenantId      the tenant ID
     * @param name          the user's full name
     * @param email         the user's email
     * @param passwordHash  the BCrypt password hash
     * @throws ValidationException if validation fails
     */
    public User(Long tenantId, String name, String email, String passwordHash) {
        setTenantId(tenantId);
        setName(name);
        setEmail(email);
        setPasswordHash(passwordHash);
        this.isActive = true;
    }

    /**
     * Validates and sets the user's name.
     *
     * @param name the name to set
     * @throws ValidationException if name is invalid
     */
    public void setName(String name) {
        if (name == null || name.trim().isEmpty()) {
            throw new ValidationException("VALIDATION_ERROR", "User name cannot be empty");
        }
        if (name.length() < 2 || name.length() > 255) {
            throw new ValidationException("VALIDATION_ERROR",
                "User name must be between 2 and 255 characters");
        }
        this.name = name.trim();
    }

    /**
     * Validates and sets the user's email.
     *
     * @param email the email to set
     * @throws ValidationException if email is invalid
     */
    public void setEmail(String email) {
        if (email == null || email.trim().isEmpty()) {
            throw new ValidationException("VALIDATION_ERROR", "User email cannot be empty");
        }
        String trimmedEmail = email.trim().toLowerCase();
        if (!trimmedEmail.matches("^[A-Za-z0-9+_.-]+@(.+)$")) {
            throw new ValidationException("VALIDATION_ERROR", "Invalid email format");
        }
        this.email = trimmedEmail;
    }

    /**
     * Sets the password hash.
     * Should only be called with BCrypt hashes.
     *
     * @param passwordHash the BCrypt password hash
     * @throws ValidationException if hash is null or empty
     */
    public void setPasswordHash(String passwordHash) {
        if (passwordHash == null || passwordHash.trim().isEmpty()) {
            throw new ValidationException("VALIDATION_ERROR", "Password hash cannot be empty");
        }
        this.passwordHash = passwordHash;
    }

    /**
     * Sets the active status.
     *
     * @param active the active status
     */
    public void setActive(Boolean active) {
        this.isActive = active != null ? active : false;
    }

    /**
     * Activates the user.
     */
    public void activate() {
        this.isActive = true;
    }

    /**
     * Deactivates the user (soft delete).
     */
    public void deactivate() {
        this.isActive = false;
    }

    /**
     * Adds a role to the user.
     *
     * @param role the role to add
     */
    public void addRole(Role role) {
        if (role != null) {
            this.roles.add(role);
        }
    }

    /**
     * Removes a role from the user.
     *
     * @param role the role to remove
     */
    public void removeRole(Role role) {
        this.roles.remove(role);
    }

    /**
     * Sets all roles for the user.
     *
     * @param roles the set of roles
     */
    public void setRoles(Set<Role> roles) {
        this.roles = roles != null ? new HashSet<>(roles) : new HashSet<>();
    }

    /**
     * Adds branch access for the user.
     *
     * @param branch the branch to add access to
     */
    public void addBranch(Branch branch) {
        if (branch != null) {
            this.branches.add(branch);
        }
    }

    /**
     * Removes branch access from the user.
     *
     * @param branch the branch to remove access from
     */
    public void removeBranch(Branch branch) {
        this.branches.remove(branch);
    }

    /**
     * Sets all branches for the user.
     *
     * @param branches the set of branches
     */
    public void setBranches(Set<Branch> branches) {
        this.branches = branches != null ? new HashSet<>(branches) : new HashSet<>();
    }

    /**
     * Checks if the user has a specific role.
     *
     * @param roleEnum the role to check
     * @return true if the user has the role
     */
    public boolean hasRole(RoleEnum roleEnum) {
        return roles.stream().anyMatch(role -> role.getName() == roleEnum);
    }

    /**
     * Checks if the user has access to a specific branch.
     *
     * @param branchId the branch ID
     * @return true if the user has access
     */
    public boolean hasAccessToBranch(Long branchId) {
        return branches.stream().anyMatch(branch -> branch.getId().equals(branchId));
    }

    // Getters

    public String getName() {
        return name;
    }

    public String getEmail() {
        return email;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public Boolean getIsActive() {
        return isActive;
    }

    public boolean isActive() {
        return isActive != null && isActive;
    }

    public Set<Role> getRoles() {
        return new HashSet<>(roles);
    }

    public Set<Branch> getBranches() {
        return new HashSet<>(branches);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof User)) return false;
        if (!super.equals(o)) return false;
        User user = (User) o;
        return Objects.equals(email, user.email) && Objects.equals(getTenantId(), user.getTenantId());
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), email, getTenantId());
    }

    @Override
    public String toString() {
        return String.format("User[id=%d, tenantId=%d, name='%s', email='%s', isActive=%s]",
            getId(), getTenantId(), name, email, isActive);
    }
}
