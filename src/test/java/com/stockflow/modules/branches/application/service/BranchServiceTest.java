package com.stockflow.modules.branches.application.service;

import com.stockflow.modules.branches.application.dto.*;
import com.stockflow.modules.branches.domain.model.Branch;
import com.stockflow.modules.branches.domain.repository.BranchRepository;
import com.stockflow.shared.domain.exception.BadRequestException;
import com.stockflow.shared.domain.exception.NotFoundException;
import com.stockflow.shared.infrastructure.security.TenantContext;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Branch Service Tests")
class BranchServiceTest {

    @Mock
    private BranchRepository branchRepository;

    @InjectMocks
    private BranchService branchService;

    private Branch testBranch;
    private MockedStatic<TenantContext> mockedTenantContext;

    @BeforeEach
    void setUp() {
        testBranch = Branch.builder()
            .id(1L)
            .tenantId(1L)
            .name("Test Branch")
            .code("TST")
            .isActive(true)
            .build();

        // Mock TenantContext
        mockedTenantContext = mockStatic(TenantContext.class);
        mockedTenantContext.when(TenantContext::getTenantId).thenReturn(1L);
    }

    @AfterEach
    void tearDown() {
        if (mockedTenantContext != null) {
            mockedTenantContext.close();
        }
    }

    @Test
    @DisplayName("Deve criar branch com sucesso")
    void shouldCreateBranchSuccessfully() {
        CreateBranchRequest request = CreateBranchRequest.builder()
            .name("New Branch")
            .code("NEW")
            .address("Address 123")
            .build();

        when(branchRepository.existsByCodeAndTenantId(anyString(), anyLong())).thenReturn(false);
        when(branchRepository.save(any(Branch.class))).thenReturn(testBranch);

        BranchResponse response = branchService.createBranch(request);

        assertNotNull(response);
        verify(branchRepository).save(any(Branch.class));
    }

    @Test
    @DisplayName("Deve lançar exceção quando código já existe")
    void shouldThrowExceptionWhenCodeExists() {
        CreateBranchRequest request = CreateBranchRequest.builder()
            .name("New Branch")
            .code("TST")
            .build();

        when(branchRepository.existsByCodeAndTenantId(anyString(), anyLong())).thenReturn(true);

        assertThrows(BadRequestException.class, () -> {
            branchService.createBranch(request);
        });

        verify(branchRepository, never()).save(any(Branch.class));
    }

    @Test
    @DisplayName("Deve buscar branch por ID")
    void shouldGetBranchById() {
        when(branchRepository.findByIdAndTenantId(anyLong(), anyLong()))
            .thenReturn(Optional.of(testBranch));

        BranchResponse response = branchService.getBranchById(1L);

        assertNotNull(response);
        assertEquals("Test Branch", response.name());
    }

    @Test
    @DisplayName("Deve lançar exceção quando branch não encontrado")
    void shouldThrowExceptionWhenBranchNotFound() {
        when(branchRepository.findByIdAndTenantId(anyLong(), anyLong()))
            .thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> {
            branchService.getBranchById(999L);
        });
    }

    @Test
    @DisplayName("Deve atualizar branch")
    void shouldUpdateBranch() {
        UpdateBranchRequest request = UpdateBranchRequest.builder()
            .name("Updated Name")
            .address("Updated Address")
            .build();

        when(branchRepository.findByIdAndTenantId(anyLong(), anyLong()))
            .thenReturn(Optional.of(testBranch));
        when(branchRepository.save(any(Branch.class))).thenReturn(testBranch);

        BranchResponse response = branchService.updateBranch(1L, request);

        assertNotNull(response);
        verify(branchRepository).save(any(Branch.class));
    }

    @Test
    @DisplayName("Deve ativar/desativar branch")
    void shouldToggleBranchActive() {
        when(branchRepository.findByIdAndTenantId(anyLong(), anyLong()))
            .thenReturn(Optional.of(testBranch));
        when(branchRepository.save(any(Branch.class))).thenReturn(testBranch);

        branchService.toggleBranchActive(1L, false);

        verify(branchRepository).save(any(Branch.class));
        assertFalse(testBranch.getIsActive());
    }

    @Test
    @DisplayName("Deve deletar branch (soft delete)")
    void shouldDeleteBranch() {
        when(branchRepository.findByIdAndTenantId(anyLong(), anyLong()))
            .thenReturn(Optional.of(testBranch));
        when(branchRepository.save(any(Branch.class))).thenReturn(testBranch);

        branchService.deleteBranch(1L);

        verify(branchRepository).save(any(Branch.class));
        assertFalse(testBranch.getIsActive());
    }

    @Test
    @DisplayName("Deve listar branches com paginação")
    void shouldListBranchesWithPagination() {
        Page<Branch> page = new PageImpl<>(List.of(testBranch));
        when(branchRepository.findByTenantId(anyLong(), any())).thenReturn(page);

        Page<BranchResponse> result = branchService.getAllBranches(PageRequest.of(0, 20));

        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
    }

    @Test
    @DisplayName("Deve listar branches ativas")
    void shouldListActiveBranches() {
        Page<Branch> page = new PageImpl<>(List.of(testBranch));
        when(branchRepository.findByTenantIdAndIsActiveTrue(anyLong(), any())).thenReturn(page);

        Page<BranchResponse> result = branchService.getActiveBranches(PageRequest.of(0, 20));

        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
    }

    @Test
    @DisplayName("Deve buscar branches por termo")
    void shouldSearchBranches() {
        Page<Branch> page = new PageImpl<>(List.of(testBranch));
        when(branchRepository.searchActiveBranches(anyLong(), anyString(), any())).thenReturn(page);

        Page<BranchResponse> result = branchService.searchBranches("test", PageRequest.of(0, 20));

        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
    }

    @Test
    @DisplayName("Deve buscar branches por IDs")
    void shouldGetBranchesByIds() {
        when(branchRepository.findByIdInAndTenantId(anyList(), anyLong()))
            .thenReturn(List.of(testBranch));

        List<BranchResponse> result = branchService.getBranchesByIds(List.of(1L));

        assertNotNull(result);
        assertEquals(1, result.size());
    }
}
