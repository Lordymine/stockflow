package com.stockflow.modules.catalog.application.dto;

import com.stockflow.modules.catalog.domain.model.Product;
import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Response DTO for product operations.
 *
 * <p>Contains product data returned from the API.</p>
 */
@Schema(description = "Product response payload")
public record ProductResponse(

    @Schema(description = "Product ID", example = "1")
    Long id,

    @Schema(description = "Tenant ID", example = "1")
    Long tenantId,

    @Schema(description = "Product name", example = "Wireless Gaming Mouse")
    String name,

    @Schema(description = "Stock Keeping Unit", example = "MOUSE-WL-001")
    String sku,

    @Schema(description = "Product description", example = "High-precision wireless gaming mouse with RGB lighting")
    String description,

    @Schema(description = "Product barcode", example = "7891234567890")
    String barcode,

    @Schema(description = "Unit of measure", example = "UN")
    String unitOfMeasure,

    @Schema(description = "Product image URL", example = "https://example.com/images/mouse.jpg")
    String imageUrl,

    @Schema(description = "Cost price", example = "45.50")
    BigDecimal costPrice,

    @Schema(description = "Sale price", example = "89.90")
    BigDecimal salePrice,

    @Schema(description = "Minimum stock level", example = "10")
    Integer minStock,

    @Schema(description = "Category ID", example = "1")
    Long categoryId,

    @Schema(description = "Creation timestamp", example = "2024-01-01T10:00:00")
    LocalDateTime createdAt,

    @Schema(description = "Last update timestamp", example = "2024-01-01T10:00:00")
    LocalDateTime updatedAt,

    @Schema(description = "Entity version for optimistic locking", example = "0")
    Long version,

    @Schema(description = "Indicates if the product is active", example = "true")
    Boolean isActive
) {
}
