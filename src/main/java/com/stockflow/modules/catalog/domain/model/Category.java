package com.stockflow.modules.catalog.domain.model;

import com.stockflow.shared.domain.model.BaseEntity;
import jakarta.persistence.*;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.Where;
import java.util.Objects;

/**
 * Category entity for organizing products in the catalog.
 *
 * <p>Categories represent hierarchical groupings for products within a tenant.
 * They provide a way to organize and classify products for better navigation and management.</p>
 *
 * <p><strong>Soft Delete:</strong> Uses @SQLDelete and @Where annotations for soft delete functionality.
 * When a category is "deleted", it's marked as inactive (is_active = false) instead of being removed
 * from the database. This maintains data integrity and allows for audit trails.</p>
 *
 * <p>Invariants:</p>
 * <ul>
 *   <li>Name must be between 2 and 100 characters</li>
 *   <li>Name must be unique within the tenant</li>
 * </ul>
 */
@Entity
@Table(name = "categories")
@SQLDelete(sql = "UPDATE categories SET is_active = false WHERE id = ?")
@Where(clause = "is_active = true")
public class Category extends BaseEntity {

    /**
     * Human-readable name of the category.
     */
    @Column(name = "name", nullable = false, length = 100)
    private String name;

    /**
     * Indicates whether the category is active.
     * Inactive categories cannot be assigned to products.
     */
    @Column(name = "is_active", nullable = false)
    private Boolean isActive = true;

    /**
     * Default constructor for JPA.
     */
    protected Category() {
    }

    /**
     * Constructor for creating a new category.
     *
     * @param tenantId the tenant ID
     * @param name     the category name
     */
    public Category(Long tenantId, String name) {
        setTenantId(tenantId);
        setName(name);
        this.isActive = true;
    }

    // Validation methods

    /**
     * Sets the category name with validation.
     *
     * @param name the category name
     * @throws IllegalArgumentException if name is null, empty, or invalid length
     */
    public void setName(String name) {
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("Category name cannot be empty");
        }
        String trimmedName = name.trim();
        if (trimmedName.length() < 2 || trimmedName.length() > 100) {
            throw new IllegalArgumentException("Category name must be between 2 and 100 characters");
        }
        this.name = trimmedName;
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
     * Activates the category.
     */
    public void activate() {
        this.isActive = true;
    }

    /**
     * Deactivates the category (soft delete).
     * This will be automatically called when JPA delete() is used due to @SQLDelete annotation.
     */
    public void deactivate() {
        this.isActive = false;
    }

    // Getters

    public String getName() {
        return name;
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
        if (!(o instanceof Category)) return false;
        if (!super.equals(o)) return false;
        Category category = (Category) o;
        return Objects.equals(name, category.name) &&
               Objects.equals(getTenantId(), category.getTenantId());
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), name, getTenantId());
    }

    @Override
    public String toString() {
        return String.format("Category[id=%d, tenantId=%d, name='%s', isActive=%s]",
            getId(), getTenantId(), name, isActive);
    }
}
