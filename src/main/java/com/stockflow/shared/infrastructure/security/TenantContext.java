package com.stockflow.shared.infrastructure.security;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Contexto do Tenant usando ThreadLocal para armazenar o tenant atual.
 *
 * ThreadLocal garante que cada thread tenha sua própria cópia do tenant_id,
 * evitando problemas de concorrência em aplicações web.
 */
@Slf4j
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class TenantContext {

    private static final ThreadLocal<Long> CURRENT_TENANT = new ThreadLocal<>();

    /**
     * Define o tenant atual para a thread.
     */
    public static void setTenantId(Long tenantId) {
        log.debug("Setting tenant context: {}", tenantId);
        CURRENT_TENANT.set(tenantId);
    }

    /**
     * Retorna o tenant atual da thread.
     */
    public static Long getTenantId() {
        Long tenantId = CURRENT_TENANT.get();
        if (tenantId == null) {
            throw new IllegalStateException("Tenant context not set");
        }
        return tenantId;
    }

    /**
     * Retorna o tenant atual ou null se não definido.
     */
    public static Long getTenantIdOrNull() {
        return CURRENT_TENANT.get();
    }

    /**
     * Limpa o contexto da thread.
     */
    public static void clear() {
        log.debug("Clearing tenant context");
        CURRENT_TENANT.remove();
    }

    /**
     * Verifica se o contexto está definido.
     */
    public static boolean isSet() {
        return CURRENT_TENANT.get() != null;
    }
}
