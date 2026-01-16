package com.stockflow.modules.tenant.infrastructure.web;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.stockflow.modules.tenant.application.dto.TenantRequest;
import com.stockflow.modules.tenant.domain.model.Tenant;
import com.stockflow.modules.tenant.domain.repository.TenantRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
@DisplayName("Tenant Controller Tests")
class TenantControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private TenantRepository repository;

    private Tenant tenant;

    @BeforeEach
    void setUp() {
        tenant = repository.save(Tenant.builder()
                .name("Test Tenant")
                .slug("test-tenant")
                .isActive(true)
                .build());
    }

    @Test
    @DisplayName("Deve criar tenant")
    void shouldCreateTenant() throws Exception {
        TenantRequest request = TenantRequest.builder()
                .name("New Tenant")
                .slug("new-tenant")
                .isActive(true)
                .build();

        mockMvc.perform(post("/api/v1/tenants")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.name").value("New Tenant"))
                .andExpect(jsonPath("$.data.slug").value("new-tenant"));
    }

    @Test
    @DisplayName("Deve buscar tenant por ID")
    void shouldFindById() throws Exception {
        mockMvc.perform(get("/api/v1/tenants/{id}", tenant.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.id").value(tenant.getId()))
                .andExpect(jsonPath("$.data.name").value("Test Tenant"));
    }

    @Test
    @DisplayName("Deve listar todos os tenants")
    void shouldListAll() throws Exception {
        mockMvc.perform(get("/api/v1/tenants"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data", hasSize(greaterThanOrEqualTo(1))));
    }

    @Test
    @DisplayName("Deve atualizar tenant")
    void shouldUpdateTenant() throws Exception {
        TenantRequest request = TenantRequest.builder()
                .name("Updated Tenant")
                .slug("updated-tenant")
                .isActive(true)
                .build();

        mockMvc.perform(put("/api/v1/tenants/{id}", tenant.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.name").value("Updated Tenant"));
    }

    @Test
    @DisplayName("Deve ativar/desativar tenant")
    void shouldToggleActive() throws Exception {
        mockMvc.perform(patch("/api/v1/tenants/{id}/active", tenant.getId())
                        .param("isActive", "false"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.isActive").value(false));
    }

    @Test
    @DisplayName("Deve deletar tenant")
    void shouldDeleteTenant() throws Exception {
        // Primeiro desativa
        tenant.setIsActive(false);
        repository.save(tenant);

        mockMvc.perform(delete("/api/v1/tenants/{id}", tenant.getId()))
                .andExpect(status().isNoContent());
    }

    @Test
    @DisplayName("Deve validar request")
    void shouldValidateRequest() throws Exception {
        TenantRequest request = TenantRequest.builder()
                .name("") // Nome inválido
                .slug("invalid slug!") // Slug inválido
                .build();

        mockMvc.perform(post("/api/v1/tenants")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }
}
