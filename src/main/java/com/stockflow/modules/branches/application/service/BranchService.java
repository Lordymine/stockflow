package com.stockflow.modules.branches.application.service;

import com.stockflow.modules.branches.application.dto.BranchRequest;
import com.stockflow.modules.branches.application.dto.BranchResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

/**
 * Service interface for branch operations.
 */
public interface BranchService {

    BranchResponse create(BranchRequest request);

    Page<BranchResponse> list(Pageable pageable);

    BranchResponse updateActive(Long branchId, Boolean isActive);
}
