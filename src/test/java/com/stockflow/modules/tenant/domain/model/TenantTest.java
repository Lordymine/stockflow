package com.stockflow.modules.tenant.domain.model;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Tenant Entity Tests")
class TenantTest {

    private Tenant tenant;

    @BeforeEach
    void setUp() {
        tenant = Tenant.builder()
                .id(1L)
                .name("Demo Company")
                .slug("demo-company")
                .isActive(true)
                .createdAt(LocalDateTime.now())
                .build();
    }

    @Test
    @DisplayName("Deve criar tenant com builder")
    void shouldCreateTenantWithBuilder() {
        assertNotNull(tenant);
        assertEquals("Demo Company", tenant.getName());
        assertEquals("demo-company", tenant.getSlug());
        assertTrue(tenant.isActive());
    }

    @Test
    @DisplayName("Deve ativar tenant")
    void shouldActivateTenant() {
        tenant.setIsActive(false);
        assertFalse(tenant.isActive());

        tenant.activate();
        assertTrue(tenant.isActive());
    }

    @Test
    @DisplayName("Deve desativar tenant")
    void shouldDeactivateTenant() {
        tenant.deactivate();
        assertFalse(tenant.isActive());
    }

    @Test
    @DisplayName("Deve comparar tenants pelo id")
    void shouldCompareTenantsById() {
        Tenant tenant2 = Tenant.builder()
                .id(1L)
                .name("Different Name")
                .slug("different-slug")
                .build();

        assertEquals(tenant, tenant2);
        assertEquals(tenant.hashCode(), tenant2.hashCode());
    }

    @Test
    @DisplayName("Deve ter toString sem dados sensíveis")
    void shouldHaveToString() {
        String toString = tenant.toString();

        assertTrue(toString.contains("Demo Company"));
        assertTrue(toString.contains("demo-company"));
        assertFalse(toString.contains("password"));
    }
}
