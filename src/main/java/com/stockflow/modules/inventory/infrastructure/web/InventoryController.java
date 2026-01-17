package com.stockflow.modules.inventory.infrastructure.web;

import com.stockflow.modules.inventory.application.dto.BranchStockResponse;
import com.stockflow.modules.inventory.application.dto.StockMovementRequest;
import com.stockflow.modules.inventory.application.dto.StockMovementResponse;
import com.stockflow.modules.inventory.application.dto.TransferStockRequest;
import com.stockflow.modules.inventory.application.service.InventoryService;
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
 * REST controller for inventory operations.
 *
 * <p>Provides endpoints for managing stock levels, creating movements, and transferring stock.
 * All operations are scoped to the current tenant.</p>
 */
@RestController
@RequestMapping("/api/inventory")
@Tag(name = "Inventory", description = "Inventory and stock management endpoints")
@SecurityRequirement(name = "bearerAuth")
public class InventoryController {

    private final InventoryService inventoryService;

    public InventoryController(InventoryService inventoryService) {
        this.inventoryService = inventoryService;
    }

    // Stock Query Endpoints

    /**
     * Gets all stock entries for the current tenant with pagination.
     *
     * @param pageable pagination parameters
     * @return page of branch stock responses
     */
    @GetMapping("/stock")
    @Operation(summary = "Get all stock", description = "Retrieves all stock entries for the current tenant with pagination")
    public ResponseEntity<Page<BranchStockResponse>> getAllStock(
            @Parameter(description = "Pagination parameters")
            @PageableDefault(size = 20) Pageable pageable) {
        Page<BranchStockResponse> response = inventoryService.getAllStock(pageable);
        return ResponseEntity.ok(response);
    }

    /**
     * Gets all stock entries for a specific branch with pagination.
     *
     * @param branchId the branch ID
     * @param pageable pagination parameters
     * @return page of branch stock responses
     */
    @GetMapping("/stock/branch/{branchId}")
    @Operation(summary = "Get stock by branch", description = "Retrieves all stock entries for a specific branch with pagination")
    public ResponseEntity<Page<BranchStockResponse>> getStockByBranch(
            @Parameter(description = "Branch ID", required = true)
            @PathVariable Long branchId,
            @Parameter(description = "Pagination parameters")
            @PageableDefault(size = 20) Pageable pageable) {
        Page<BranchStockResponse> response = inventoryService.getStockByBranch(branchId, pageable);
        return ResponseEntity.ok(response);
    }

    /**
     * Gets all stock entries for a specific product across all branches with pagination.
     *
     * @param productId the product ID
     * @param pageable  pagination parameters
     * @return page of branch stock responses
     */
    @GetMapping("/stock/product/{productId}")
    @Operation(summary = "Get stock by product", description = "Retrieves all stock entries for a specific product across all branches with pagination")
    public ResponseEntity<Page<BranchStockResponse>> getStockByProduct(
            @Parameter(description = "Product ID", required = true)
            @PathVariable Long productId,
            @Parameter(description = "Pagination parameters")
            @PageableDefault(size = 20) Pageable pageable) {
        Page<BranchStockResponse> response = inventoryService.getStockByProduct(productId, pageable);
        return ResponseEntity.ok(response);
    }

    /**
     * Gets the current stock for a specific product in a specific branch.
     *
     * @param branchId  the branch ID
     * @param productId the product ID
     * @return the branch stock response
     */
    @GetMapping("/stock/branch/{branchId}/product/{productId}")
    @Operation(summary = "Get specific stock", description = "Retrieves the current stock for a specific product in a specific branch")
    public ResponseEntity<BranchStockResponse> getStock(
            @Parameter(description = "Branch ID", required = true)
            @PathVariable Long branchId,
            @Parameter(description = "Product ID", required = true)
            @PathVariable Long productId) {
        BranchStockResponse response = inventoryService.getStock(branchId, productId);
        return ResponseEntity.ok(response);
    }

    // Movement Endpoints

    /**
     * Creates a new stock movement (IN, OUT, or ADJUSTMENT).
     *
     * @param request the stock movement request
     * @return the created stock movement response
     */
    @PostMapping("/movements")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    @Operation(summary = "Create stock movement", description = "Creates a new stock movement (IN, OUT, or ADJUSTMENT). Requires ADMIN or MANAGER role.")
    public ResponseEntity<StockMovementResponse> createMovement(@RequestBody StockMovementRequest request) {
        StockMovementResponse response = inventoryService.createMovement(request);
        return ResponseEntity.ok(response);
    }

    /**
     * Gets all movement history for the current tenant with pagination.
     *
     * @param pageable pagination parameters
     * @return page of stock movement responses
     */
    @GetMapping("/movements")
    @Operation(summary = "Get all movements", description = "Retrieves all movement history for the current tenant with pagination")
    public ResponseEntity<Page<StockMovementResponse>> getAllMovements(
            @Parameter(description = "Pagination parameters")
            @PageableDefault(size = 20) Pageable pageable) {
        Page<StockMovementResponse> response = inventoryService.getAllMovements(pageable);
        return ResponseEntity.ok(response);
    }

    /**
     * Gets movement history for a specific product in a specific branch.
     *
     * @param branchId  the branch ID
     * @param productId the product ID
     * @param pageable  pagination parameters
     * @return page of stock movement responses
     */
    @GetMapping("/movements/branch/{branchId}/product/{productId}")
    @Operation(summary = "Get movement history", description = "Retrieves movement history for a specific product in a specific branch with pagination")
    public ResponseEntity<Page<StockMovementResponse>> getMovementHistory(
            @Parameter(description = "Branch ID", required = true)
            @PathVariable Long branchId,
            @Parameter(description = "Product ID", required = true)
            @PathVariable Long productId,
            @Parameter(description = "Pagination parameters")
            @PageableDefault(size = 20) Pageable pageable) {
        Page<StockMovementResponse> response = inventoryService.getMovementHistory(
            branchId, productId, pageable);
        return ResponseEntity.ok(response);
    }

    // Transfer Endpoints

    /**
     * Transfers stock from one branch to another.
     *
     * <p>This creates two movements: TRANSFER_OUT for source branch and TRANSFER_IN for destination branch.</p>
     *
     * @param request the transfer stock request
     * @return the transfer result containing both movements
     */
    @PostMapping("/transfers")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    @Operation(summary = "Transfer stock", description = "Transfers stock from one branch to another. Creates TRANSFER_OUT and TRANSFER_IN movements. Requires ADMIN or MANAGER role.")
    public ResponseEntity<InventoryService.TransferResult> transferStock(@RequestBody TransferStockRequest request) {
        InventoryService.TransferResult response = inventoryService.transferStock(request);
        return ResponseEntity.ok(response);
    }
}
