package com.stockflow.modules.inventory.application.service;

import com.stockflow.modules.catalog.domain.model.Product;
import com.stockflow.modules.catalog.domain.repository.ProductRepository;
import com.stockflow.modules.branches.domain.model.Branch;
import com.stockflow.modules.inventory.application.dto.BranchStockResponse;
import com.stockflow.modules.inventory.application.dto.StockMovementRequest;
import com.stockflow.modules.inventory.application.dto.StockMovementResponse;
import com.stockflow.modules.inventory.application.dto.TransferResult;
import com.stockflow.modules.inventory.application.dto.TransferStockRequest;
import com.stockflow.modules.inventory.application.mapper.InventoryMapper;
import com.stockflow.modules.inventory.domain.model.BranchProductStock;
import com.stockflow.modules.inventory.domain.model.MovementReason;
import com.stockflow.modules.inventory.domain.model.MovementType;
import com.stockflow.modules.inventory.domain.model.StockMovement;
import com.stockflow.modules.inventory.domain.repository.BranchProductStockRepository;
import com.stockflow.modules.inventory.domain.repository.StockMovementRepository;
import com.stockflow.modules.branches.domain.repository.BranchRepository;
import com.stockflow.modules.users.domain.model.RoleEnum;
import com.stockflow.shared.domain.exception.InsufficientStockException;
import com.stockflow.shared.domain.exception.ForbiddenException;
import com.stockflow.shared.domain.exception.NotFoundException;
import com.stockflow.shared.domain.exception.ValidationException;
import com.stockflow.shared.infrastructure.cache.CacheConfig;
import com.stockflow.shared.infrastructure.security.CustomUserDetails;
import com.stockflow.shared.infrastructure.security.TenantContext;
import org.springframework.cache.annotation.CacheEvict;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Implementation of inventory service.
 *
 * <p>Handles business logic for stock management including querying stock levels,
 * creating movements, and transferring stock between branches. All operations are
 * scoped to the current tenant and include validation to ensure data integrity.</p>
 */
@Service
public class InventoryServiceImpl implements InventoryService {

    private static final Logger logger = LoggerFactory.getLogger(InventoryServiceImpl.class);

    private final BranchProductStockRepository stockRepository;
    private final StockMovementRepository movementRepository;
    private final BranchRepository branchRepository;
    private final ProductRepository productRepository;
    private final InventoryMapper inventoryMapper;

    public InventoryServiceImpl(BranchProductStockRepository stockRepository,
                                StockMovementRepository movementRepository,
                                BranchRepository branchRepository,
                                ProductRepository productRepository,
                                InventoryMapper inventoryMapper) {
        this.stockRepository = stockRepository;
        this.movementRepository = movementRepository;
        this.branchRepository = branchRepository;
        this.productRepository = productRepository;
        this.inventoryMapper = inventoryMapper;
    }

    @Override
    @Transactional(readOnly = true)
    public BranchStockResponse getStock(Long branchId, Long productId) {
        logger.debug("Getting stock for branch {} and product {}", branchId, productId);

        Long tenantId = TenantContext.getTenantId();

        // Validate branch exists and belongs to tenant
        validateBranchExists(branchId, tenantId);

        // Validate product exists and belongs to tenant
        validateProductExists(productId, tenantId);

        // Find or create stock entry
        BranchProductStock stock = stockRepository.findByTenantIdAndBranchIdAndProductId(
            branchId, productId, tenantId
        ).orElseGet(() -> {
            logger.debug("Stock entry not found, creating new entry with zero quantity");
            BranchProductStock newStock = new BranchProductStock(tenantId, branchId, productId, 0);
            return stockRepository.save(newStock);
        });

        return inventoryMapper.toResponse(stock);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<BranchStockResponse> getAllStock(Pageable pageable) {
        logger.debug("Getting all stock for tenant");

        Long tenantId = TenantContext.getTenantId();
        Page<BranchProductStock> stocks = stockRepository.findByTenantId(tenantId, pageable);

        logger.debug("Found {} stock entries", stocks.getTotalElements());

        return stocks.map(inventoryMapper::toResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<BranchStockResponse> getStockByBranch(Long branchId, Pageable pageable) {
        logger.debug("Getting stock for branch {}", branchId);

        Long tenantId = TenantContext.getTenantId();

        // Validate branch exists and belongs to tenant
        validateBranchExists(branchId, tenantId);

        Page<BranchProductStock> stocks = stockRepository.findByTenantIdAndBranchId(
            branchId, tenantId, pageable
        );

        logger.debug("Found {} stock entries for branch {}", stocks.getTotalElements(), branchId);

        return stocks.map(inventoryMapper::toResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<BranchStockResponse> getStockByProduct(Long productId, Pageable pageable) {
        logger.debug("Getting stock for product {}", productId);

        Long tenantId = TenantContext.getTenantId();

        // Validate product exists and belongs to tenant
        validateProductExists(productId, tenantId);

        Page<BranchProductStock> stocks = stockRepository.findByTenantIdAndProductId(
            productId, tenantId, pageable
        );

        logger.debug("Found {} stock entries for product {}", stocks.getTotalElements(), productId);

        return stocks.map(inventoryMapper::toResponse);
    }

    @Override
    @Transactional
    @CacheEvict(value = {
            CacheConfig.DASHBOARD_OVERVIEW,
            CacheConfig.DASHBOARD_BRANCH,
            CacheConfig.TOP_PRODUCTS
    }, allEntries = true)
    public StockMovementResponse createMovement(StockMovementRequest request) {
        logger.info("Creating stock movement: branch={}, product={}, type={}, reason={}, quantity={}",
            request.branchId(), request.productId(), request.type(), request.reason(), request.quantity());

        Long tenantId = TenantContext.getTenantId();
        Long userId = getCurrentUserId();

        validateStaffPermissions(request);

        // Validate branch exists and belongs to tenant
        validateBranchExists(request.branchId(), tenantId);

        // Validate product exists and belongs to tenant
        validateProductExists(request.productId(), tenantId);

        // Get or create stock entry
        BranchProductStock stock = stockRepository.findByTenantIdAndBranchIdAndProductId(
            request.branchId(), request.productId(), tenantId
        ).orElseGet(() -> new BranchProductStock(tenantId, request.branchId(), request.productId()));

        // Validate stock availability for OUT movements
        if (isOutMovement(request)) {
            validateStockAvailability(stock, request.quantity());
        }

        // Create stock movement record
        StockMovement movement = inventoryMapper.toEntity(request, tenantId, userId);
        StockMovement savedMovement = movementRepository.save(movement);

        // Update stock quantity based on movement type and reason
        updateStockQuantity(stock, request);

        // Save updated stock
        try {
            stockRepository.save(stock);
        } catch (OptimisticLockingFailureException e) {
            logger.error("Optimistic locking failure for stock: branch={}, product={}",
                request.branchId(), request.productId());
            throw new ValidationException("STOCK_CONCURRENT_MODIFICATION",
                "Stock was modified by another transaction. Please retry.");
        }

        logger.info("Stock movement created successfully with ID: {}", savedMovement.getId());

        return inventoryMapper.toResponse(savedMovement);
    }

    @Override
    @Transactional
    @CacheEvict(value = {
            CacheConfig.DASHBOARD_OVERVIEW,
            CacheConfig.DASHBOARD_BRANCH,
            CacheConfig.TOP_PRODUCTS
    }, allEntries = true)
    public TransferResult transferStock(TransferStockRequest request) {
        logger.info("Transferring stock: sourceBranch={}, destBranch={}, product={}, quantity={}",
            request.sourceBranchId(), request.destinationBranchId(), request.productId(), request.quantity());

        Long tenantId = TenantContext.getTenantId();
        Long userId = getCurrentUserId();

        // Validate source and destination are different
        if (request.sourceBranchId().equals(request.destinationBranchId())) {
            throw new ValidationException("TRANSFER_SAME_BRANCH",
                "Source and destination branches cannot be the same");
        }

        // Validate both branches exist and belong to tenant
        validateBranchExists(request.sourceBranchId(), tenantId);
        validateBranchExists(request.destinationBranchId(), tenantId);

        // Validate product exists and belongs to tenant
        validateProductExists(request.productId(), tenantId);

        // Create OUT movement for source branch
        StockMovementRequest outRequest = new StockMovementRequest(
            request.sourceBranchId(),
            request.productId(),
            MovementType.OUT,
            MovementReason.TRANSFER_OUT,
            request.quantity(),
            request.note()
        );

        StockMovementResponse fromMovement = createMovement(outRequest);

        // Create IN movement for destination branch
        StockMovementRequest inRequest = new StockMovementRequest(
            request.destinationBranchId(),
            request.productId(),
            MovementType.IN,
            MovementReason.TRANSFER_IN,
            request.quantity(),
            request.note()
        );

        StockMovementResponse toMovement = createMovement(inRequest);

        logger.info("Stock transfer completed successfully");

        return new TransferResult(fromMovement.id(), toMovement.id());
    }

    @Override
    @Transactional(readOnly = true)
    public Page<StockMovementResponse> getMovementsByBranch(Long branchId,
                                                            Long productId,
                                                            MovementType type,
                                                            MovementReason reason,
                                                            Pageable pageable) {
        logger.debug("Getting movement history for branch {} with filters", branchId);

        Long tenantId = TenantContext.getTenantId();

        // Validate branch exists and belongs to tenant
        validateBranchExists(branchId, tenantId);

        if (productId != null) {
            validateProductExistsIncludingInactive(productId, tenantId);
        }

        Page<StockMovement> movements = movementRepository.findByBranchWithFilters(
            branchId, tenantId, productId, type, reason, pageable
        );

        logger.debug("Found {} movements", movements.getTotalElements());

        return movements.map(inventoryMapper::toResponse);
    }

    // Private helper methods

    /**
     * Validates that the branch exists and belongs to the tenant.
     *
     * @param branchId the branch ID to validate
     * @param tenantId the tenant ID
     * @throws NotFoundException if branch not found
     */
    private void validateBranchExists(Long branchId, Long tenantId) {
        Branch branch = branchRepository.findByIdAndTenantIdIncludingInactive(branchId, tenantId)
            .orElseThrow(() -> new NotFoundException("BRANCH_NOT_FOUND",
                "Branch not found with ID: " + branchId));

        if (!branch.isActive()) {
            throw new ValidationException("BRANCH_ACTIVE_REQUIRED",
                "Branch must be active for this operation");
        }
    }

    /**
     * Validates that the product exists and belongs to the tenant.
     *
     * @param productId the product ID to validate
     * @param tenantId the tenant ID
     * @throws NotFoundException if product not found
     */
    private void validateProductExists(Long productId, Long tenantId) {
        Product product = productRepository.findByIdAndTenantIdIncludingInactive(productId, tenantId)
            .orElseThrow(() -> new NotFoundException("PRODUCT_NOT_FOUND",
                "Product not found with ID: " + productId));

        if (!product.isActive()) {
            throw new ValidationException("PRODUCT_ACTIVE_REQUIRED",
                "Product must be active for this operation");
        }
    }

    private void validateProductExistsIncludingInactive(Long productId, Long tenantId) {
        productRepository.findByIdAndTenantIdIncludingInactive(productId, tenantId)
            .orElseThrow(() -> new NotFoundException("PRODUCT_NOT_FOUND",
                "Product not found with ID: " + productId));
    }

    private void validateStaffPermissions(StockMovementRequest request) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !(authentication.getPrincipal() instanceof CustomUserDetails userDetails)) {
            return;
        }

        boolean isStaffOnly = userDetails.hasRole(RoleEnum.STAFF)
            && !userDetails.hasRole(RoleEnum.ADMIN)
            && !userDetails.hasRole(RoleEnum.MANAGER);

        if (!isStaffOnly) {
            return;
        }

        boolean allowedType = request.type() == MovementType.IN || request.type() == MovementType.OUT;
        boolean allowedReason = request.reason() != MovementReason.ADJUSTMENT_IN
            && request.reason() != MovementReason.ADJUSTMENT_OUT
            && request.reason() != MovementReason.TRANSFER_IN
            && request.reason() != MovementReason.TRANSFER_OUT;

        if (!allowedType || !allowedReason) {
            throw new ForbiddenException("INSUFFICIENT_PRIVILEGES",
                "STAFF users can only create IN or OUT movements");
        }
    }

    /**
     * Checks if the movement is an OUT movement.
     *
     * @param request the stock movement request
     * @return true if this is an OUT movement
     */
    private boolean isOutMovement(StockMovementRequest request) {
        return request.type() == MovementType.OUT ||
               request.reason() == MovementReason.TRANSFER_OUT ||
               request.reason() == MovementReason.ADJUSTMENT_OUT ||
               request.reason() == MovementReason.SALE ||
               request.reason() == MovementReason.LOSS;
    }

    /**
     * Validates that sufficient stock is available for an OUT movement.
     *
     * @param stock    the current stock
     * @param quantity the quantity to withdraw
     * @throws InsufficientStockException if stock is insufficient
     */
    private void validateStockAvailability(BranchProductStock stock, Integer quantity) {
        int currentQuantity = stock.getQuantity() != null ? stock.getQuantity() : 0;
        if (currentQuantity < quantity) {
            logger.warn("Insufficient stock: available={}, requested={}", currentQuantity, quantity);
            throw InsufficientStockException.of(
                stock.getProductId(),
                stock.getBranchId(),
                quantity,
                currentQuantity
            );
        }
    }

    /**
     * Updates stock quantity based on movement type and reason.
     *
     * @param stock   the stock to update
     * @param request the movement request
     */
    private void updateStockQuantity(BranchProductStock stock, StockMovementRequest request) {
        int quantity = request.quantity();

        // Determine if this increases or decreases stock
        if (request.type() == MovementType.IN ||
            request.reason() == MovementReason.ADJUSTMENT_IN ||
            request.reason() == MovementReason.TRANSFER_IN ||
            request.reason() == MovementReason.PURCHASE ||
            request.reason() == MovementReason.RETURN) {
            // Increase stock
            stock.addQuantity(quantity);
            logger.debug("Increased stock by {}: new quantity = {}", quantity, stock.getQuantity());
        } else {
            // Decrease stock
            stock.subtractQuantity(quantity);
            logger.debug("Decreased stock by {}: new quantity = {}", quantity, stock.getQuantity());
        }
    }

    /**
     * Gets the ID of the currently authenticated user.
     *
     * @return the user ID, or null if not authenticated
     */
    private Long getCurrentUserId() {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication != null && authentication.isAuthenticated()) {
                Object principal = authentication.getPrincipal();
                if (principal instanceof CustomUserDetails customUserDetails) {
                    return customUserDetails.getUserId();
                }
                if (principal instanceof String) {
                    try {
                        return Long.parseLong((String) principal);
                    } catch (NumberFormatException e) {
                        logger.warn("Could not parse user ID from principal: {}", principal);
                        return null;
                    }
                }
            }
        } catch (Exception e) {
            logger.warn("Could not get current user ID", e);
        }
        return null;
    }
}
