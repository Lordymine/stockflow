package com.stockflow.modules.catalog.infrastructure.web;

import com.stockflow.modules.catalog.application.dto.CategoryRequest;
import com.stockflow.modules.catalog.application.dto.CategoryResponse;
import com.stockflow.modules.catalog.application.service.CategoryService;
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
 * REST controller for category operations.
 *
 * <p>Provides endpoints for managing product categories in the catalog.
 * All operations are scoped to the current tenant.</p>
 */
@RestController
@RequestMapping("/api/catalog/categories")
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
    public ResponseEntity<CategoryResponse> create(@RequestBody CategoryRequest request) {
        CategoryResponse response = categoryService.create(request);
        return ResponseEntity.ok(response);
    }

    /**
     * Finds all categories with pagination.
     *
     * @param pageable pagination parameters
     * @return page of categories
     */
    @GetMapping
    @Operation(summary = "Find all categories", description = "Retrieves all categories for the current tenant with pagination")
    public ResponseEntity<Page<CategoryResponse>> findAll(
            @Parameter(description = "Pagination parameters")
            @PageableDefault(size = 20) Pageable pageable) {
        Page<CategoryResponse> response = categoryService.findAll(pageable);
        return ResponseEntity.ok(response);
    }

    /**
     * Finds a category by ID.
     *
     * @param id the category ID
     * @return the category
     */
    @GetMapping("/{id}")
    @Operation(summary = "Find category by ID", description = "Retrieves a specific category by ID")
    public ResponseEntity<CategoryResponse> findById(
            @Parameter(description = "Category ID", required = true)
            @PathVariable Long id) {
        CategoryResponse response = categoryService.findById(id);
        return ResponseEntity.ok(response);
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
    public ResponseEntity<CategoryResponse> update(
            @Parameter(description = "Category ID", required = true)
            @PathVariable Long id,
            @RequestBody CategoryRequest request) {
        CategoryResponse response = categoryService.update(id, request);
        return ResponseEntity.ok(response);
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
    public ResponseEntity<Void> delete(
            @Parameter(description = "Category ID", required = true)
            @PathVariable Long id) {
        categoryService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
