package com.stockflow.modules.catalog.application.service;

import com.stockflow.modules.catalog.application.dto.CategoryRequest;
import com.stockflow.modules.catalog.application.dto.CategoryResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

/**
 * Service interface for category operations.
 *
 * <p>Defines business logic operations for managing product categories.</p>
 */
public interface CategoryService {

    /**
     * Creates a new category.
     *
     * @param request the category request data
     * @return the created category response
     */
    CategoryResponse create(CategoryRequest request);

    /**
     * Updates an existing category.
     *
     * @param id      the category ID
     * @param request the category request data
     * @return the updated category response
     */
    CategoryResponse update(Long id, CategoryRequest request);

    /**
     * Deletes a category (soft delete).
     *
     * @param id the category ID
     */
    void delete(Long id);

    /**
     * Finds all categories for the current tenant with pagination.
     *
     * @param pageable pagination parameters
     * @return page of categories
     */
    Page<CategoryResponse> findAll(Pageable pageable);

    /**
     * Finds a category by ID.
     *
     * @param id the category ID
     * @return the category response
     */
    CategoryResponse findById(Long id);
}
