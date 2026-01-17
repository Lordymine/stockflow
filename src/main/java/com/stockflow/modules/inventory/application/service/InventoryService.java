package com.stockflow.modules.inventory.application.service;

import com.stockflow.modules.inventory.application.dto.BranchStockResponse;
import com.stockflow.modules.inventory.application.dto.StockMovementRequest;
import com.stockflow.modules.inventory.application.dto.StockMovementResponse;
import com.stockflow.modules.inventory.application.dto.TransferStockRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

/**
 * Service interface for inventory management operations.
 *
 * <p>Provides business logic for stock management including:</p>
 * <ul>
 *   <li>Querying stock levels by branch and product</li>
 *   <li>Creating stock movements (IN, OUT, ADJUSTMENT)</li>
 *   <li>Transferring stock between branches</li>
 *   <li>Retrieving movement history</li>
 * </ul>
 *
 * <p>All operations are scoped to the current tenant and include proper validation
 * to ensure stock never goes negative and branches/products belong to the tenant.</p>
 */
public interface InventoryService {

    /**
     * Gets the current stock for a specific product in a specific branch.
     *
     * @param branchId  the branch ID
     * @param productId the product ID
     * @return the branch stock response
     */
    BranchStockResponse getStock(Long branchId, Long productId);

    /**
     * Gets all stock entries for the current tenant with pagination.
     *
     * @param pageable pagination parameters
     * @return page of branch stock responses
     */
    Page<BranchStockResponse> getAllStock(Pageable pageable);

    /**
     * Gets all stock entries for a specific branch with pagination.
     *
     * @param branchId the branch ID
     * @param pageable pagination parameters
     * @return page of branch stock responses
     */
    Page<BranchStockResponse> getStockByBranch(Long branchId, Pageable pageable);

    /**
     * Gets all stock entries for a specific product across all branches with pagination.
     *
     * @param productId the product ID
     * @param pageable  pagination parameters
     * @return page of branch stock responses
     */
    Page<BranchStockResponse> getStockByProduct(Long productId, Pageable pageable);

    /**
     * Creates a new stock movement (IN, OUT, or ADJUSTMENT).
     *
     * <p>This method:</p>
     * <ul>
     *   <li>Validates that the branch exists and belongs to the tenant</li>
     *   <li>Validates that the product exists and belongs to the tenant</li>
     *   <li>For OUT movements, validates sufficient stock is available</li>
     *   <li>Creates the stock movement record</li>
     *   <li>Updates the branch product stock accordingly</li>
     * </ul>
     *
     * @param request the stock movement request
     * @return the created stock movement response
     * @throws com.stockflow.shared.domain.exception.InsufficientStockException if OUT movement would make stock negative
     * @throws com.stockflow.shared.domain.exception.NotFoundException       if branch or product not found
     */
    StockMovementResponse createMovement(StockMovementRequest request);

    /**
     * Transfers stock from one branch to another.
     *
     * <p>This method:</p>
     * <ul>
     *   <li>Validates both branches exist and belong to the tenant</li>
     *   <li>Validates the product exists and belongs to the tenant</li>
     *   <li>Validates sufficient stock is available in the source branch</li>
     *   <li>Creates a TRANSFER_OUT movement for the source branch</li>
     *   <li>Creates a TRANSFER_IN movement for the destination branch</li>
     *   <li>Updates stock levels in both branches atomically</li>
     * </ul>
     *
     * @param request the transfer stock request
     * @return the transfer result containing both movements
     * @throws com.stockflow.shared.domain.exception.InsufficientStockException if insufficient stock in source branch
     * @throws com.stockflow.shared.domain.exception.NotFoundException       if branches or product not found
     * @throws com.stockflow.shared.domain.exception.ValidationException      if source and destination are the same
     */
    TransferResult transferStock(TransferStockRequest request);

    /**
     * Gets the movement history for a specific product in a specific branch.
     *
     * @param branchId  the branch ID
     * @param productId the product ID
     * @param pageable  pagination parameters
     * @return page of stock movement responses
     */
    Page<StockMovementResponse> getMovementHistory(Long branchId, Long productId, Pageable pageable);

    /**
     * Gets all movement history for the current tenant with pagination.
     *
     * @param pageable pagination parameters
     * @return page of stock movement responses
     */
    Page<StockMovementResponse> getAllMovements(Pageable pageable);

    /**
     * Result of a stock transfer operation containing both movements.
     *
     * @param fromMovement the movement from the source branch
     * @param toMovement   the movement to the destination branch
     */
    record TransferResult(
        StockMovementResponse fromMovement,
        StockMovementResponse toMovement
    ) {
    }
}
