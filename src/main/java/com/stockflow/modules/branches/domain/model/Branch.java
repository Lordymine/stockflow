package com.stockflow.modules.branches.domain.model;

import com.stockflow.shared.domain.model.BaseEntity;
import jakarta.persistence.*;
import org.hibernate.annotations.SQLDelete;
import java.util.Objects;

/**
 * Branch entity representing a physical location/branch of a tenant.
 *
 * <p>Branches are scoped to tenants and users can be granted access to specific branches
 * for access control. All inventory operations are tied to specific branches.</p>
 *
 * <p><strong>Soft Delete:</strong> Uses @SQLDelete for soft delete functionality.
 * When a branch is "deleted", it's marked as inactive (is_active = false) instead of being removed
 * from the database. This maintains data integrity and allows for audit trails.</p>
 *
 * <p>Invariants:</p>
 * <ul>
 *   <li>Name must be between 2 and 255 characters</li>
 *   <li>Code must be unique within the tenant</li>
 *   <li>Code must be between 2 and 50 characters</li>
 * </ul>
 */
@Entity
@Table(name = "branches")
@SQLDelete(sql = "UPDATE branches SET is_active = false WHERE id = ?")
public class Branch extends BaseEntity {

    /**
     * Human-readable name of the branch.
     */
    @Column(name = "name", nullable = false, length = 255)
    private String name;

    /**
     * Unique code for the branch within the tenant.
     * Used for quick identification and API references.
     */
    @Column(name = "code", nullable = false, length = 50)
    private String code;

    /**
     * Physical address of the branch.
     */
    @Column(name = "address", columnDefinition = "TEXT")
    private String address;

    /**
     * Contact phone number.
     */
    @Column(name = "phone", length = 20)
    private String phone;

    /**
     * Name of the branch manager.
     */
    @Column(name = "manager_name", length = 100)
    private String managerName;

    /**
     * Indicates whether the branch is active.
     * Inactive branches cannot be used for inventory operations.
     */
    @Column(name = "is_active", nullable = false)
    private Boolean isActive = true;

    /**
     * Default constructor for JPA.
     */
    protected Branch() {
    }

    /**
     * Constructor for creating a new branch.
     *
     * @param tenantId the tenant ID
     * @param name     the branch name
     * @param code     the branch code
     */
    public Branch(Long tenantId, String name, String code) {
        setTenantId(tenantId);
        setName(name);
        setCode(code);
        this.isActive = true;
    }

    // Validation methods

    public void setName(String name) {
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("Branch name cannot be empty");
        }
        if (name.length() < 2 || name.length() > 255) {
            throw new IllegalArgumentException("Branch name must be between 2 and 255 characters");
        }
        this.name = name.trim();
    }

    public void setCode(String code) {
        if (code == null || code.trim().isEmpty()) {
            throw new IllegalArgumentException("Branch code cannot be empty");
        }
        String trimmedCode = code.trim().toUpperCase();
        if (trimmedCode.length() < 2 || trimmedCode.length() > 50) {
            throw new IllegalArgumentException("Branch code must be between 2 and 50 characters");
        }
        this.code = trimmedCode;
    }

    public void setActive(Boolean active) {
        this.isActive = active != null ? active : false;
    }

    /**
     * Activates the branch.
     */
    public void activate() {
        this.isActive = true;
    }

    /**
     * Deactivates the branch (soft delete).
     * This will be automatically called when JPA delete() is used due to @SQLDelete annotation.
     */
    public void deactivate() {
        this.isActive = false;
    }

    // Getters

    public String getName() {
        return name;
    }

    public String getCode() {
        return code;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getManagerName() {
        return managerName;
    }

    public void setManagerName(String managerName) {
        this.managerName = managerName;
    }

    public Boolean getIsActive() {
        return isActive;
    }

    public boolean isActive() {
        return isActive != null && isActive;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Branch)) return false;
        if (!super.equals(o)) return false;
        Branch branch = (Branch) o;
        return Objects.equals(code, branch.code) &&
               Objects.equals(getTenantId(), branch.getTenantId());
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), code, getTenantId());
    }

    @Override
    public String toString() {
        return String.format("Branch[id=%d, tenantId=%d, name='%s', code='%s', isActive=%s]",
            getId(), getTenantId(), name, code, isActive);
    }
}
