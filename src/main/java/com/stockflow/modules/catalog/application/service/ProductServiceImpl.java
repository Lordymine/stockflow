package com.stockflow.modules.catalog.application.service;

import com.stockflow.modules.catalog.application.dto.ProductRequest;
import com.stockflow.modules.catalog.application.dto.ProductResponse;
import com.stockflow.modules.catalog.application.mapper.ProductMapper;
import com.stockflow.modules.catalog.domain.model.Product;
import com.stockflow.modules.catalog.domain.repository.CategoryRepository;
import com.stockflow.modules.catalog.domain.repository.ProductRepository;
import com.stockflow.shared.domain.exception.ConflictException;
import com.stockflow.shared.domain.exception.NotFoundException;
import com.stockflow.shared.domain.exception.ValidationException;
import com.stockflow.shared.infrastructure.security.TenantContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Set;

/**
 * Implementation of product service.
 *
 * <p>Handles business logic for product management including creation, updates,
 * deletion, and querying. All operations are scoped to the current tenant.</p>
 */
@Service
public class ProductServiceImpl implements ProductService {

    private static final Logger logger = LoggerFactory.getLogger(ProductServiceImpl.class);

    private static final Set<String> VALID_SORT_FIELDS = Set.of("name", "salePrice", "createdAt", "sku");
    private static final Set<String> VALID_SORT_ORDERS = Set.of("ASC", "DESC");

    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;
    private final ProductMapper productMapper;

    public ProductServiceImpl(ProductRepository productRepository,
                              CategoryRepository categoryRepository,
                              ProductMapper productMapper) {
        this.productRepository = productRepository;
        this.categoryRepository = categoryRepository;
        this.productMapper = productMapper;
    }

    @Override
    @Transactional
    public ProductResponse create(ProductRequest request) {
        logger.info("Creating product with SKU: {}", request.sku());

        Long tenantId = TenantContext.getTenantId();

        // Check if product with same SKU already exists in tenant
        if (productRepository.existsBySkuAndTenantId(request.sku(), tenantId)) {
            throw new ConflictException("PRODUCT_SKU_ALREADY_EXISTS",
                "A product with this SKU already exists in your tenant");
        }

        // Validate category if provided
        validateCategoryIfExists(request.categoryId(), tenantId);

        // Validate price relationship
        validatePriceRelationship(request.costPrice(), request.salePrice());

        // Create and save product
        Product product = productMapper.toEntity(request, tenantId);
        Product savedProduct = productRepository.save(product);

        logger.info("Product created successfully with ID: {}", savedProduct.getId());

        return productMapper.toResponse(savedProduct);
    }

    @Override
    @Transactional
    public ProductResponse update(Long id, ProductRequest request) {
        logger.info("Updating product with ID: {}", id);

        Long tenantId = TenantContext.getTenantId();

        // Find product ensuring it belongs to tenant
        Product product = productRepository.findByIdAndTenantId(id, tenantId)
            .orElseThrow(() -> new NotFoundException("PRODUCT_NOT_FOUND",
                "Product not found with ID: " + id));

        // Check if another product with same SKU exists
        productRepository.findBySkuAndTenantId(request.sku(), tenantId)
            .ifPresent(existing -> {
                if (!existing.getId().equals(id)) {
                    throw new ConflictException("PRODUCT_SKU_ALREADY_EXISTS",
                        "Another product with this SKU already exists");
                }
            });

        // Validate category if provided
        validateCategoryIfExists(request.categoryId(), tenantId);

        // Validate price relationship
        validatePriceRelationship(request.costPrice(), request.salePrice());

        // Update product
        productMapper.updateEntityFromRequest(request, product);

        // Validate entity-level invariants
        if (product.getCostPrice() != null && product.getSalePrice() != null) {
            product.validatePriceRelationship();
        }

        Product updatedProduct = productRepository.save(product);

        logger.info("Product updated successfully: {}", updatedProduct.getId());

        return productMapper.toResponse(updatedProduct);
    }

    @Override
    @Transactional
    public void delete(Long id) {
        logger.info("Deleting product with ID: {}", id);

        Long tenantId = TenantContext.getTenantId();

        // Find product ensuring it belongs to tenant
        Product product = productRepository.findByIdAndTenantId(id, tenantId)
            .orElseThrow(() -> new NotFoundException("PRODUCT_NOT_FOUND",
                "Product not found with ID: " + id));

        // Soft delete by deactivating (handled by @SQLDelete)
        productRepository.delete(product);

        logger.info("Product deleted successfully: {}", id);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ProductResponse> findAll(Pageable pageable) {
        logger.debug("Fetching all products for tenant");

        Long tenantId = TenantContext.getTenantId();
        Page<Product> products = productRepository.findAllByTenantId(tenantId, pageable);

        logger.debug("Found {} products", products.getTotalElements());

        return products.map(productMapper::toResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public ProductResponse findById(Long id) {
        logger.debug("Finding product by ID: {}", id);

        Long tenantId = TenantContext.getTenantId();
        Product product = productRepository.findByIdAndTenantId(id, tenantId)
            .orElseThrow(() -> new NotFoundException("PRODUCT_NOT_FOUND",
                "Product not found with ID: " + id));

        return productMapper.toResponse(product);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ProductResponse> findByCategory(Long categoryId, Pageable pageable) {
        logger.debug("Finding products by category ID: {}", categoryId);

        Long tenantId = TenantContext.getTenantId();

        // Validate category exists and belongs to tenant
        if (categoryId != null) {
            categoryRepository.findByIdAndTenantId(categoryId, tenantId)
                .orElseThrow(() -> new NotFoundException("CATEGORY_NOT_FOUND",
                    "Category not found with ID: " + categoryId));
        }

        Page<Product> products = productRepository.findByCategoryIdAndTenantId(categoryId, tenantId, pageable);

        logger.debug("Found {} products in category {}", products.getTotalElements(), categoryId);

        return products.map(productMapper::toResponse);
    }

    /**
     * Validates that the category exists and belongs to the tenant.
     *
     * @param categoryId the category ID to validate
     * @param tenantId   the tenant ID
     * @throws NotFoundException if category not found
     */
    private void validateCategoryIfExists(Long categoryId, Long tenantId) {
        if (categoryId != null) {
            categoryRepository.findByIdAndTenantId(categoryId, tenantId)
                .orElseThrow(() -> new NotFoundException("CATEGORY_NOT_FOUND",
                    "Category not found with ID: " + categoryId));
        }
    }

    /**
     * Validates that cost price is not greater than sale price.
     *
     * @param costPrice the cost price
     * @param salePrice the sale price
     * @throws ValidationException if cost price exceeds sale price
     */
    private void validatePriceRelationship(BigDecimal costPrice, BigDecimal salePrice) {
        if (costPrice != null && salePrice != null) {
            if (costPrice.compareTo(salePrice) > 0) {
                throw new ValidationException("INVALID_PRICE_RELATIONSHIP",
                    "Cost price cannot be greater than sale price");
            }
        }
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ProductResponse> search(String search, Long categoryId, BigDecimal minPrice,
                                        BigDecimal maxPrice, Boolean isActive, String sortBy,
                                        String sortOrder, int page, int size) {
        logger.debug("Searching products with filters - search: {}, categoryId: {}, minPrice: {}, maxPrice: {}, isActive: {}, sortBy: {}, sortOrder: {}, page: {}, size: {}",
                search, categoryId, minPrice, maxPrice, isActive, sortBy, sortOrder, page, size);

        Long tenantId = TenantContext.getTenantId();

        // Default active filter to true when not provided
        Boolean effectiveIsActive = isActive != null ? isActive : Boolean.TRUE;

        // Validate and default sortBy
        String validSortBy = VALID_SORT_FIELDS.contains(sortBy) ? sortBy : "name";

        // Validate and default sortOrder
        Sort.Direction direction = VALID_SORT_ORDERS.contains(sortOrder)
                ? Sort.Direction.fromString(sortOrder)
                : Sort.Direction.ASC;

        // Validate category if provided
        validateCategoryIfExists(categoryId, tenantId);

        // Create Pageable with Sort
        Sort sort = Sort.by(direction, validSortBy);
        Pageable pageable = PageRequest.of(page, size, sort);

        // Execute search
        Page<Product> products = productRepository.searchProducts(
                search, categoryId, minPrice, maxPrice, effectiveIsActive, tenantId, pageable);

        logger.debug("Found {} products matching search criteria", products.getTotalElements());

        return products.map(productMapper::toResponse);
    }

    @Override
    @Transactional
    public ProductResponse updateActive(Long productId, Boolean isActive) {
        logger.info("Updating active status for product with ID: {}", productId);

        if (isActive == null) {
            throw new ValidationException("VALIDATION_ERROR",
                "Active status is required");
        }

        Long tenantId = TenantContext.getTenantId();

        // Find product ensuring it belongs to tenant
        Product product = productRepository.findByIdAndTenantIdIncludingInactive(productId, tenantId)
            .orElseThrow(() -> new NotFoundException("PRODUCT_NOT_FOUND",
                "Product not found with ID: " + productId));

        product.setActive(isActive);

        // Save product
        Product updatedProduct = productRepository.save(product);

        logger.info("Product active status updated successfully: {} (now active: {})",
            updatedProduct.getId(), updatedProduct.isActive());

        return productMapper.toResponse(updatedProduct);
    }
}
