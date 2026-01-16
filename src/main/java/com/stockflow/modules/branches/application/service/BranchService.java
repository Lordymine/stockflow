package com.stockflow.modules.branches.application.service;

import com.stockflow.modules.branches.application.dto.*;
import com.stockflow.modules.branches.domain.model.Branch;
import com.stockflow.modules.branches.domain.repository.BranchRepository;
import com.stockflow.shared.domain.exception.BadRequestException;
import com.stockflow.shared.domain.exception.NotFoundException;
import com.stockflow.shared.infrastructure.security.TenantContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class BranchService {

    private final BranchRepository branchRepository;

    @Transactional
    public BranchResponse createBranch(CreateBranchRequest request) {
        log.info("Creating branch with code: {}", request.code());

        Long tenantId = TenantContext.getTenantId();

        if (branchRepository.existsByCodeAndTenantId(request.code(), tenantId)) {
            throw new BadRequestException("Branch with this code already exists");
        }

        Branch branch = Branch.builder()
            .tenantId(tenantId)
            .name(request.name())
            .code(request.code().toUpperCase())
            .address(request.address())
            .phone(request.phone())
            .managerName(request.managerName())
            .isActive(request.isActive() != null ? request.isActive() : true)
            .build();

        branch = branchRepository.save(branch);

        log.info("Branch created successfully with ID: {}", branch.getId());
        return mapToResponse(branch);
    }

    @Transactional(readOnly = true)
    public BranchResponse getBranchById(Long id) {
        log.debug("Fetching branch with ID: {}", id);

        Branch branch = branchRepository.findByIdAndTenantId(id, TenantContext.getTenantId())
            .orElseThrow(() -> new NotFoundException("Branch not found"));

        return mapToResponse(branch);
    }

    @Transactional(readOnly = true)
    public Page<BranchResponse> getAllBranches(Pageable pageable) {
        log.debug("Fetching all branches for tenant");

        Long tenantId = TenantContext.getTenantId();
        Page<Branch> branches = branchRepository.findByTenantId(tenantId, pageable);

        return branches.map(this::mapToResponse);
    }

    @Transactional(readOnly = true)
    public Page<BranchResponse> getActiveBranches(Pageable pageable) {
        log.debug("Fetching active branches");

        Long tenantId = TenantContext.getTenantId();
        Page<Branch> branches = branchRepository.findByTenantIdAndIsActiveTrue(tenantId, pageable);

        return branches.map(this::mapToResponse);
    }

    @Transactional(readOnly = true)
    public Page<BranchResponse> searchBranches(String search, Pageable pageable) {
        log.debug("Searching branches with term: {}", search);

        Long tenantId = TenantContext.getTenantId();
        Page<Branch> branches = branchRepository.searchActiveBranches(tenantId, search, pageable);

        return branches.map(this::mapToResponse);
    }

    @Transactional
    public BranchResponse updateBranch(Long id, UpdateBranchRequest request) {
        log.info("Updating branch with ID: {}", id);

        Branch branch = branchRepository.findByIdAndTenantId(id, TenantContext.getTenantId())
            .orElseThrow(() -> new NotFoundException("Branch not found"));

        if (request.name() != null) {
            branch.setName(request.name());
        }

        if (request.address() != null) {
            branch.setAddress(request.address());
        }

        if (request.phone() != null) {
            branch.setPhone(request.phone());
        }

        if (request.managerName() != null) {
            branch.setManagerName(request.managerName());
        }

        branch = branchRepository.save(branch);

        log.info("Branch updated successfully");
        return mapToResponse(branch);
    }

    @Transactional
    public void toggleBranchActive(Long id, Boolean isActive) {
        log.info("Toggling active status for branch ID: {}", id);

        Branch branch = branchRepository.findByIdAndTenantId(id, TenantContext.getTenantId())
            .orElseThrow(() -> new NotFoundException("Branch not found"));

        branch.setIsActive(isActive);
        branchRepository.save(branch);

        log.info("Branch active status updated to: {}", isActive);
    }

    @Transactional
    public void deleteBranch(Long id) {
        log.info("Deleting branch with ID: {}", id);

        Branch branch = branchRepository.findByIdAndTenantId(id, TenantContext.getTenantId())
            .orElseThrow(() -> new NotFoundException("Branch not found"));

        // Check if branch has users assigned
        // if (branchRepository.hasUsersAssigned(id, branch.getTenantId())) {
        //     throw new BadRequestException("Cannot delete branch with users assigned");
        // }

        branch.setIsActive(false);
        branchRepository.save(branch);

        log.info("Branch soft deleted successfully");
    }

    @Transactional(readOnly = true)
    public List<BranchResponse> getBranchesByIds(List<Long> ids) {
        log.debug("Fetching branches by IDs: {}", ids);

        Long tenantId = TenantContext.getTenantId();
        List<Branch> branches = branchRepository.findByIdInAndTenantId(ids, tenantId);

        return branches.stream()
            .map(this::mapToResponse)
            .toList();
    }

    private BranchResponse mapToResponse(Branch branch) {
        return BranchResponse.builder()
            .id(branch.getId())
            .tenantId(branch.getTenantId())
            .name(branch.getName())
            .code(branch.getCode())
            .address(branch.getAddress())
            .phone(branch.getPhone())
            .managerName(branch.getManagerName())
            .isActive(branch.getIsActive())
            .createdAt(branch.getCreatedAt())
            .updatedAt(branch.getUpdatedAt())
            .build();
    }
}
