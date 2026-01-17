package com.stockflow.modules.catalog.infrastructure.web;

import com.stockflow.modules.catalog.application.dto.ProductRequest;
import com.stockflow.modules.catalog.application.dto.ProductResponse;
import com.stockflow.modules.catalog.application.service.ProductService;
import com.stockflow.shared.application.dto.ActiveRequest;
import com.stockflow.shared.application.dto.ApiResponse;
import com.stockflow.shared.application.dto.ItemsResponse;
import com.stockflow.shared.application.dto.PageMeta;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;

/**
 * REST controller for product operations.
 */
@RestController
@RequestMapping("/api/v1/products")
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
    public ResponseEntity<ApiResponse<ProductResponse>> create(@Valid @RequestBody ProductRequest request) {
        ProductResponse response = productService.create(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.of(response));
    }

    /**
     * Lists products with optional search and filters.
     *
     * @param search     optional search term
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
    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'STAFF')")
    @Operation(summary = "List products", description = "Retrieves products with optional search and filters")
    public ResponseEntity<ApiResponse<ItemsResponse<ProductResponse>>> list(
            @Parameter(description = "Search term (name, sku, description, barcode)")
            @RequestParam(required = false) String search,
            @Parameter(description = "Filter by category ID")
            @RequestParam(required = false) Long categoryId,
            @Parameter(description = "Minimum sale price")
            @RequestParam(required = false) BigDecimal minPrice,
            @Parameter(description = "Maximum sale price")
            @RequestParam(required = false) BigDecimal maxPrice,
            @Parameter(description = "Filter by active status")
            @RequestParam(required = false) Boolean isActive,
            @Parameter(description = "Sort field (name, salePrice, createdAt, sku)")
            @RequestParam(required = false) String sortBy,
            @Parameter(description = "Sort order (ASC, DESC)")
            @RequestParam(required = false) String sortOrder,
            @Parameter(description = "Page number (default 0)")
            @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size (default 20)")
            @RequestParam(defaultValue = "20") int size) {
        Page<ProductResponse> response = productService.search(
            search, categoryId, minPrice, maxPrice, isActive, sortBy, sortOrder, page, size);
        return ResponseEntity.ok(
            ApiResponse.of(new ItemsResponse<>(response.getContent()), PageMeta.of(response))
        );
    }

    /**
     * Finds a product by ID.
     *
     * @param id the product ID
     * @return the product
     */
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'STAFF')")
    @Operation(summary = "Find product by ID", description = "Retrieves a specific product by ID")
    public ResponseEntity<ApiResponse<ProductResponse>> findById(
            @Parameter(description = "Product ID", required = true)
            @PathVariable Long id) {
        ProductResponse response = productService.findById(id);
        return ResponseEntity.ok(ApiResponse.of(response));
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
    public ResponseEntity<ApiResponse<ProductResponse>> update(
            @Parameter(description = "Product ID", required = true)
            @PathVariable Long id,
            @Valid @RequestBody ProductRequest request) {
        ProductResponse response = productService.update(id, request);
        return ResponseEntity.ok(ApiResponse.of(response));
    }

    /**
     * Toggles the active status of a product.
     *
     * @param id the product ID
     * @return the updated product
     */
    @PatchMapping("/{id}/active")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    @Operation(
        summary = "Update product active status",
        description = "Activates or deactivates a product. Requires ADMIN or MANAGER role."
    )
    public ResponseEntity<ApiResponse<ProductResponse>> updateActiveStatus(
            @Parameter(description = "Product ID", required = true)
            @PathVariable Long id,
            @Valid @RequestBody ActiveRequest request) {
        ProductResponse response = productService.updateActive(id, request.isActive());
        return ResponseEntity.ok(ApiResponse.of(response));
    }
}
