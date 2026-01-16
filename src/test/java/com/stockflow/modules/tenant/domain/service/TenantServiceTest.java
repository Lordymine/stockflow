package com.stockflow.modules.tenant.domain.service;

import com.stockflow.modules.tenant.application.dto.TenantRequest;
import com.stockflow.modules.tenant.application.dto.TenantResponse;
import com.stockflow.modules.tenant.application.mapper.TenantMapper;
import com.stockflow.modules.tenant.domain.model.Tenant;
import com.stockflow.modules.tenant.domain.repository.TenantRepository;
import com.stockflow.shared.domain.exception.ConflictException;
import com.stockflow.shared.domain.exception.NotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Tenant Service Tests")
class TenantServiceTest {

    @Mock
    private TenantRepository repository;

    @Mock
    private TenantMapper mapper;

    @InjectMocks
    private TenantServiceImpl service;

    private Tenant tenant;
    private TenantRequest request;

    @BeforeEach
    void setUp() {
        tenant = Tenant.builder()
                .id(1L)
                .name("Demo Company")
                .slug("demo-company")
                .isActive(true)
                .build();

        request = TenantRequest.builder()
                .name("Demo Company")
                .slug("demo-company")
                .isActive(true)
                .build();
    }

    @Test
    @DisplayName("Deve criar tenant com sucesso")
    void shouldCreateTenant() {
        when(repository.existsBySlug("demo-company")).thenReturn(false);
        when(mapper.toEntity(request)).thenReturn(tenant);
        when(repository.save(any(Tenant.class))).thenReturn(tenant);
        when(mapper.toResponse(tenant)).thenReturn(
            TenantResponse.builder()
                .id(1L)
                .name("Demo Company")
                .slug("demo-company")
                .isActive(true)
                .build()
        );

        TenantResponse response = service.create(request);

        assertNotNull(response);
        assertEquals("Demo Company", response.name());
        verify(repository).save(any(Tenant.class));
    }

    @Test
    @DisplayName("Deve lançar erro quando slug duplicado")
    void shouldThrowErrorWhenDuplicateSlug() {
        when(repository.existsBySlug("demo-company")).thenReturn(true);

        assertThrows(ConflictException.class, () -> service.create(request));
        verify(repository, never()).save(any(Tenant.class));
    }

    @Test
    @DisplayName("Deve buscar tenant por ID")
    void shouldFindById() {
        when(repository.findById(1L)).thenReturn(Optional.of(tenant));
        when(mapper.toResponse(tenant)).thenReturn(
            TenantResponse.builder()
                .id(1L)
                .name("Demo Company")
                .slug("demo-company")
                .isActive(true)
                .build()
        );

        TenantResponse response = service.findById(1L);

        assertNotNull(response);
        assertEquals("Demo Company", response.name());
    }

    @Test
    @DisplayName("Deve lançar erro quando não encontrado")
    void shouldThrowWhenNotFound() {
        when(repository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> service.findById(1L));
    }
}
