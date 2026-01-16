package com.stockflow.modules.branches.domain.model;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Branch Entity Tests")
class BranchTest {

    private Branch branch;

    @BeforeEach
    void setUp() {
        branch = Branch.builder()
            .id(1L)
            .tenantId(1L)
            .name("Matrix Headquarters")
            .code("MAT")
            .address("Av. Paulista 1000")
            .phone("+55 11 1234-5678")
            .managerName("John Manager")
            .isActive(true)
            .createdAt(LocalDateTime.now())
            .build();
    }

    @Test
    @DisplayName("Deve criar branch com builder")
    void shouldCreateBranchWithBuilder() {
        assertNotNull(branch);
        assertEquals("Matrix Headquarters", branch.getName());
        assertEquals("MAT", branch.getCode());
        assertEquals(1L, branch.getTenantId());
    }

    @Test
    @DisplayName("Deve ter isActive true por padrão")
    void shouldHaveActiveTrueByDefault() {
        Branch newBranch = Branch.builder()
            .tenantId(1L)
            .name("New Branch")
            .code("NEW")
            .build();

        assertTrue(newBranch.getIsActive());
    }

    @Test
    @DisplayName("Deve atualizar updatedAt no preUpdate")
    void shouldUpdateUpdatedAtOnPreUpdate() {
        branch.onUpdate();
        assertNotNull(branch.getUpdatedAt());
    }

    @Test
    @DisplayName("Deve implementar equals corretamente")
    void shouldImplementEqualsCorrectly() {
        Branch branch1 = Branch.builder().id(1L).build();
        Branch branch2 = Branch.builder().id(1L).build();
        Branch branch3 = Branch.builder().id(2L).build();

        assertEquals(branch1, branch2);
        assertNotEquals(branch1, branch3);
    }

    @Test
    @DisplayName("Deve implementar hashCode corretamente")
    void shouldImplementHashCodeCorrectly() {
        Branch branch1 = Branch.builder().id(1L).build();
        Branch branch2 = Branch.builder().id(1L).build();

        assertEquals(branch1.hashCode(), branch2.hashCode());
    }
}
