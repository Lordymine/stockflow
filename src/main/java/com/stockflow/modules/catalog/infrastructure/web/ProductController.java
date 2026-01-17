package com.stockflow.modules.catalog.infrastructure.web;

import com.stockflow.modules.catalog.application.dto.ProductRequest;
import com.stockflow.modules.catalog.application.dto.ProductResponse;
import com.stockflow.modules.catalog.application.service.ProductService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

/**
 * REST controller for product operations.
 *
 * <p>Provides endpoints for managing products in the catalog.
 * All operations are scoped to the current tenant.</p>
 */
@RestController
@RequestMapping("/api/catalog/products")
@Tag(name = "Products", description = "Product management endpoints")
@SecurityRequirement(name = "bearerAuth")
public class ProductController {

    private final ProductService productService;

    public ProductController(ProductService productService) {
        this.productService = productService;
    }

    /**
     * Creates a new product.
     *
     * @param request the product request data
     * @return the created product
     */
    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    @Operation(summary = "Create product", description = "Creates a new product. Requires ADMIN or MANAGER role.")
    public ResponseEntity<ProductResponse> create(@RequestBody ProductRequest request) {
        ProductResponse response = productService.create(request);
        return ResponseEntity.ok(response);
    }

    /**
     * Finds all products with pagination.
     *
     * @param pageable pagination parameters
     * @return page of products
     */
    @GetMapping
    @Operation(summary = "Find all products", description = "Retrieves all products for the current tenant with pagination")
    public ResponseEntity<Page<ProductResponse>> findAll(
            @Parameter(description = "Pagination parameters")
            @PageableDefault(size = 20) Pageable pageable) {
        Page<ProductResponse> response = productService.findAll(pageable);
        return ResponseEntity.ok(response);
    }

    /**
     * Finds a product by ID.
     *
     * @param id the product ID
     * @return the product
     */
    @GetMapping("/{id}")
    @Operation(summary = "Find product by ID", description = "Retrieves a specific product by ID")
    public ResponseEntity<ProductResponse> findById(
            @Parameter(description = "Product ID", required = true)
            @PathVariable Long id) {
        ProductResponse response = productService.findById(id);
        return ResponseEntity.ok(response);
    }

    /**
     * Finds all products in a specific category with pagination.
     *
     * @param categoryId the category ID
     * @param pageable   pagination parameters
     * @return page of products in the category
     */
    @GetMapping("/category/{categoryId}")
    @Operation(summary = "Find products by category", description = "Retrieves all products in a specific category with pagination")
    public ResponseEntity<Page<ProductResponse>> findByCategory(
            @Parameter(description = "Category ID", required = true)
            @PathVariable Long categoryId,
            @Parameter(description = "Pagination parameters")
            @PageableDefault(size = 20) Pageable pageable) {
        Page<ProductResponse> response = productService.findByCategory(categoryId, pageable);
        return ResponseEntity.ok(response);
    }

    /**
     * Updates an existing product.
     *
     * @param id      the product ID
     * @param request the product request data
     * @return the updated product
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    @Operation(summary = "Update product", description = "Updates an existing product. Requires ADMIN or MANAGER role.")
    public ResponseEntity<ProductResponse> update(
            @Parameter(description = "Product ID", required = true)
            @PathVariable Long id,
            @RequestBody ProductRequest request) {
        ProductResponse response = productService.update(id, request);
        return ResponseEntity.ok(response);
    }

    /**
     * Deletes a product (soft delete).
     *
     * @param id the product ID
     * @return empty response
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    @Operation(summary = "Delete product", description = "Soft deletes a product. Requires ADMIN or MANAGER role.")
    public ResponseEntity<Void> delete(
            @Parameter(description = "Product ID", required = true)
            @PathVariable Long id) {
        productService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
