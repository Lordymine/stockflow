package com.stockflow.modules.inventory.infrastructure.web;

import com.stockflow.modules.inventory.application.dto.BranchStockResponse;
import com.stockflow.modules.inventory.application.dto.StockMovementCreateRequest;
import com.stockflow.modules.inventory.application.dto.StockMovementRequest;
import com.stockflow.modules.inventory.application.dto.StockMovementResponse;
import com.stockflow.modules.inventory.application.dto.TransferResult;
import com.stockflow.modules.inventory.application.dto.TransferStockRequest;
import com.stockflow.modules.inventory.application.service.InventoryService;
import com.stockflow.modules.inventory.domain.model.MovementReason;
import com.stockflow.modules.inventory.domain.model.MovementType;
import com.stockflow.shared.application.dto.ApiResponse;
import com.stockflow.shared.application.dto.ItemsResponse;
import com.stockflow.shared.application.dto.PageMeta;
import com.stockflow.shared.infrastructure.security.BranchAccess;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

/**
 * REST controller for inventory operations.
 */
@RestController
@RequestMapping("/api/v1")
@Tag(name = "Inventory", description = "Inventory and stock management endpoints")
@SecurityRequirement(name = "bearerAuth")
public class InventoryController {

    private final InventoryService inventoryService;

    public InventoryController(InventoryService inventoryService) {
        this.inventoryService = inventoryService;
    }

    /**
     * Gets all stock entries for a specific branch with pagination.
     *
     * @param branchId the branch ID
     * @param pageable pagination parameters
     * @return paginated list of branch stock responses
     */
    @BranchAccess
    @GetMapping("/branches/{branchId}/stock")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'STAFF')")
    @Operation(summary = "Get stock by branch", description = "Retrieves all stock entries for a branch with pagination")
    public ResponseEntity<ApiResponse<ItemsResponse<BranchStockResponse>>> getStockByBranch(
            @BranchAccess
            @Parameter(description = "Branch ID", required = true)
            @PathVariable Long branchId,
            @Parameter(description = "Pagination parameters")
            @PageableDefault(size = 20) Pageable pageable) {
        Page<BranchStockResponse> response = inventoryService.getStockByBranch(branchId, pageable);
        return ResponseEntity.ok(
            ApiResponse.of(new ItemsResponse<>(response.getContent()), PageMeta.of(response))
        );
    }

    /**
     * Gets the current stock for a specific product in a specific branch.
     *
     * @param branchId  the branch ID
     * @param productId the product ID
     * @return the branch stock response
     */
    @BranchAccess
    @GetMapping("/branches/{branchId}/stock/{productId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'STAFF')")
    @Operation(summary = "Get specific stock", description = "Retrieves the current stock for a specific product in a specific branch")
    public ResponseEntity<ApiResponse<BranchStockResponse>> getStock(
            @BranchAccess
            @Parameter(description = "Branch ID", required = true)
            @PathVariable Long branchId,
            @Parameter(description = "Product ID", required = true)
            @PathVariable Long productId) {
        BranchStockResponse response = inventoryService.getStock(branchId, productId);
        return ResponseEntity.ok(ApiResponse.of(response));
    }

    /**
     * Creates a new stock movement (IN, OUT, or ADJUSTMENT).
     *
     * @param branchId the branch ID
     * @param request  the stock movement request
     * @return the created stock movement response
     */
    @BranchAccess
    @PostMapping("/branches/{branchId}/movements")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'STAFF')")
    @Operation(summary = "Create stock movement", description = "Creates a new stock movement for a branch")
    public ResponseEntity<ApiResponse<StockMovementResponse>> createMovement(
            @BranchAccess
            @Parameter(description = "Branch ID", required = true)
            @PathVariable Long branchId,
            @Valid @RequestBody StockMovementCreateRequest request) {
        StockMovementRequest serviceRequest = new StockMovementRequest(
            branchId,
            request.productId(),
            request.type(),
            request.reason(),
            request.quantity(),
            request.note()
        );
        StockMovementResponse response = inventoryService.createMovement(serviceRequest);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.of(response));
    }

    /**
     * Gets movement history for a specific branch with optional filters.
     *
     * @param branchId the branch ID
     * @param productId optional product ID filter
     * @param type optional movement type filter
     * @param reason optional movement reason filter
     * @param pageable pagination parameters
     * @return paginated list of stock movement responses
     */
    @BranchAccess
    @GetMapping("/branches/{branchId}/movements")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'STAFF')")
    @Operation(summary = "Get movement history", description = "Retrieves movement history for a branch with optional filters")
    public ResponseEntity<ApiResponse<ItemsResponse<StockMovementResponse>>> getMovementHistory(
            @BranchAccess
            @Parameter(description = "Branch ID", required = true)
            @PathVariable Long branchId,
            @Parameter(description = "Filter by product ID")
            @RequestParam(required = false) Long productId,
            @Parameter(description = "Filter by movement type")
            @RequestParam(required = false) MovementType type,
            @Parameter(description = "Filter by movement reason")
            @RequestParam(required = false) MovementReason reason,
            @Parameter(description = "Pagination parameters")
            @PageableDefault(size = 20) Pageable pageable) {
        Page<StockMovementResponse> response = inventoryService.getMovementsByBranch(
            branchId, productId, type, reason, pageable);
        return ResponseEntity.ok(
            ApiResponse.of(new ItemsResponse<>(response.getContent()), PageMeta.of(response))
        );
    }

    /**
     * Transfers stock from one branch to another.
     *
     * <p>This creates two movements: TRANSFER_OUT for source branch and TRANSFER_IN for destination branch.</p>
     *
     * @param request the transfer stock request
     * @return the transfer result containing movement IDs
     */
    @BranchAccess
    @PostMapping("/transfers")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    @Operation(summary = "Transfer stock", description = "Transfers stock between branches")
    public ResponseEntity<ApiResponse<TransferResult>> transferStock(
            @Valid @RequestBody TransferStockRequest request) {
        TransferResult response = inventoryService.transferStock(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.of(response));
    }
}
