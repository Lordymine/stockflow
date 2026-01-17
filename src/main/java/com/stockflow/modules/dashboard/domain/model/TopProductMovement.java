package com.stockflow.modules.dashboard.domain.model;

import java.util.Objects;

/**
 * Value object representing a product's movement statistics.
 *
 * <p>This immutable value object contains aggregated movement data for a product,
 * used to identify the most frequently moved products in the system.</p>
 *
 * <p><strong>Fields:</strong></p>
 * <ul>
 *   <li>productId: The product ID</li>
 *   <li>productName: The product name</li>
 *   <li>productSku: The product SKU</li>
 *   <li>movementCount: Total number of movements for this product</li>
 *   <li>totalQuantity: Total quantity moved (sum of all movement quantities)</li>
 * </ul>
 *
 * <p><strong>Invariants:</strong></p>
 * <ul>
 *   <li>Product ID must not be null</li>
 *   <li>Product name and SKU must not be blank</li>
 *   <li>Movement counts must be non-negative</li>
 * </ul>
 */
public class TopProductMovement {

    /**
     * The product ID.
     */
    private final Long productId;

    /**
     * The product name.
     */
    private final String productName;

    /**
     * The product SKU.
     */
    private final String productSku;

    /**
     * Total number of movements for this product.
     */
    private final Integer movementCount;

    /**
     * Total quantity moved (sum of all movement quantities).
     */
    private final Integer totalQuantity;

    /**
     * Default constructor for serialization frameworks.
     */
    protected TopProductMovement() {
        this.productId = null;
        this.productName = null;
        this.productSku = null;
        this.movementCount = 0;
        this.totalQuantity = 0;
    }

    /**
     * Constructor for creating top product movement data.
     *
     * @param productId     the product ID
     * @param productName   the product name
     * @param productSku    the product SKU
     * @param movementCount the total number of movements
     * @param totalQuantity the total quantity moved
     * @throws IllegalArgumentException if validation fails
     */
    public TopProductMovement(Long productId, String productName, String productSku,
                             Integer movementCount, Integer totalQuantity) {
        if (productId == null) {
            throw new IllegalArgumentException("Product ID cannot be null");
        }
        if (productName == null || productName.trim().isEmpty()) {
            throw new IllegalArgumentException("Product name cannot be blank");
        }
        if (productSku == null || productSku.trim().isEmpty()) {
            throw new IllegalArgumentException("Product SKU cannot be blank");
        }
        if (movementCount == null || movementCount < 0) {
            throw new IllegalArgumentException("Movement count cannot be negative");
        }
        if (totalQuantity == null || totalQuantity < 0) {
            throw new IllegalArgumentException("Total quantity cannot be negative");
        }

        this.productId = productId;
        this.productName = productName.trim();
        this.productSku = productSku.trim();
        this.movementCount = movementCount;
        this.totalQuantity = totalQuantity;
    }

    // Getters

    public Long getProductId() {
        return productId;
    }

    public String getProductName() {
        return productName;
    }

    public String getProductSku() {
        return productSku;
    }

    public Integer getMovementCount() {
        return movementCount;
    }

    public Integer getTotalQuantity() {
        return totalQuantity;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof TopProductMovement)) return false;
        TopProductMovement that = (TopProductMovement) o;
        return Objects.equals(productId, that.productId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(productId);
    }

    @Override
    public String toString() {
        return String.format("TopProductMovement[productId=%d, productName='%s', productSku='%s', " +
                            "movementCount=%d, totalQuantity=%d]",
                productId, productName, productSku, movementCount, totalQuantity);
    }
}
