package com.stockflow.modules.users.application.service;

import com.stockflow.modules.users.domain.model.RoleEnum;
import com.stockflow.modules.users.domain.model.User;
import com.stockflow.modules.users.domain.repository.UserRepository;
import com.stockflow.shared.domain.exception.ForbiddenException;
import com.stockflow.shared.domain.exception.NotFoundException;
import com.stockflow.shared.infrastructure.security.TenantContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class BranchAccessService {

    private final UserRepository userRepository;

    /**
     * Obtém branches acessíveis pelo usuário atual
     */
    public Set<Long> getCurrentUserAccessibleBranches() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated()) {
            throw new ForbiddenException("User not authenticated");
        }

        String email = authentication.getName();
        User user = userRepository.findByEmail(email)
            .orElseThrow(() -> new NotFoundException("User not found"));

        // Admin tem acesso a todas as branches (será filtrado pela query)
        boolean isAdmin = user.getUserRoles().stream()
            .anyMatch(ur -> ur.getRole().getName() == RoleEnum.ADMIN);

        if (isAdmin) {
            return Set.of(-1L); // Marcador especial para "todas as branches"
        }

        // Retorna branches atribuídas ao usuário
        return user.getUserBranches().stream()
            .map(ub -> ub.getBranchId())
            .collect(Collectors.toSet());
    }

    /**
     * Valida se o usuário atual tem acesso a uma branch específica
     */
    public void validateBranchAccess(Long branchId) {
        Set<Long> accessibleBranches = getCurrentUserAccessibleBranches();

        // -1L significa admin tem acesso a todas as branches
        if (accessibleBranches.contains(-1L)) {
            return;
        }

        if (!accessibleBranches.contains(branchId)) {
            log.warn("User attempted to access branch without permission: {}", branchId);
            throw new ForbiddenException("Access denied to branch: " + branchId);
        }
    }

    /**
     * Verifica se o usuário atual tem acesso a uma branch específica
     */
    public boolean hasAccessToBranch(Long branchId) {
        try {
            validateBranchAccess(branchId);
            return true;
        } catch (ForbiddenException e) {
            return false;
        }
    }

    /**
     * Obtém branches acessíveis por um usuário específico
     */
    public Set<Long> getUserAccessibleBranches(Long userId) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new NotFoundException("User not found"));

        validateTenantAccess(user.getTenantId());

        // Admin tem acesso a todas
        boolean isAdmin = user.getUserRoles().stream()
            .anyMatch(ur -> ur.getRole().getName() == RoleEnum.ADMIN);

        if (isAdmin) {
            return Set.of(-1L);
        }

        return user.getUserBranches().stream()
            .map(ub -> ub.getBranchId())
            .collect(Collectors.toSet());
    }

    private void validateTenantAccess(Long userTenantId) {
        Long currentTenantId = TenantContext.getTenantId();
        if (!userTenantId.equals(currentTenantId)) {
            throw new ForbiddenException("Access denied: user belongs to different tenant");
        }
    }
}
