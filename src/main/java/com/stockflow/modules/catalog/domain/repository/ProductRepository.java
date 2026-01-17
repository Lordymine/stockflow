package com.stockflow.modules.catalog.domain.repository;

import com.stockflow.modules.catalog.domain.model.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Repository interface for Product entity.
 *
 * <p>Provides data access operations for products with tenant isolation.
 * All queries ensure proper tenant scoping to maintain multi-tenancy data isolation.</p>
 */
@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {

    /**
     * Finds a product by ID ensuring it belongs to the tenant.
     *
     * @param id       the product ID
     * @param tenantId the tenant ID
     * @return Optional containing the product if found
     */
    @Query("SELECT p FROM Product p WHERE p.id = :id AND p.tenantId = :tenantId AND p.isActive = true")
    Optional<Product> findByIdAndTenantId(@Param("id") Long id, @Param("tenantId") Long tenantId);

    /**
     * Finds all active products for a tenant with pagination.
     *
     * @param tenantId the tenant ID
     * @param pageable pagination parameters
     * @return page of products
     */
    @Query("SELECT p FROM Product p WHERE p.tenantId = :tenantId AND p.isActive = true")
    Page<Product> findAllByTenantId(@Param("tenantId") Long tenantId, Pageable pageable);

    /**
     * Finds a product by SKU within a specific tenant.
     *
     * @param sku      the product SKU
     * @param tenantId the tenant ID
     * @return Optional containing the product if found
     */
    @Query("SELECT p FROM Product p WHERE p.sku = :sku AND p.tenantId = :tenantId AND p.isActive = true")
    Optional<Product> findBySkuAndTenantId(@Param("sku") String sku, @Param("tenantId") Long tenantId);

    /**
     * Checks if a product with the given SKU exists in the tenant.
     *
     * @param sku      the SKU to check
     * @param tenantId the tenant ID
     * @return true if a product with the SKU exists
     */
    @Query("SELECT COUNT(p) > 0 FROM Product p WHERE p.sku = :sku AND p.tenantId = :tenantId")
    boolean existsBySkuAndTenantId(@Param("sku") String sku, @Param("tenantId") Long tenantId);

    /**
     * Finds all products in a specific category for a tenant with pagination.
     *
     * @param categoryId the category ID
     * @param tenantId   the tenant ID
     * @param pageable   pagination parameters
     * @return page of products in the category
     */
    @Query("SELECT p FROM Product p WHERE p.categoryId = :categoryId AND p.tenantId = :tenantId AND p.isActive = true")
    Page<Product> findByCategoryIdAndTenantId(@Param("categoryId") Long categoryId,
                                               @Param("tenantId") Long tenantId,
                                               Pageable pageable);

    /**
     * Counts active products in a tenant.
     *
     * @param tenantId the tenant ID
     * @return count of active products
     */
    @Query("SELECT COUNT(p) FROM Product p WHERE p.tenantId = :tenantId AND p.isActive = true")
    long countActiveByTenantId(@Param("tenantId") Long tenantId);

    /**
     * Counts active products in a specific category for a tenant.
     *
     * @param categoryId the category ID
     * @param tenantId   the tenant ID
     * @return count of active products in the category
     */
    @Query("SELECT COUNT(p) FROM Product p WHERE p.categoryId = :categoryId AND p.tenantId = :tenantId AND p.isActive = true")
    long countActiveByCategoryIdAndTenantId(@Param("categoryId") Long categoryId,
                                             @Param("tenantId") Long tenantId);

    /**
     * Searches products with multiple filters.
     *
     * @param search     optional search term (searches in name, sku, description, barcode)
     * @param categoryId optional category filter
     * @param minPrice   optional minimum sale price filter
     * @param maxPrice   optional maximum sale price filter
     * @param isActive   optional active status filter
     * @param tenantId   the tenant ID
     * @param pageable   pagination parameters
     * @return page of matching products
     */
    @Query("SELECT p FROM Product p WHERE p.tenantId = :tenantId AND " +
           "(:search IS NULL OR LOWER(p.name) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "LOWER(p.sku) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "LOWER(p.description) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "p.barcode LIKE CONCAT('%', :search, '%')) AND " +
           "(:categoryId IS NULL OR p.categoryId = :categoryId) AND " +
           "(:minPrice IS NULL OR p.salePrice >= :minPrice) AND " +
           "(:maxPrice IS NULL OR p.salePrice <= :maxPrice) AND " +
           "(:isActive IS NULL OR p.isActive = :isActive)")
    Page<Product> searchProducts(
            @Param("search") String search,
            @Param("categoryId") Long categoryId,
            @Param("minPrice") java.math.BigDecimal minPrice,
            @Param("maxPrice") java.math.BigDecimal maxPrice,
            @Param("isActive") Boolean isActive,
            @Param("tenantId") Long tenantId,
            Pageable pageable
    );
}
