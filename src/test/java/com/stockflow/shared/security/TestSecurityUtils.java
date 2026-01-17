package com.stockflow.shared.security;

import com.stockflow.modules.users.domain.model.RoleEnum;
import com.stockflow.shared.infrastructure.security.CustomUserDetails;
import com.stockflow.shared.infrastructure.security.TenantContext;
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors;
import org.springframework.test.web.servlet.request.RequestPostProcessor;

import java.util.List;
import java.util.Set;

/**
 * Helper methods for building authenticated requests in tests.
 */
public final class TestSecurityUtils {

    private TestSecurityUtils() {
    }

    public static RequestPostProcessor admin(Long tenantId, List<Long> branchIds) {
        return user(1L, tenantId, "admin@stockflow.test", Set.of(RoleEnum.ADMIN), branchIds);
    }

    public static RequestPostProcessor manager(Long tenantId, List<Long> branchIds) {
        return user(2L, tenantId, "manager@stockflow.test", Set.of(RoleEnum.MANAGER), branchIds);
    }

    public static RequestPostProcessor staff(Long tenantId, List<Long> branchIds) {
        return user(3L, tenantId, "staff@stockflow.test", Set.of(RoleEnum.STAFF), branchIds);
    }

    public static RequestPostProcessor user(Long userId, Long tenantId, String email,
                                            Set<RoleEnum> roles, List<Long> branchIds) {
        CustomUserDetails userDetails = new CustomUserDetails(
            userId,
            tenantId,
            email,
            "password",
            roles,
            branchIds
        );
        RequestPostProcessor authentication = SecurityMockMvcRequestPostProcessors.user(userDetails);
        return request -> {
            TenantContext.setTenantId(tenantId);
            return authentication.postProcessRequest(request);
        };
    }
}
