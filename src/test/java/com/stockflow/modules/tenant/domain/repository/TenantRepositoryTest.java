package com.stockflow.modules.tenant.domain.repository;

import com.stockflow.modules.tenant.domain.model.Tenant;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
@DisplayName("Tenant Repository Tests")
class TenantRepositoryTest {

    @Autowired
    private TenantRepository tenantRepository;

    private Tenant tenant1;
    private Tenant tenant2;

    @BeforeEach
    void setUp() {
        tenant1 = Tenant.builder()
                .name("Company One")
                .slug("company-one")
                .isActive(true)
                .build();

        tenant2 = Tenant.builder()
                .name("Company Two")
                .slug("company-two")
                .isActive(false)
                .build();

        tenant1 = tenantRepository.save(tenant1);
        tenant2 = tenantRepository.save(tenant2);
    }

    @Test
    @DisplayName("Deve buscar tenant por slug")
    void shouldFindBySlug() {
        Optional<Tenant> found = tenantRepository.findBySlug("company-one");

        assertTrue(found.isPresent());
        assertEquals("Company One", found.get().getName());
    }

    @Test
    @DisplayName("Deve buscar apenas tenants ativos")
    void shouldFindOnlyActiveTenants() {
        List<Tenant> active = tenantRepository.findByIsActiveTrue();

        assertEquals(1, active.size());
        assertTrue(active.get(0).isActive());
    }

    @Test
    @DisplayName("Deve verificar se slug existe")
    void shouldCheckSlugExists() {
        assertTrue(tenantRepository.existsBySlug("company-one"));
        assertFalse(tenantRepository.existsBySlug("non-existent"));
    }

    @Test
    @DisplayName("Deve verificar se slug existe ignorando id")
    void shouldCheckSlugExistsIgnoringId() {
        // Slug existe, mas é o mesmo registro
        assertFalse(tenantRepository.existsBySlugAndIdNot("company-one", tenant1.getId()));

        // Slug existe em outro registro
        assertTrue(tenantRepository.existsBySlugAndIdNot("company-two", tenant1.getId()));
    }

    @Test
    @DisplayName("Deve buscar por nome contendo string")
    void shouldFindByNameContaining() {
        List<Tenant> found = tenantRepository.findByNameContainingIgnoreCase("company");

        assertEquals(2, found.size());
    }

    @Test
    @DisplayName("Deve contar tenants ativos")
    void shouldCountActiveTenants() {
        long count = tenantRepository.countByIsActiveTrue();

        assertEquals(1, count);
    }

    @Test
    @DisplayName("Deve criar e buscar tenant")
    void shouldCreateAndFindTenant() {
        Tenant newTenant = Tenant.builder()
                .name("New Company")
                .slug("new-company")
                .isActive(true)
                .build();

        Tenant saved = tenantRepository.save(newTenant);

        assertNotNull(saved.getId());

        Optional<Tenant> found = tenantRepository.findById(saved.getId());
        assertTrue(found.isPresent());
        assertEquals("New Company", found.get().getName());
    }

    @Test
    @DisplayName("Deve atualizar tenant")
    void shouldUpdateTenant() {
        tenant1.setName("Updated Name");

        Tenant saved = tenantRepository.save(tenant1);

        assertEquals("Updated Name", saved.getName());
    }

    @Test
    @DisplayName("Deve deletar tenant")
    void shouldDeleteTenant() {
        Long id = tenant1.getId();

        tenantRepository.deleteById(id);

        Optional<Tenant> found = tenantRepository.findById(id);
        assertFalse(found.isPresent());
    }
}
