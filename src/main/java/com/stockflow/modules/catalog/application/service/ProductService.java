package com.stockflow.modules.catalog.application.service;

import com.stockflow.modules.catalog.application.dto.ProductRequest;
import com.stockflow.modules.catalog.application.dto.ProductResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

/**
 * Service interface for product operations.
 *
 * <p>Defines business logic operations for managing products in the catalog.</p>
 */
public interface ProductService {

    /**
     * Creates a new product.
     *
     * @param request the product request data
     * @return the created product response
     */
    ProductResponse create(ProductRequest request);

    /**
     * Updates an existing product.
     *
     * @param id      the product ID
     * @param request the product request data
     * @return the updated product response
     */
    ProductResponse update(Long id, ProductRequest request);

    /**
     * Deletes a product (soft delete).
     *
     * @param id the product ID
     */
    void delete(Long id);

    /**
     * Finds all products for the current tenant with pagination.
     *
     * @param pageable pagination parameters
     * @return page of products
     */
    Page<ProductResponse> findAll(Pageable pageable);

    /**
     * Finds a product by ID.
     *
     * @param id the product ID
     * @return the product response
     */
    ProductResponse findById(Long id);

    /**
     * Finds all products in a specific category with pagination.
     *
     * @param categoryId the category ID
     * @param pageable   pagination parameters
     * @return page of products in the category
     */
    Page<ProductResponse> findByCategory(Long categoryId, Pageable pageable);

    /**
     * Searches products with multiple filters.
     *
     * @param search     optional search term (searches in name, sku, description, barcode)
     * @param categoryId optional category filter
     * @param minPrice   optional minimum sale price filter
     * @param maxPrice   optional maximum sale price filter
     * @param isActive   optional active status filter
     * @param sortBy     optional sort field (name, salePrice, createdAt, sku)
     * @param sortOrder  optional sort order (ASC, DESC)
     * @param page       page number (default 0)
     * @param size       page size (default 20)
     * @return page of matching products
     */
    Page<ProductResponse> search(
            String search,
            Long categoryId,
            java.math.BigDecimal minPrice,
            java.math.BigDecimal maxPrice,
            Boolean isActive,
            String sortBy,
            String sortOrder,
            int page,
            int size
    );

    /**
     * Updates the active status of a product.
     *
     * @param productId the product ID
     * @param isActive the new active status
     * @return the updated product response
     */
    ProductResponse updateActive(Long productId, Boolean isActive);
}
