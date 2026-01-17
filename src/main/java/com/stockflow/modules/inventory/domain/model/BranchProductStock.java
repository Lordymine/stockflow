package com.stockflow.modules.inventory.domain.model;

import com.stockflow.shared.domain.model.BaseEntity;
import jakarta.persistence.*;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

import java.util.Objects;

/**
 * Entity representing the stock quantity of a product in a specific branch.
 *
 * <p>This entity tracks the current stock level for each product in each branch.
 * It uses optimistic locking via the version field to prevent concurrent modification conflicts.</p>
 *
 * <p><strong>Optimistic Locking:</strong></p>
 * <ul>
 *   <li>The version field is automatically incremented on each update</li>
 *   <li>Concurrent updates will throw ObjectOptimisticLockingFailureException</li>
 *   <li>Clients must retry the operation with the latest version</li>
 * </ul>
 *
 * <p>Invariants:</p>
 * <ul>
 *   <li>Each (tenant, branch, product) combination must be unique</li>
 *   <li>Quantity must be non-negative</li>
 *   <li>Version is automatically managed by JPA</li>
 * </ul>
 */
@Entity
@Table(name = "branch_product_stock",
    uniqueConstraints = {
        @UniqueConstraint(name = "uk_tenant_branch_product", columnNames = {"tenant_id", "branch_id", "product_id"})
    }
)
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
public class BranchProductStock extends BaseEntity {

    /**
     * The branch ID where this stock is located.
     */
    @Column(name = "branch_id", nullable = false)
    private Long branchId;

    /**
     * The product ID for which stock is being tracked.
     */
    @Column(name = "product_id", nullable = false)
    private Long productId;

    /**
     * Current quantity of the product in this branch.
     * Must always be non-negative.
     */
    @Column(name = "quantity", nullable = false)
    private Integer quantity = 0;

    /**
     * Default constructor for JPA.
     */
    protected BranchProductStock() {
    }

    /**
     * Constructor for creating a new stock entry.
     *
     * @param tenantId  the tenant ID
     * @param branchId  the branch ID
     * @param productId the product ID
     * @param quantity  the initial quantity (must be non-negative)
     */
    public BranchProductStock(Long tenantId, Long branchId, Long productId, Integer quantity) {
        setTenantId(tenantId);
        setBranchId(branchId);
        setProductId(productId);
        setQuantity(quantity);
    }

    /**
     * Constructor for creating a new stock entry with zero quantity.
     *
     * @param tenantId  the tenant ID
     * @param branchId  the branch ID
     * @param productId the product ID
     */
    public BranchProductStock(Long tenantId, Long branchId, Long productId) {
        this(tenantId, branchId, productId, 0);
    }

    // Setters with validation

    /**
     * Sets the branch ID.
     *
     * @param branchId the branch ID
     */
    public void setBranchId(Long branchId) {
        if (branchId == null) {
            throw new IllegalArgumentException("Branch ID cannot be null");
        }
        this.branchId = branchId;
    }

    /**
     * Sets the product ID.
     *
     * @param productId the product ID
     */
    public void setProductId(Long productId) {
        if (productId == null) {
            throw new IllegalArgumentException("Product ID cannot be null");
        }
        this.productId = productId;
    }

    /**
     * Sets the stock quantity.
     *
     * @param quantity the quantity (must be non-negative)
     * @throws IllegalArgumentException if quantity is negative
     */
    public void setQuantity(Integer quantity) {
        if (quantity == null) {
            throw new IllegalArgumentException("Quantity cannot be null");
        }
        if (quantity < 0) {
            throw new IllegalArgumentException("Quantity cannot be negative");
        }
        this.quantity = quantity;
    }

    /**
     * Adds to the current stock quantity.
     *
     * @param amount the amount to add (must be positive)
     * @throws IllegalArgumentException if amount is not positive
     */
    public void addQuantity(int amount) {
        if (amount <= 0) {
            throw new IllegalArgumentException("Amount to add must be positive");
        }
        this.quantity += amount;
    }

    /**
     * Subtracts from the current stock quantity.
     *
     * @param amount the amount to subtract (must be positive)
     * @throws IllegalArgumentException if amount is not positive or would result in negative stock
     */
    public void subtractQuantity(int amount) {
        if (amount <= 0) {
            throw new IllegalArgumentException("Amount to subtract must be positive");
        }
        if (this.quantity < amount) {
            throw new IllegalArgumentException(
                String.format("Cannot subtract %d from stock with quantity %d", amount, this.quantity)
            );
        }
        this.quantity -= amount;
    }

    /**
     * Checks if the stock is at or below the minimum threshold.
     *
     * @param minStock the minimum stock threshold
     * @return true if stock is at or below minimum
     */
    public boolean isBelowMinimum(Integer minStock) {
        return minStock != null && this.quantity <= minStock;
    }

    // Getters

    public Long getBranchId() {
        return branchId;
    }

    public Long getProductId() {
        return productId;
    }

    public Integer getQuantity() {
        return quantity;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof BranchProductStock)) return false;
        if (!super.equals(o)) return false;
        BranchProductStock that = (BranchProductStock) o;
        return Objects.equals(branchId, that.branchId) &&
               Objects.equals(productId, that.productId) &&
               Objects.equals(getTenantId(), that.getTenantId());
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), branchId, productId, getTenantId());
    }

    @Override
    public String toString() {
        return String.format("BranchProductStock[id=%d, tenantId=%d, branchId=%d, productId=%d, quantity=%d, version=%d]",
            getId(), getTenantId(), branchId, productId, quantity, getVersion());
    }
}
