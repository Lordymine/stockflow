package com.stockflow.modules.dashboard.domain.model;

import java.util.Objects;

/**
 * Value object representing dashboard metrics.
 *
 * <p>This immutable value object contains aggregated metrics for the dashboard,
 * including product counts, low stock alerts, and movement statistics.</p>
 *
 * <p><strong>Metrics:</strong></p>
 * <ul>
 *   <li>totalActiveProducts: Total number of active products in the tenant</li>
 *   <li>lowStockItems: Number of products with stock below minimum threshold</li>
 *   <li>totalMovements: Total number of stock movements (all time)</li>
 *   <li>recentMovements: Number of movements in the last 7 days</li>
 * </ul>
 *
 * <p><strong>Invariants:</strong></p>
 * <ul>
 *   <li>All counts must be non-negative</li>
 *   <li>Immutability ensures thread-safety</li>
 * </ul>
 */
public class DashboardMetrics {

    /**
     * Total number of active products for the tenant.
     */
    private final Integer totalActiveProducts;

    /**
     * Number of products with stock below minimum threshold.
     */
    private final Integer lowStockItems;

    /**
     * Total number of stock movements (all time).
     */
    private final Integer totalMovements;

    /**
     * Number of movements in the last 7 days.
     */
    private final Integer recentMovements;

    /**
     * Default constructor for serialization frameworks.
     */
    protected DashboardMetrics() {
        this.totalActiveProducts = 0;
        this.lowStockItems = 0;
        this.totalMovements = 0;
        this.recentMovements = 0;
    }

    /**
     * Constructor for creating dashboard metrics.
     *
     * @param totalActiveProducts total number of active products
     * @param lowStockItems      number of products with low stock
     * @param totalMovements     total number of movements
     * @param recentMovements    number of recent movements (7 days)
     * @throws IllegalArgumentException if any value is negative
     */
    public DashboardMetrics(Integer totalActiveProducts, Integer lowStockItems,
                           Integer totalMovements, Integer recentMovements) {
        validateNonNegative(totalActiveProducts, "Total active products");
        validateNonNegative(lowStockItems, "Low stock items");
        validateNonNegative(totalMovements, "Total movements");
        validateNonNegative(recentMovements, "Recent movements");

        this.totalActiveProducts = totalActiveProducts;
        this.lowStockItems = lowStockItems;
        this.totalMovements = totalMovements;
        this.recentMovements = recentMovements;
    }

    /**
     * Validates that a value is non-negative.
     *
     * @param value the value to validate
     * @param name  the name of the field (for error message)
     * @throws IllegalArgumentException if value is negative
     */
    private void validateNonNegative(Integer value, String name) {
        if (value == null || value < 0) {
            throw new IllegalArgumentException(name + " cannot be negative");
        }
    }

    // Getters

    public Integer getTotalActiveProducts() {
        return totalActiveProducts;
    }

    public Integer getLowStockItems() {
        return lowStockItems;
    }

    public Integer getTotalMovements() {
        return totalMovements;
    }

    public Integer getRecentMovements() {
        return recentMovements;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof DashboardMetrics)) return false;
        DashboardMetrics that = (DashboardMetrics) o;
        return Objects.equals(totalActiveProducts, that.totalActiveProducts) &&
               Objects.equals(lowStockItems, that.lowStockItems) &&
               Objects.equals(totalMovements, that.totalMovements) &&
               Objects.equals(recentMovements, that.recentMovements);
    }

    @Override
    public int hashCode() {
        return Objects.hash(totalActiveProducts, lowStockItems, totalMovements, recentMovements);
    }

    @Override
    public String toString() {
        return String.format("DashboardMetrics[totalActiveProducts=%d, lowStockItems=%d, " +
                            "totalMovements=%d, recentMovements=%d]",
                totalActiveProducts, lowStockItems, totalMovements, recentMovements);
    }
}
