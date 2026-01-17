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
import java.util.LinkedHashSet;
import java.util.Set;

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

        // Find branchIds in parameters or request payloads
        Set<Long> branchIds = extractBranchIds(parameters, args);

        if (branchIds.isEmpty()) {
            logger.warn("Branch access validation failed - no branch ID found in method parameters");
            throw new ForbiddenException("FORBIDDEN_BRANCH_ACCESS",
                "Branch ID is required for this operation");
        }

        // Validate access to every referenced branch
        for (Long branchId : branchIds) {
            if (!userDetails.hasAccessToBranch(branchId)) {
                logger.warn("User {} does not have access to branch {}",
                    userDetails.getEmail(), branchId);
                throw new ForbiddenException("FORBIDDEN_BRANCH_ACCESS",
                    String.format("You do not have access to branch %d", branchId));
            }
        }

        logger.debug("Branch access validated for user {} on branches {}",
            userDetails.getEmail(), branchIds);
    }

    /**
     * Extracts branch IDs from method parameters or request payloads.
     *
     * @param parameters the method parameters
     * @param args       the argument values
     * @return set of branch IDs (empty if none found)
     */
    private Set<Long> extractBranchIds(Parameter[] parameters, Object[] args) {
        Set<Long> branchIds = new LinkedHashSet<>();

        for (int i = 0; i < parameters.length; i++) {
            Parameter parameter = parameters[i];
            Object arg = args[i];

            if (arg == null) {
                continue;
            }

            // Check if parameter is explicitly annotated with BranchAccess
            boolean annotated = Arrays.stream(parameter.getAnnotations())
                .anyMatch(ann -> ann.annotationType().equals(BranchAccess.class));

            if (arg instanceof Long && annotated) {
                branchIds.add((Long) arg);
                continue;
            }

            // Fallback: check common parameter names when available
            if (arg instanceof Long &&
                (parameter.getName().equals("branchId") ||
                 parameter.getName().equals("sourceBranchId") ||
                 parameter.getName().equals("destinationBranchId"))) {
                branchIds.add((Long) arg);
                continue;
            }

            // Inspect request payloads for branch identifiers
            branchIds.addAll(extractBranchIdsFromPayload(arg));
        }

        return branchIds;
    }

    private Set<Long> extractBranchIdsFromPayload(Object payload) {
        Set<Long> branchIds = new LinkedHashSet<>();
        extractBranchIdFromAccessor(payload, "branchId", branchIds);
        extractBranchIdFromAccessor(payload, "getBranchId", branchIds);
        extractBranchIdFromAccessor(payload, "sourceBranchId", branchIds);
        extractBranchIdFromAccessor(payload, "getSourceBranchId", branchIds);
        extractBranchIdFromAccessor(payload, "destinationBranchId", branchIds);
        extractBranchIdFromAccessor(payload, "getDestinationBranchId", branchIds);
        return branchIds;
    }

    private void extractBranchIdFromAccessor(Object payload, String accessor, Set<Long> branchIds) {
        try {
            Method method = payload.getClass().getMethod(accessor);
            Object value = method.invoke(payload);
            if (value instanceof Long) {
                branchIds.add((Long) value);
            }
        } catch (NoSuchMethodException ignored) {
            // Ignore missing accessors
        } catch (Exception ex) {
            logger.debug("Failed to extract branch ID from payload accessor {}", accessor, ex);
        }
    }
}
