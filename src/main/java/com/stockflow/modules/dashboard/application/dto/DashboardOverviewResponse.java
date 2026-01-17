package com.stockflow.modules.dashboard.application.dto;

import com.stockflow.modules.dashboard.domain.model.DashboardMetrics;
import com.stockflow.modules.dashboard.domain.model.TopProductMovement;
import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

/**
 * Response DTO for dashboard overview endpoint.
 *
 * <p>Contains all dashboard metrics and top products in a single response.
 * This is the main dashboard view that provides operational insights.</p>
 *
 * <p><strong>Cache Strategy:</strong></p>
 * <ul>
 *   <li>This entire response should be cached with TTL of 5 minutes</li>
 *   <li>Cache key should include tenant and optionally branch</li>
 *   <li>Invalidated on any stock movement or transfer</li>
 * </ul>
 *
 * @param metrics     dashboard metrics (products count, low stock, movements)
 * @param topProducts list of most moved products
 */
@Schema(description = "Dashboard overview containing metrics and top products")
public record DashboardOverviewResponse(

        @Schema(description = "Dashboard metrics including product counts and movement statistics")
        DashboardMetricsDTO metrics,

        @Schema(description = "List of most frequently moved products")
        List<TopProductDTO> topProducts
) {

    /**
     * DTO for dashboard metrics.
     *
     * @param totalActiveProducts total number of active products
     * @param lowStockItems      number of products with stock below minimum
     * @param totalMovements     total number of stock movements (all time)
     * @param recentMovements    number of movements in the last 7 days
     */
    @Schema(description = "Dashboard metrics")
    public record DashboardMetricsDTO(

            @Schema(description = "Total number of active products", example = "150")
            Integer totalActiveProducts,

            @Schema(description = "Number of products with stock below minimum", example = "12")
            Integer lowStockItems,

            @Schema(description = "Total number of stock movements", example = "5430")
            Integer totalMovements,

            @Schema(description = "Number of movements in the last 7 days", example = "230")
            Integer recentMovements
    ) {

        /**
         * Creates a DTO from domain model.
         *
         * @param metrics the domain metrics
         * @return DTO representation
         */
        public static DashboardMetricsDTO fromDomain(DashboardMetrics metrics) {
            return new DashboardMetricsDTO(
                    metrics.getTotalActiveProducts(),
                    metrics.getLowStockItems(),
                    metrics.getTotalMovements(),
                    metrics.getRecentMovements()
            );
        }
    }

    /**
     * DTO for top product movement data.
     *
     * @param productId     the product ID
     * @param productName   the product name
     * @param productSku    the product SKU
     * @param movementCount total number of movements
     * @param totalQuantity total quantity moved
     */
    @Schema(description = "Top product movement statistics")
    public record TopProductDTO(

            @Schema(description = "Product ID", example = "1")
            Long productId,

            @Schema(description = "Product name", example = "Wireless Mouse")
            String productName,

            @Schema(description = "Product SKU", example = "WM-001")
            String productSku,

            @Schema(description = "Total number of movements", example = "45")
            Integer movementCount,

            @Schema(description = "Total quantity moved", example = "320")
            Integer totalQuantity
    ) {

        /**
         * Creates a DTO from domain model.
         *
         * @param topProduct the domain top product movement
         * @return DTO representation
         */
        public static TopProductDTO fromDomain(TopProductMovement topProduct) {
            return new TopProductDTO(
                    topProduct.getProductId(),
                    topProduct.getProductName(),
                    topProduct.getProductSku(),
                    topProduct.getMovementCount(),
                    topProduct.getTotalQuantity()
            );
        }
    }

    /**
     * Creates a complete dashboard overview response from domain models.
     *
     * @param metrics     the dashboard metrics
     * @param topProducts list of top product movements
     * @return complete overview response
     */
    public static DashboardOverviewResponse fromDomain(DashboardMetrics metrics, List<TopProductMovement> topProducts) {
        return new DashboardOverviewResponse(
                DashboardMetricsDTO.fromDomain(metrics),
                topProducts.stream()
                        .map(TopProductDTO::fromDomain)
                        .toList()
        );
    }
}
