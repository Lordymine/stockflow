package com.stockflow.modules.branches.infrastructure.web;

import com.stockflow.modules.branches.application.dto.*;
import com.stockflow.modules.branches.application.service.BranchService;
import com.stockflow.shared.application.dto.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/branches")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Branches", description = "Branch Management API")
@SecurityRequirement(name = "bearerAuth")
public class BranchController {

    private final BranchService branchService;

    @PostMapping
    @Operation(summary = "Create branch", description = "Create a new branch (ADMIN, MANAGER)")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public ResponseEntity<ApiResponse<BranchResponse>> createBranch(
        @Valid @RequestBody CreateBranchRequest request
    ) {
        log.info("Creating branch with code: {}", request.code());
        BranchResponse response = branchService.createBranch(request);
        return ResponseEntity
            .status(HttpStatus.CREATED)
            .body(ApiResponse.ok(response));
    }

    @GetMapping
    @Operation(summary = "List branches", description = "Get all branches with pagination (ADMIN, MANAGER, STAFF)")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'STAFF')")
    public ResponseEntity<ApiResponse<Page<BranchResponse>>> getAllBranches(
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "20") int size,
        @RequestParam(defaultValue = "name") String sortBy,
        @RequestParam(defaultValue = "asc") String sortDir
    ) {
        log.info("Fetching all branches - page: {}, size: {}", page, size);

        Sort sort = sortDir.equalsIgnoreCase("desc")
            ? Sort.by(sortBy).descending()
            : Sort.by(sortBy).ascending();

        PageRequest pageRequest = PageRequest.of(page, size, sort);
        Page<BranchResponse> branches = branchService.getAllBranches(pageRequest);

        return ResponseEntity.ok(ApiResponse.ok(branches));
    }

    @GetMapping("/active")
    @Operation(summary = "List active branches", description = "Get only active branches")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'STAFF')")
    public ResponseEntity<ApiResponse<Page<BranchResponse>>> getActiveBranches(
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "20") int size
    ) {
        log.info("Fetching active branches");

        PageRequest pageRequest = PageRequest.of(page, size, Sort.by("name").ascending());
        Page<BranchResponse> branches = branchService.getActiveBranches(pageRequest);

        return ResponseEntity.ok(ApiResponse.ok(branches));
    }

    @GetMapping("/search")
    @Operation(summary = "Search branches", description = "Search branches by name or code")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'STAFF')")
    public ResponseEntity<ApiResponse<Page<BranchResponse>>> searchBranches(
        @RequestParam String search,
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "20") int size
    ) {
        log.info("Searching branches with term: {}", search);

        PageRequest pageRequest = PageRequest.of(page, size, Sort.by("name").ascending());
        Page<BranchResponse> branches = branchService.searchBranches(search, pageRequest);

        return ResponseEntity.ok(ApiResponse.ok(branches));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get branch by ID", description = "Get branch details by ID")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'STAFF')")
    public ResponseEntity<ApiResponse<BranchResponse>> getBranchById(@PathVariable Long id) {
        log.info("Fetching branch with ID: {}", id);
        BranchResponse response = branchService.getBranchById(id);
        return ResponseEntity.ok(ApiResponse.ok(response));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update branch", description = "Update branch details (ADMIN, MANAGER)")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public ResponseEntity<ApiResponse<BranchResponse>> updateBranch(
        @PathVariable Long id,
        @Valid @RequestBody UpdateBranchRequest request
    ) {
        log.info("Updating branch with ID: {}", id);
        BranchResponse response = branchService.updateBranch(id, request);
        return ResponseEntity.ok(ApiResponse.ok(response));
    }

    @PatchMapping("/{id}/active")
    @Operation(summary = "Toggle branch active status", description = "Activate or deactivate branch (ADMIN only)")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> toggleBranchActive(
        @PathVariable Long id,
        @RequestParam Boolean isActive
    ) {
        log.info("Toggling active status for branch ID: {}", id);
        branchService.toggleBranchActive(id, isActive);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete branch", description = "Soft delete branch (ADMIN only)")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteBranch(@PathVariable Long id) {
        log.info("Deleting branch with ID: {}", id);
        branchService.deleteBranch(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/batch")
    @Operation(summary = "Get branches by IDs", description = "Get multiple branches by IDs")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'STAFF')")
    public ResponseEntity<ApiResponse<java.util.List<BranchResponse>>> getBranchesByIds(
        @RequestBody java.util.List<Long> ids
    ) {
        log.info("Fetching branches by IDs: {}", ids);
        java.util.List<BranchResponse> branches = branchService.getBranchesByIds(ids);
        return ResponseEntity.ok(ApiResponse.ok(branches));
    }
}
