package com.stockflow.shared.infrastructure.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * Filter to clean up tenant context at the end of each request.
 *
 * <p>This filter runs AFTER all request processing to ensure the ThreadLocal
 * tenant context is cleared, preventing thread contamination in thread pool environments.</p>
 *
 * <p>This filter has the highest order precedence to ensure it runs last.</p>
 */
@Component
@Order(Ordered.LOWEST_PRECEDENCE)
public class TenantCleanupFilter extends OncePerRequestFilter {

    private static final Logger logger = LoggerFactory.getLogger(TenantCleanupFilter.class);

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                   HttpServletResponse response,
                                   FilterChain filterChain) throws ServletException, IOException {
        try {
            filterChain.doFilter(request, response);
        } finally {
            // Always clean up the tenant context, even if an exception occurred
            if (TenantContext.hasTenant()) {
                Long tenantId = TenantContext.getTenantId();
                TenantContext.clear();
                logger.trace("Cleared tenant context for tenant: {}", tenantId);
            }
        }
    }
}
