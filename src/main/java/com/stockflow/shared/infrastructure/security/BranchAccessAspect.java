package com.stockflow.shared.infrastructure.security;

import com.stockflow.modules.users.application.service.BranchAccessService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Component;

@Aspect
@Component
@RequiredArgsConstructor
@Slf4j
public class BranchAccessAspect {

    private final BranchAccessService branchAccessService;

    @Before("@annotation(branchAccess)")
    public void validateBranchAccess(JoinPoint joinPoint, BranchAccess branchAccess) {
        String paramName = branchAccess.paramName();
        boolean throwIfDenied = branchAccess.throwIfDenied();

        log.debug("Validating branch access for parameter: {}", paramName);

        // Find the branchId parameter in method arguments
        Long branchId = findBranchIdParameter(joinPoint.getArgs(), (org.aspectj.lang.reflect.MethodSignature) joinPoint.getSignature(), paramName);

        if (branchId != null) {
            boolean hasAccess = branchAccessService.hasAccessToBranch(branchId);

            if (!hasAccess && throwIfDenied) {
                log.warn("Access denied to branch: {}", branchId);
                throw new AccessDeniedException("You do not have permission to access this branch");
            }

            if (!hasAccess) {
                log.debug("Access denied to branch: {} (not throwing exception)", branchId);
            }
        }
    }

    private Long findBranchIdParameter(Object[] args, org.aspectj.lang.reflect.MethodSignature signature, String paramName) {
        // Try to find by parameter name first
        String[] parameterNames = signature.getParameterNames();

        if (parameterNames != null) {
            for (int i = 0; i < parameterNames.length; i++) {
                if (parameterNames[i].equals(paramName) && args[i] instanceof Long) {
                    return (Long) args[i];
                }
            }
        }

        // Fallback: try to find the first Long parameter
        for (Object arg : args) {
            if (arg instanceof Long) {
                return (Long) arg;
            }
        }

        return null;
    }
}
