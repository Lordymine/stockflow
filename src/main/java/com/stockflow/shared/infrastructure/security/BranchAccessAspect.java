package com.stockflow.shared.infrastructure.security;

import com.stockflow.shared.domain.exception.ForbiddenException;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.reflect.MethodSignature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.Arrays;

/**
 * Aspect for validating branch access based on {@link BranchAccess} annotation.
 *
 * <p>This aspect intercepts methods annotated with {@code @BranchAccess} and
 * validates that the authenticated user has access to the specified branch ID.</p>
 *
 * <p>The branch ID can be:</p>
 * <ul>
 *   <li>A method parameter annotated with {@code @PathVariable} or {@code @RequestParam}</li>
 *   <li>A field in a request DTO</li>
 * </ul>
 */
@Aspect
@Component
public class BranchAccessAspect {

    private static final Logger logger = LoggerFactory.getLogger(BranchAccessAspect.class);

    /**
     * Validates branch access before method execution.
     *
     * @param joinPoint the join point
     */
    @Before("@annotation(com.stockflow.shared.infrastructure.security.BranchAccess)")
    public void validateBranchAccess(JoinPoint joinPoint) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !(authentication.getPrincipal() instanceof CustomUserDetails)) {
            logger.warn("Unauthorized access attempt - no valid authentication");
            throw new ForbiddenException("FORBIDDEN_RESOURCE_ACCESS",
                "Authentication required to access this resource");
        }

        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Method method = signature.getMethod();
        Parameter[] parameters = method.getParameters();
        Object[] args = joinPoint.getArgs();

        // Find branchId parameter
        Long branchId = extractBranchId(parameters, args);

        if (branchId == null) {
            logger.warn("Branch access validation failed - no branch ID found in method parameters");
            throw new ForbiddenException("FORBIDDEN_BRANCH_ACCESS",
                "Branch ID is required for this operation");
        }

        // Validate access
        if (!userDetails.hasAccessToBranch(branchId)) {
            logger.warn("User {} does not have access to branch {}",
                userDetails.getEmail(), branchId);
            throw new ForbiddenException("FORBIDDEN_BRANCH_ACCESS",
                String.format("You do not have access to branch %d", branchId));
        }

        logger.debug("Branch access validated: user {} has access to branch {}",
            userDetails.getEmail(), branchId);
    }

    /**
     * Extracts branch ID from method parameters.
     *
     * @param parameters the method parameters
     * @param args       the argument values
     * @return the branch ID, or null if not found
     */
    private Long extractBranchId(Parameter[] parameters, Object[] args) {
        for (int i = 0; i < parameters.length; i++) {
            Parameter parameter = parameters[i];
            Object arg = args[i];

            if (arg == null) {
                continue;
            }

            // Check if parameter is named branchId or has BranchAccess annotation
            if (parameter.getName().equals("branchId") ||
                parameter.getName().equals("sourceBranchId") ||
                parameter.getName().equals("destinationBranchId") ||
                Arrays.stream(parameter.getAnnotations())
                    .anyMatch(ann -> ann.annotationType().equals(BranchAccess.class))) {

                if (arg instanceof Long) {
                    return (Long) arg;
                }
            }
        }

        return null;
    }
}
