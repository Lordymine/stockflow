package com.stockflow.modules.branches.domain.repository;

import com.stockflow.modules.branches.domain.model.Branch;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@ActiveProfiles("test")
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
@DisplayName("Branch Repository Tests")
class BranchRepositoryTest {

    @Autowired
    private BranchRepository branchRepository;

    @Autowired
    private TestEntityManager entityManager;

    private Branch testBranch;

    @BeforeEach
    void setUp() {
        entityManager.clear();
        testBranch = Branch.builder()
            .tenantId(1L)
            .name("Test Branch")
            .code("TST")
            .isActive(true)
            .build();
    }

    @Test
    @DisplayName("Deve salvar branch")
    void shouldSaveBranch() {
        Branch saved = branchRepository.save(testBranch);

        assertNotNull(saved.getId());
        assertEquals("Test Branch", saved.getName());
    }

    @Test
    @DisplayName("Deve buscar branch por id e tenant")
    void shouldFindBranchByIdAndTenantId() {
        Branch saved = branchRepository.save(testBranch);

        java.util.Optional<Branch> found = branchRepository.findByIdAndTenantId(saved.getId(), 1L);

        assertTrue(found.isPresent());
        assertEquals("Test Branch", found.get().getName());
    }

    @Test
    @DisplayName("Deve buscar branches por tenant")
    void shouldFindBranchesByTenantId() {
        branchRepository.save(testBranch);

        Branch branch2 = Branch.builder()
            .tenantId(1L)
            .name("Branch 2")
            .code("BR2")
            .build();
        branchRepository.save(branch2);

        List<Branch> branches = branchRepository.findByTenantId(1L);

        assertEquals(2, branches.size());
    }

    @Test
    @DisplayName("Deve verificar existência de código por tenant")
    void shouldCheckCodeExistsByTenant() {
        branchRepository.save(testBranch);

        boolean exists = branchRepository.existsByCodeAndTenantId("TST", 1L);

        assertTrue(exists);
    }

    @Test
    @DisplayName("Deve buscar apenas branches ativos")
    void shouldFindOnlyActiveBranches() {
        testBranch.setIsActive(false);
        branchRepository.save(testBranch);

        Branch activeBranch = Branch.builder()
            .tenantId(1L)
            .name("Active Branch")
            .code("ACT")
            .isActive(true)
            .build();
        branchRepository.save(activeBranch);

        List<Branch> activeBranches = branchRepository.findByTenantIdAndIsActiveTrue(1L);

        assertEquals(1, activeBranches.size());
        assertEquals("Active Branch", activeBranches.get(0).getName());
    }

    @Test
    @DisplayName("Deve paginar branches")
    void shouldPaginateBranches() {
        branchRepository.save(testBranch);

        Branch branch2 = Branch.builder()
            .tenantId(1L)
            .name("Branch 2")
            .code("BR2")
            .isActive(true)
            .build();
        branchRepository.save(branch2);

        Page<Branch> page = branchRepository.findByTenantId(1L, PageRequest.of(0, 2));

        assertEquals(2, page.getContent().size());
        assertEquals(2, page.getTotalElements());
        assertEquals(1, page.getTotalPages());
    }

    @Test
    @DisplayName("Deve buscar por nome ou código")
    void shouldSearchByNameOrCode() {
        branchRepository.save(testBranch);

        Branch branch2 = Branch.builder()
            .tenantId(1L)
            .name("North Branch")
            .code("NOR")
            .isActive(true)
            .build();
        branchRepository.save(branch2);

        Page<Branch> results = branchRepository.searchActiveBranches(
            1L, "TST", PageRequest.of(0, 10)
        );

        assertEquals(1, results.getTotalElements());
        assertEquals("Test Branch", results.getContent().get(0).getName());
    }

    @Test
    @DisplayName("Deve contar branches por tenant")
    void shouldCountBranchesByTenant() {
        branchRepository.save(testBranch);

        Branch branch2 = Branch.builder()
            .tenantId(1L)
            .name("Branch 2")
            .code("BR2")
            .isActive(true)
            .build();
        branchRepository.save(branch2);

        long total = branchRepository.countByTenantId(1L);
        long active = branchRepository.countByTenantIdAndIsActiveTrue(1L);

        assertEquals(2, total);
        assertEquals(2, active);
    }

    @Test
    @DisplayName("Deve buscar branches por lista de IDs")
    void shouldFindBranchesByIdIn() {
        Branch branch1 = branchRepository.save(testBranch);

        Branch branch2 = Branch.builder()
            .tenantId(1L)
            .name("Branch 2")
            .code("BR2")
            .isActive(true)
            .build();
        branchRepository.save(branch2);

        List<Branch> branches = branchRepository.findByIdInAndTenantId(
            List.of(branch1.getId(), branch2.getId()), 1L
        );

        assertEquals(2, branches.size());
    }
}
