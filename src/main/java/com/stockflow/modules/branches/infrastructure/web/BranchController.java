package com.stockflow.modules.branches.infrastructure.web;

import com.stockflow.modules.branches.application.dto.BranchRequest;
import com.stockflow.modules.branches.application.dto.BranchResponse;
import com.stockflow.modules.branches.application.service.BranchService;
import com.stockflow.shared.application.dto.ActiveRequest;
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
 * REST controller for branch operations.
 */
@RestController
@RequestMapping("/api/v1/branches")
@Tag(name = "Branches", description = "Branch management endpoints")
@SecurityRequirement(name = "bearerAuth")
public class BranchController {

    private final BranchService branchService;

    public BranchController(BranchService branchService) {
        this.branchService = branchService;
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Create branch", description = "Creates a new branch (ADMIN only)")
    public ResponseEntity<ApiResponse<BranchResponse>> create(@Valid @RequestBody BranchRequest request) {
        BranchResponse response = branchService.create(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.of(response));
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'STAFF')")
    @Operation(summary = "List branches", description = "Lists branches filtered by the user's branch access")
    public ResponseEntity<ApiResponse<ItemsResponse<BranchResponse>>> list(
            @Parameter(description = "Pagination parameters")
            @PageableDefault(size = 20) Pageable pageable) {
        Page<BranchResponse> response = branchService.list(pageable);
        return ResponseEntity.ok(ApiResponse.of(new ItemsResponse<>(response.getContent()), PageMeta.of(response)));
    }

    @BranchAccess
    @PatchMapping("/{id}/active")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Activate or deactivate branch", description = "Updates branch active status (ADMIN only)")
    public ResponseEntity<ApiResponse<BranchResponse>> updateActiveStatus(
            @BranchAccess
            @Parameter(description = "Branch ID", required = true)
            @PathVariable Long id,
            @Valid @RequestBody ActiveRequest request) {
        BranchResponse response = branchService.updateActive(id, request.isActive());
        return ResponseEntity.ok(ApiResponse.of(response));
    }
}
