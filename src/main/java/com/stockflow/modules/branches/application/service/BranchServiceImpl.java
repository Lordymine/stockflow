package com.stockflow.modules.branches.application.service;

import com.stockflow.modules.branches.application.dto.BranchRequest;
import com.stockflow.modules.branches.application.dto.BranchResponse;
import com.stockflow.modules.branches.application.mapper.BranchMapper;
import com.stockflow.modules.branches.domain.model.Branch;
import com.stockflow.modules.branches.domain.repository.BranchRepository;
import com.stockflow.shared.domain.exception.ConflictException;
import com.stockflow.shared.domain.exception.ForbiddenException;
import com.stockflow.shared.domain.exception.NotFoundException;
import com.stockflow.shared.infrastructure.security.CustomUserDetails;
import com.stockflow.shared.infrastructure.security.TenantContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Implementation of branch management service.
 */
@Service
public class BranchServiceImpl implements BranchService {

    private static final Logger logger = LoggerFactory.getLogger(BranchServiceImpl.class);

    private final BranchRepository branchRepository;
    private final BranchMapper branchMapper;

    public BranchServiceImpl(BranchRepository branchRepository, BranchMapper branchMapper) {
        this.branchRepository = branchRepository;
        this.branchMapper = branchMapper;
    }

    @Override
    @Transactional
    public BranchResponse create(BranchRequest request) {
        Long tenantId = TenantContext.requireTenantId();
        String normalizedCode = request.code() != null ? request.code().trim().toUpperCase() : null;

        if (normalizedCode != null && branchRepository.existsByCodeAndTenantId(normalizedCode, tenantId)) {
            throw new ConflictException("BRANCH_CODE_ALREADY_EXISTS",
                "Branch code already exists in this tenant");
        }

        Branch branch = branchMapper.toEntity(request, tenantId);
        Branch saved = branchRepository.save(branch);

        logger.info("Branch created successfully: {}", saved.getId());

        return branchMapper.toResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<BranchResponse> list(Pageable pageable) {
        Long tenantId = TenantContext.requireTenantId();
        List<Long> branchIds = requireBranchAccess();

        Page<Branch> branches = branchRepository.findAllByTenantIdAndIdIn(tenantId, branchIds, pageable);
        return branches.map(branchMapper::toResponse);
    }

    @Override
    @Transactional
    public BranchResponse updateActive(Long branchId, Boolean isActive) {
        Long tenantId = TenantContext.requireTenantId();

        Branch branch = branchRepository.findByIdAndTenantIdIncludingInactive(branchId, tenantId)
            .orElseThrow(() -> new NotFoundException("BRANCH_NOT_FOUND",
                "Branch not found with ID: " + branchId));

        branch.setActive(isActive);
        Branch saved = branchRepository.save(branch);

        logger.info("Branch {} active status updated to {}", saved.getId(), saved.getIsActive());

        return branchMapper.toResponse(saved);
    }

    private List<Long> requireBranchAccess() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !(authentication.getPrincipal() instanceof CustomUserDetails userDetails)) {
            throw new ForbiddenException("FORBIDDEN_BRANCH_ACCESS",
                "Branch access is required for this operation");
        }

        List<Long> branchIds = userDetails.getBranchIds();
        if (branchIds == null || branchIds.isEmpty()) {
            throw new ForbiddenException("FORBIDDEN_BRANCH_ACCESS",
                "Branch access is required for this operation");
        }

        return branchIds;
    }
}
