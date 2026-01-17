package com.stockflow.modules.catalog.application.service;

import com.stockflow.modules.catalog.application.dto.CategoryRequest;
import com.stockflow.modules.catalog.application.dto.CategoryResponse;
import com.stockflow.modules.catalog.application.mapper.CategoryMapper;
import com.stockflow.modules.catalog.domain.model.Category;
import com.stockflow.modules.catalog.domain.repository.CategoryRepository;
import com.stockflow.shared.domain.exception.ConflictException;
import com.stockflow.shared.domain.exception.NotFoundException;
import com.stockflow.shared.domain.exception.ValidationException;
import com.stockflow.shared.infrastructure.security.TenantContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Implementation of category service.
 *
 * <p>Handles business logic for category management including creation, updates,
 * deletion, and querying. All operations are scoped to the current tenant.</p>
 */
@Service
public class CategoryServiceImpl implements CategoryService {

    private static final Logger logger = LoggerFactory.getLogger(CategoryServiceImpl.class);

    private final CategoryRepository categoryRepository;
    private final CategoryMapper categoryMapper;

    public CategoryServiceImpl(CategoryRepository categoryRepository,
                               CategoryMapper categoryMapper) {
        this.categoryRepository = categoryRepository;
        this.categoryMapper = categoryMapper;
    }

    @Override
    @Transactional
    public CategoryResponse create(CategoryRequest request) {
        logger.info("Creating category with name: {}", request.name());

        Long tenantId = TenantContext.getTenantId();

        // Check if category with same name already exists in tenant
        if (categoryRepository.existsByNameAndTenantId(request.name(), tenantId)) {
            throw new ConflictException("CATEGORY_NAME_ALREADY_EXISTS",
                "A category with this name already exists in your tenant");
        }

        // Create and save category
        Category category = categoryMapper.toEntity(request, tenantId);
        Category savedCategory = categoryRepository.save(category);

        logger.info("Category created successfully with ID: {}", savedCategory.getId());

        return categoryMapper.toResponse(savedCategory);
    }

    @Override
    @Transactional
    public CategoryResponse update(Long id, CategoryRequest request) {
        logger.info("Updating category with ID: {}", id);

        Long tenantId = TenantContext.getTenantId();

        // Find category ensuring it belongs to tenant
        Category category = categoryRepository.findByIdAndTenantId(id, tenantId)
            .orElseThrow(() -> new NotFoundException("CATEGORY_NOT_FOUND",
                "Category not found with ID: " + id));

        // Check if another category with same name exists
        categoryRepository.findByNameAndTenantId(request.name(), tenantId)
            .ifPresent(existing -> {
                if (!existing.getId().equals(id)) {
                    throw new ConflictException("CATEGORY_NAME_ALREADY_EXISTS",
                        "Another category with this name already exists");
                }
            });

        // Update category
        category.setName(request.name());
        Category updatedCategory = categoryRepository.save(category);

        logger.info("Category updated successfully: {}", updatedCategory.getId());

        return categoryMapper.toResponse(updatedCategory);
    }

    @Override
    @Transactional
    public void delete(Long id) {
        logger.info("Deleting category with ID: {}", id);

        Long tenantId = TenantContext.getTenantId();

        // Find category ensuring it belongs to tenant
        Category category = categoryRepository.findByIdAndTenantId(id, tenantId)
            .orElseThrow(() -> new NotFoundException("CATEGORY_NOT_FOUND",
                "Category not found with ID: " + id));

        // Soft delete by deactivating (handled by @SQLDelete)
        categoryRepository.delete(category);

        logger.info("Category deleted successfully: {}", id);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<CategoryResponse> findAll(Pageable pageable) {
        logger.debug("Fetching all categories for tenant");

        Long tenantId = TenantContext.getTenantId();
        Page<Category> categories = categoryRepository.findAllByTenantId(tenantId, pageable);

        logger.debug("Found {} categories", categories.getTotalElements());

        return categories.map(categoryMapper::toResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public CategoryResponse findById(Long id) {
        logger.debug("Finding category by ID: {}", id);

        Long tenantId = TenantContext.getTenantId();
        Category category = categoryRepository.findByIdAndTenantId(id, tenantId)
            .orElseThrow(() -> new NotFoundException("CATEGORY_NOT_FOUND",
                "Category not found with ID: " + id));

        return categoryMapper.toResponse(category);
    }
}
