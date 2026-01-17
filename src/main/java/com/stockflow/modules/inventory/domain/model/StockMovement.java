package com.stockflow.modules.inventory.domain.model;

import com.stockflow.shared.domain.model.BaseEntity;
import jakarta.persistence.*;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.hibernate.annotations.Immutable;

import java.util.Objects;

/**
 * Entity representing a stock movement transaction.
 *
 * <p>Stock movements are immutable records of all stock changes.
 * Once created, they cannot be modified or deleted to ensure audit trail integrity.</p>
 *
 * <p><strong>Immutability:</strong></p>
 * <ul>
 *   <li>Marked with @Immutable to prevent updates</li>
 *   <li>No setters (except for JPA constructor)</li>
 *   <li>Represents historical facts that should never change</li>
 * </ul>
 *
 * <p><strong>Movement Types:</strong></p>
 * <ul>
 *   <li><b>IN:</b> Stock entering the branch (PURCHASE, RETURN, TRANSFER_IN)</li>
 *   <li><b>OUT:</b> Stock leaving the branch (SALE, LOSS, TRANSFER_OUT)</li>
 *   <li><b>ADJUSTMENT:</b> Manual corrections (ADJUSTMENT_IN, ADJUSTMENT_OUT)</li>
 *   <li><b>TRANSFER:</b> Between branches (TRANSFER_OUT, TRANSFER_IN)</li>
 * </ul>
 *
 * <p>Invariants:</p>
 * <ul>
 *   <li>Quantity must be positive</li>
 *   <li>Once created, cannot be modified</li>
 *   <li>Always has a type and reason</li>
 *   <li>Links to branch, product, and optionally the user who created it</li>
 * </ul>
 */
@Entity
@Table(name = "stock_movements")
@Immutable
@Cache(usage = CacheConcurrencyStrategy.READ_ONLY)
public class StockMovement extends BaseEntity {

    /**
     * The branch ID where the movement occurred.
     */
    @Column(name = "branch_id", nullable = false)
    private Long branchId;

    /**
     * The product ID being moved.
     */
    @Column(name = "product_id", nullable = false)
    private Long productId;

    /**
     * Type of movement (IN, OUT, ADJUSTMENT, TRANSFER).
     */
    @Column(name = "type", nullable = false, length = 20)
    @Enumerated(EnumType.STRING)
    private MovementType type;

    /**
     * Reason for the movement.
     */
    @Column(name = "reason", nullable = false, length = 30)
    @Enumerated(EnumType.STRING)
    private MovementReason reason;

    /**
     * Quantity moved (always positive).
     * Direction is determined by the type field.
     */
    @Column(name = "quantity", nullable = false)
    private Integer quantity;

    /**
     * Optional note explaining the movement.
     */
    @Column(name = "note", columnDefinition = "TEXT")
    private String note;

    /**
     * ID of the user who created this movement.
     */
    @Column(name = "created_by_user_id")
    private Long createdByUserId;

    /**
     * Default constructor for JPA.
     */
    protected StockMovement() {
    }

    /**
     * Constructor for creating a new stock movement.
     *
     * @param tenantId       the tenant ID
     * @param branchId       the branch ID
     * @param productId      the product ID
     * @param type           the movement type
     * @param reason         the movement reason
     * @param quantity       the quantity moved (must be positive)
     * @param note           optional note
     * @param createdByUserId the user ID who created this movement
     */
    public StockMovement(Long tenantId, Long branchId, Long productId, MovementType type,
                        MovementReason reason, Integer quantity, String note, Long createdByUserId) {
        setTenantId(tenantId);
        setBranchId(branchId);
        setProductId(productId);
        setType(type);
        setReason(reason);
        setQuantity(quantity);
        setNote(note);
        setCreatedByUserId(createdByUserId);
    }

    // Private setters for constructor use (no public setters to ensure immutability)

    private void setBranchId(Long branchId) {
        if (branchId == null) {
            throw new IllegalArgumentException("Branch ID cannot be null");
        }
        this.branchId = branchId;
    }

    private void setProductId(Long productId) {
        if (productId == null) {
            throw new IllegalArgumentException("Product ID cannot be null");
        }
        this.productId = productId;
    }

    private void setType(MovementType type) {
        if (type == null) {
            throw new IllegalArgumentException("Movement type cannot be null");
        }
        this.type = type;
    }

    private void setReason(MovementReason reason) {
        if (reason == null) {
            throw new IllegalArgumentException("Movement reason cannot be null");
        }
        this.reason = reason;
    }

    private void setQuantity(Integer quantity) {
        if (quantity == null) {
            throw new IllegalArgumentException("Quantity cannot be null");
        }
        if (quantity <= 0) {
            throw new IllegalArgumentException("Quantity must be positive");
        }
        this.quantity = quantity;
    }

    private void setNote(String note) {
        this.note = note != null ? note.trim() : null;
    }

    private void setCreatedByUserId(Long createdByUserId) {
        this.createdByUserId = createdByUserId;
    }

    // Getters only (immutable entity)

    public Long getBranchId() {
        return branchId;
    }

    public Long getProductId() {
        return productId;
    }

    public MovementType getType() {
        return type;
    }

    public MovementReason getReason() {
        return reason;
    }

    public Integer getQuantity() {
        return quantity;
    }

    public String getNote() {
        return note;
    }

    public Long getCreatedByUserId() {
        return createdByUserId;
    }

    /**
     * Checks if this movement increases stock.
     *
     * @return true if type is IN or reason is ADJUSTMENT_IN/TRANSFER_IN
     */
    public boolean isStockIncrease() {
        return type == MovementType.IN ||
               reason == MovementReason.ADJUSTMENT_IN ||
               reason == MovementReason.TRANSFER_IN;
    }

    /**
     * Checks if this movement decreases stock.
     *
     * @return true if type is OUT or reason is ADJUSTMENT_OUT/TRANSFER_OUT
     */
    public boolean isStockDecrease() {
        return type == MovementType.OUT ||
               reason == MovementReason.ADJUSTMENT_OUT ||
               reason == MovementReason.TRANSFER_OUT;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof StockMovement)) return false;
        if (!super.equals(o)) return false;
        StockMovement that = (StockMovement) o;
        return Objects.equals(branchId, that.branchId) &&
               Objects.equals(productId, that.productId) &&
               type == that.type &&
               reason == that.reason &&
               Objects.equals(quantity, that.quantity);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), branchId, productId, type, reason, quantity);
    }

    @Override
    public String toString() {
        return String.format("StockMovement[id=%d, tenantId=%d, branchId=%d, productId=%d, type=%s, reason=%s, quantity=%d]",
            getId(), getTenantId(), branchId, productId, type, reason, quantity);
    }
}
