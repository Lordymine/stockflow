package com.stockflow.modules.users.application.service;

import com.stockflow.modules.users.domain.repository.UserBranchRepository;
import com.stockflow.shared.domain.exception.ForbiddenException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

/**
 * Service for validating branch access permissions.
 */
@Service
public class BranchAccessService {

    private static final Logger logger = LoggerFactory.getLogger(BranchAccessService.class);

    private final UserBranchRepository userBranchRepository;

    public BranchAccessService(UserBranchRepository userBranchRepository) {
        this.userBranchRepository = userBranchRepository;
    }

    /**
     * Validates that the current user has access to the specified branch.
     *
     * @param userId  the user ID
     * @param branchId the branch ID
     * @throws ForbiddenException if the user doesn't have access
     */
    public void validateBranchAccess(Long userId, Long branchId) {
        if (!userBranchRepository.existsByUserIdAndBranchId(userId, branchId)) {
            logger.warn("User {} does not have access to branch {}", userId, branchId);
            throw new ForbiddenException("FORBIDDEN_BRANCH_ACCESS",
                String.format("You do not have access to branch %d", branchId));
        }

        logger.debug("Validated branch access: user {} has access to branch {}", userId, branchId);
    }

    /**
     * Checks if the current user has access to the specified branch.
     *
     * @param userId  the user ID
     * @param branchId the branch ID
     * @return true if the user has access
     */
    public boolean hasBranchAccess(Long userId, Long branchId) {
        return userBranchRepository.existsByUserIdAndBranchId(userId, branchId);
    }
}
