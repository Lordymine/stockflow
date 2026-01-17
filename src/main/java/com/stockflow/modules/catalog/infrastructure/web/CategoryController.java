package com.stockflow.modules.catalog.infrastructure.web;

import com.stockflow.modules.catalog.application.dto.CategoryRequest;
import com.stockflow.modules.catalog.application.dto.CategoryResponse;
import com.stockflow.modules.catalog.application.service.CategoryService;
import com.stockflow.shared.application.dto.ApiResponse;
import com.stockflow.shared.application.dto.ItemsResponse;
import com.stockflow.shared.application.dto.PageMeta;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

/**
 * REST controller for category operations.
 *
 * <p>Provides endpoints for managing product categories in the catalog.
 * All operations are scoped to the current tenant.</p>
 */
@RestController
@RequestMapping("/api/v1/categories")
@Tag(name = "Categories", description = "Category management endpoints")
@SecurityRequirement(name = "bearerAuth")
public class CategoryController {

    private final CategoryService categoryService;

    public CategoryController(CategoryService categoryService) {
        this.categoryService = categoryService;
    }

    /**
     * Creates a new category.
     *
     * @param request the category request data
     * @return the created category
     */
    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    @Operation(summary = "Create category", description = "Creates a new product category. Requires ADMIN or MANAGER role.")
    public ResponseEntity<ApiResponse<CategoryResponse>> create(@Valid @RequestBody CategoryRequest request) {
        CategoryResponse response = categoryService.create(request);
        return ResponseEntity.status(201).body(ApiResponse.of(response));
    }

    /**
     * Finds all categories with pagination.
     *
     * @param pageable pagination parameters
     * @return page of categories
     */
    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'STAFF')")
    @Operation(summary = "Find all categories", description = "Retrieves all categories for the current tenant with pagination")
    public ResponseEntity<ApiResponse<ItemsResponse<CategoryResponse>>> findAll(
            @Parameter(description = "Pagination parameters")
            @PageableDefault(size = 20) Pageable pageable) {
        Page<CategoryResponse> response = categoryService.findAll(pageable);
        return ResponseEntity.ok(
            ApiResponse.of(new ItemsResponse<>(response.getContent()), PageMeta.of(response))
        );
    }

    /**
     * Finds a category by ID.
     *
     * @param id the category ID
     * @return the category
     */
    @GetMapping("/{id}")
    @Operation(summary = "Find category by ID", description = "Retrieves a specific category by ID")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'STAFF')")
    public ResponseEntity<ApiResponse<CategoryResponse>> findById(
            @Parameter(description = "Category ID", required = true)
            @PathVariable Long id) {
        CategoryResponse response = categoryService.findById(id);
        return ResponseEntity.ok(ApiResponse.of(response));
    }

    /**
     * Updates an existing category.
     *
     * @param id      the category ID
     * @param request the category request data
     * @return the updated category
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    @Operation(summary = "Update category", description = "Updates an existing category. Requires ADMIN or MANAGER role.")
    public ResponseEntity<ApiResponse<CategoryResponse>> update(
            @Parameter(description = "Category ID", required = true)
            @PathVariable Long id,
            @Valid @RequestBody CategoryRequest request) {
        CategoryResponse response = categoryService.update(id, request);
        return ResponseEntity.ok(ApiResponse.of(response));
    }

    /**
     * Deletes a category (soft delete).
     *
     * @param id the category ID
     * @return empty response
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    @Operation(summary = "Delete category", description = "Soft deletes a category. Requires ADMIN or MANAGER role.")
    public ResponseEntity<ApiResponse<Void>> delete(
            @Parameter(description = "Category ID", required = true)
            @PathVariable Long id) {
        categoryService.delete(id);
        return ResponseEntity.ok(ApiResponse.empty());
    }
}
