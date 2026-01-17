package com.stockflow.modules.catalog.application.dto;

import com.stockflow.modules.catalog.domain.model.Product;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

/**
 * Request DTO for product operations.
 *
 * <p>Contains the data required to create or update a product.</p>
 */
@Schema(description = "Product request payload")
public record ProductRequest(

    @Schema(description = "Product name", example = "Wireless Gaming Mouse", required = true)
    @NotBlank(message = "Product name is required")
    @Size(min = 2, max = 255, message = "Product name must be between 2 and 255 characters")
    String name,

    @Schema(description = "Stock Keeping Unit (unique within tenant)", example = "MOUSE-WL-001", required = true)
    @NotBlank(message = "Product SKU is required")
    @Size(min = 2, max = 100, message = "Product SKU must be between 2 and 100 characters")
    String sku,

    @Schema(description = "Product description", example = "High-precision wireless gaming mouse with RGB lighting")
    String description,

    @Schema(description = "Product barcode (EAN, UPC, etc.)", example = "7891234567890")
    String barcode,

    @Schema(description = "Unit of measure", example = "UN", required = true)
    @NotBlank(message = "Unit of measure is required")
    String unitOfMeasure,

    @Schema(description = "Product image URL", example = "https://example.com/images/mouse.jpg")
    String imageUrl,

    @Schema(description = "Cost price", example = "45.50")
    @PositiveOrZero(message = "Cost price must be positive or zero")
    BigDecimal costPrice,

    @Schema(description = "Sale price", example = "89.90")
    @PositiveOrZero(message = "Sale price must be positive or zero")
    BigDecimal salePrice,

    @Schema(description = "Minimum stock level for reorder", example = "10")
    @PositiveOrZero(message = "Minimum stock must be positive or zero")
    Integer minStock,

    @Schema(description = "Category ID", example = "1")
    Long categoryId
) {
    /**
     * Converts string unit of measure to enum.
     *
     * @return UnitOfMeasure enum value
     */
    public Product.UnitOfMeasure getUnitOfMeasureAsEnum() {
        if (unitOfMeasure == null || unitOfMeasure.trim().isEmpty()) {
            return Product.UnitOfMeasure.UN;
        }
        try {
            return Product.UnitOfMeasure.valueOf(unitOfMeasure.trim().toUpperCase());
        } catch (IllegalArgumentException e) {
            return Product.UnitOfMeasure.UN;
        }
    }
}
