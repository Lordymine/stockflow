package com.stockflow.modules.tenant.domain.model;

import com.stockflow.shared.domain.exception.ValidationException;
import com.stockflow.shared.domain.model.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;

import java.util.Objects;

/**
 * Tenant entity representing a company/organization in the multi-tenant system.
 *
 * <p>Each tenant is completely isolated from others, with all data scoped by tenant_id.</p>
 *
 * <p>Invariants:</p>
 * <ul>
 *   <li>Name must be between 2 and 255 characters</li>
 *   <li>Slug must be unique, lowercase, alphanumeric with hyphens</li>
 *   <li>Slug must be between 2 and 100 characters</li>
 * </ul>
 */
@Entity
@Table(name = "tenants")
public class Tenant extends BaseEntity {

    /**
     * Automatically sets tenant_id to equal id before persisting.
     * For root entities like Tenant, tenant_id references itself.
     */
    @PrePersist
    protected void onCreate() {
        if (getTenantId() == null) {
            setTenantId(getId());
        }
    }

    @PreUpdate
    protected void onUpdate() {
        if (getTenantId() == null) {
            setTenantId(getId());
        }
    }

    /**
     * Human-readable name of the tenant/company.
     */
    @Column(name = "name", nullable = false, length = 255)
    private String name;

    /**
     * URL-friendly identifier for the tenant.
     * Must be unique, lowercase, and contain only alphanumeric characters and hyphens.
     */
    @Column(name = "slug", nullable = false, unique = true, length = 100)
    private String slug;

    /**
     * Indicates whether the tenant is active.
     * Inactive tenants cannot authenticate or perform operations.
     */
    @Column(name = "is_active", nullable = false)
    private Boolean isActive = true;

    /**
     * Default constructor for JPA.
     */
    protected Tenant() {
    }

    /**
     * Constructor for creating a new tenant.
     *
     * @param name the name of the tenant
     * @param slug the URL-friendly slug
     * @throws ValidationException if validation fails
     */
    public Tenant(String name, String slug) {
        setName(name);
        setSlug(slug);
        this.isActive = true;
    }

    /**
     * Validates and sets the tenant name.
     *
     * @param name the name to set
     * @throws ValidationException if name is invalid
     */
    public void setName(String name) {
        if (name == null || name.trim().isEmpty()) {
            throw new ValidationException("VALIDATION_ERROR", "Tenant name cannot be empty");
        }
        if (name.length() < 2 || name.length() > 255) {
            throw new ValidationException("VALIDATION_ERROR",
                "Tenant name must be between 2 and 255 characters");
        }
        this.name = name.trim();
    }

    /**
     * Validates and sets the tenant slug.
     *
     * @param slug the slug to set
     * @throws ValidationException if slug is invalid
     */
    public void setSlug(String slug) {
        if (slug == null || slug.trim().isEmpty()) {
            throw new ValidationException("VALIDATION_ERROR", "Tenant slug cannot be empty");
        }
        String trimmedSlug = slug.trim().toLowerCase();
        if (trimmedSlug.length() < 2 || trimmedSlug.length() > 100) {
            throw new ValidationException("VALIDATION_ERROR",
                "Tenant slug must be between 2 and 100 characters");
        }
        if (!trimmedSlug.matches("^[a-z0-9]+(?:-[a-z0-9]+)*$")) {
            throw new ValidationException("VALIDATION_ERROR",
                "Tenant slug must contain only lowercase letters, numbers, and hyphens");
        }
        this.slug = trimmedSlug;
    }

    /**
     * Sets the active status of the tenant.
     *
     * @param active the active status
     */
    public void setActive(Boolean active) {
        this.isActive = active != null ? active : false;
    }

    /**
     * Activates the tenant.
     */
    public void activate() {
        this.isActive = true;
    }

    /**
     * Deactivates the tenant.
     */
    public void deactivate() {
        this.isActive = false;
    }

    // Getters

    public String getName() {
        return name;
    }

    public String getSlug() {
        return slug;
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
        if (!(o instanceof Tenant)) return false;
        if (!super.equals(o)) return false;
        Tenant tenant = (Tenant) o;
        return Objects.equals(slug, tenant.slug);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), slug);
    }

    @Override
    public String toString() {
        return String.format("Tenant[id=%d, name='%s', slug='%s', isActive=%s]",
            getId(), name, slug, isActive);
    }
}
