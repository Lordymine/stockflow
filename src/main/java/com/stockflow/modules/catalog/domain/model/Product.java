package com.stockflow.modules.catalog.domain.model;

import com.stockflow.shared.domain.model.BaseEntity;
import jakarta.persistence.*;
import org.hibernate.annotations.SQLDelete;

import java.math.BigDecimal;
import java.util.Objects;

/**
 * Product entity for catalog management.
 *
 * <p>Products represent items in the inventory that can be stocked, sold, and tracked.
 * Each product belongs to a tenant and can optionally be categorized.</p>
 *
 * <p><strong>Soft Delete:</strong> Uses @SQLDelete for soft delete functionality.
 * When a product is "deleted", it's marked as inactive (is_active = false) instead of being removed
 * from the database. This maintains data integrity and allows for audit trails.</p>
 *
 * <p>Invariants:</p>
 * <ul>
 *   <li>Name must be between 2 and 255 characters</li>
 *   <li>SKU must be between 2 and 100 characters</li>
 *   <li>SKU must be unique within the tenant</li>
 *   <li>Cost price must be less than or equal to sale price</li>
 * </ul>
 */
@Entity
@Table(name = "products")
@SQLDelete(sql = "UPDATE products SET is_active = false WHERE id = ?")
public class Product extends BaseEntity {

    /**
     * Human-readable name of the product.
     */
    @Column(name = "name", nullable = false, length = 255)
    private String name;

    /**
     * Stock Keeping Unit - unique identifier within the tenant.
     */
    @Column(name = "sku", nullable = false, length = 100, unique = true)
    private String sku;

    /**
     * Detailed description of the product.
     */
    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    /**
     * Barcode for scanning (EAN, UPC, etc.).
     */
    @Column(name = "barcode", length = 50)
    private String barcode;

    /**
     * Unit of measure for inventory tracking.
     */
    @Column(name = "unit_of_measure", nullable = false, length = 3)
    @Enumerated(EnumType.STRING)
    private UnitOfMeasure unitOfMeasure = UnitOfMeasure.UN;

    /**
     * URL to the product image.
     */
    @Column(name = "image_url", length = 500)
    private String imageUrl;

    /**
     * Cost price of the product.
     */
    @Column(name = "cost_price", precision = 10, scale = 2)
    private BigDecimal costPrice;

    /**
     * Sale price of the product.
     */
    @Column(name = "sale_price", precision = 10, scale = 2)
    private BigDecimal salePrice;

    /**
     * Minimum stock level before reorder is needed.
     */
    @Column(name = "min_stock", nullable = false)
    private Integer minStock = 0;

    /**
     * Category ID (foreign key to categories table).
     */
    @Column(name = "category_id")
    private Long categoryId;

    /**
     * Indicates whether the product is active.
     * Inactive products are hidden from the catalog.
     */
    @Column(name = "is_active", nullable = false)
    private Boolean isActive = true;

    /**
     * Unit of measure enumeration.
     */
    public enum UnitOfMeasure {
        UN, // Unit
        KG, // Kilogram
        L,  // Liter
        M   // Meter
    }

    /**
     * Default constructor for JPA.
     */
    protected Product() {
    }

    /**
     * Constructor for creating a new product.
     *
     * @param tenantId      the tenant ID
     * @param name          the product name
     * @param sku           the stock keeping unit
     * @param unitOfMeasure the unit of measure
     */
    public Product(Long tenantId, String name, String sku, UnitOfMeasure unitOfMeasure) {
        setTenantId(tenantId);
        setName(name);
        setSku(sku);
        setUnitOfMeasure(unitOfMeasure);
        this.isActive = true;
        this.minStock = 0;
    }

    // Validation methods

    /**
     * Sets the product name with validation.
     *
     * @param name the product name
     * @throws IllegalArgumentException if name is null, empty, or invalid length
     */
    public void setName(String name) {
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("Product name cannot be empty");
        }
        String trimmedName = name.trim();
        if (trimmedName.length() < 2 || trimmedName.length() > 255) {
            throw new IllegalArgumentException("Product name must be between 2 and 255 characters");
        }
        this.name = trimmedName;
    }

    /**
     * Sets the SKU with validation.
     *
     * @param sku the stock keeping unit
     * @throws IllegalArgumentException if SKU is null, empty, or invalid length
     */
    public void setSku(String sku) {
        if (sku == null || sku.trim().isEmpty()) {
            throw new IllegalArgumentException("Product SKU cannot be empty");
        }
        String trimmedSku = sku.trim();
        if (trimmedSku.length() < 2 || trimmedSku.length() > 100) {
            throw new IllegalArgumentException("Product SKU must be between 2 and 100 characters");
        }
        this.sku = trimmedSku;
    }

    /**
     * Sets the description.
     *
     * @param description the product description
     */
    public void setDescription(String description) {
        this.description = description != null ? description.trim() : null;
    }

    /**
     * Sets the barcode.
     *
     * @param barcode the product barcode
     */
    public void setBarcode(String barcode) {
        this.barcode = barcode != null ? barcode.trim() : null;
    }

    /**
     * Sets the unit of measure.
     *
     * @param unitOfMeasure the unit of measure
     */
    public void setUnitOfMeasure(UnitOfMeasure unitOfMeasure) {
        this.unitOfMeasure = unitOfMeasure != null ? unitOfMeasure : UnitOfMeasure.UN;
    }

    /**
     * Sets the image URL.
     *
     * @param imageUrl the product image URL
     */
    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl != null ? imageUrl.trim() : null;
    }

    /**
     * Sets the cost price.
     *
     * @param costPrice the cost price
     * @throws IllegalArgumentException if cost price is negative
     */
    public void setCostPrice(BigDecimal costPrice) {
        if (costPrice != null && costPrice.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Cost price cannot be negative");
        }
        this.costPrice = costPrice;
    }

    /**
     * Sets the sale price.
     *
     * @param salePrice the sale price
     * @throws IllegalArgumentException if sale price is negative
     */
    public void setSalePrice(BigDecimal salePrice) {
        if (salePrice != null && salePrice.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Sale price cannot be negative");
        }
        this.salePrice = salePrice;
    }

    /**
     * Validates that cost price is not greater than sale price.
     *
     * @throws IllegalArgumentException if cost price exceeds sale price
     */
    public void validatePriceRelationship() {
        if (costPrice != null && salePrice != null) {
            if (costPrice.compareTo(salePrice) > 0) {
                throw new IllegalArgumentException("Cost price cannot be greater than sale price");
            }
        }
    }

    /**
     * Sets the minimum stock level.
     *
     * @param minStock the minimum stock level
     * @throws IllegalArgumentException if min stock is negative
     */
    public void setMinStock(Integer minStock) {
        if (minStock != null && minStock < 0) {
            throw new IllegalArgumentException("Minimum stock cannot be negative");
        }
        this.minStock = minStock != null ? minStock : 0;
    }

    /**
     * Sets the category ID.
     *
     * @param categoryId the category ID
     */
    public void setCategoryId(Long categoryId) {
        this.categoryId = categoryId;
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
     * Activates the product.
     */
    public void activate() {
        this.isActive = true;
    }

    /**
     * Deactivates the product (soft delete).
     * This will be automatically called when JPA delete() is used due to @SQLDelete annotation.
     */
    public void deactivate() {
        this.isActive = false;
    }

    // Getters

    public String getName() {
        return name;
    }

    public String getSku() {
        return sku;
    }

    public String getDescription() {
        return description;
    }

    public String getBarcode() {
        return barcode;
    }

    public UnitOfMeasure getUnitOfMeasure() {
        return unitOfMeasure;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public BigDecimal getCostPrice() {
        return costPrice;
    }

    public BigDecimal getSalePrice() {
        return salePrice;
    }

    public Integer getMinStock() {
        return minStock;
    }

    public Long getCategoryId() {
        return categoryId;
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
        if (!(o instanceof Product)) return false;
        if (!super.equals(o)) return false;
        Product product = (Product) o;
        return Objects.equals(sku, product.sku) &&
               Objects.equals(getTenantId(), product.getTenantId());
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), sku, getTenantId());
    }

    @Override
    public String toString() {
        return String.format("Product[id=%d, tenantId=%d, sku='%s', name='%s', isActive=%s]",
            getId(), getTenantId(), sku, name, isActive);
    }
}
