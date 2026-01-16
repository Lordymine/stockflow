package com.stockflow.shared.domain.model;

import jakarta.persistence.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

/**
 * Base entity class for all domain entities.
 *
 * <p>Provides common fields and auditing capabilities:</p>
 * <ul>
 *   <li>Auto-generated ID</li>
 *   <li>Multi-tenancy support with tenantId</li>
 *   <li>Auditing fields (createdAt, updatedAt)</li>
 *   <li>Optimistic locking with version field</li>
 * </ul>
 *
 * <p>All domain entities should extend this class to inherit
 * standard behavior and maintain consistency across the system.</p>
 */
@MappedSuperclass
@EntityListeners(AuditingEntityListener.class)
public abstract class BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Tenant ID for multi-tenancy isolation.
     * All data is scoped by tenant_id to ensure complete separation between companies.
     */
    @Column(name = "tenant_id", nullable = false)
    private Long tenantId;

    /**
     * Timestamp when the entity was created.
     * Automatically set by JPA auditing.
     */
    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    /**
     * Timestamp when the entity was last updated.
     * Automatically updated by JPA auditing on each persist operation.
     */
    @LastModifiedDate
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    /**
     * Version field for optimistic locking.
     * Automatically incremented on each update to prevent concurrent modification conflicts.
     *
     * @see org.springframework.dao.OptimisticLockingFailureException
     */
    @Version
    @Column(name = "version", nullable = false)
    private Long version = 0L;

    // Getters and Setters

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getTenantId() {
        return tenantId;
    }

    public void setTenantId(Long tenantId) {
        this.tenantId = tenantId;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    public Long getVersion() {
        return version;
    }

    public void setVersion(Long version) {
        this.version = version;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof BaseEntity)) return false;
        BaseEntity that = (BaseEntity) o;
        return id != null && id.equals(that.id);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }

    @Override
    public String toString() {
        return String.format("%s[id=%d, tenantId=%d]", getClass().getSimpleName(), id, tenantId);
    }
}
