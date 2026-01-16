package com.stockflow.shared.infrastructure.security;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Tenant Context Tests")
class TenantContextTest {

    @BeforeEach
    void setUp() {
        TenantContext.clear();
    }

    @AfterEach
    void tearDown() {
        TenantContext.clear();
    }

    @Test
    @DisplayName("Deve definir e recuperar tenant")
    void shouldSetAndGet() {
        TenantContext.setTenantId(123L);

        assertEquals(123L, TenantContext.getTenantId());
        assertTrue(TenantContext.isSet());
    }

    @Test
    @DisplayName("Deve retornar null quando não definido")
    void shouldReturnNullWhenNotSet() {
        assertNull(TenantContext.getTenantIdOrNull());
        assertFalse(TenantContext.isSet());
    }

    @Test
    @DisplayName("Deve lançar erro quando não definido")
    void shouldThrowWhenNotSet() {
        assertThrows(IllegalStateException.class, TenantContext::getTenantId);
    }

    @Test
    @DisplayName("Deve limpar contexto")
    void shouldClear() {
        TenantContext.setTenantId(123L);
        assertTrue(TenantContext.isSet());

        TenantContext.clear();
        assertFalse(TenantContext.isSet());
        assertNull(TenantContext.getTenantIdOrNull());
    }

    @Test
    @DisplayName("Deve funcionar com threads diferentes")
    void shouldWorkWithDifferentThreads() throws InterruptedException {
        TenantContext.setTenantId(1L);

        Thread thread1 = new Thread(() -> {
            TenantContext.setTenantId(2L);
            assertEquals(2L, TenantContext.getTenantId());
        });

        Thread thread2 = new Thread(() -> {
            TenantContext.setTenantId(3L);
            assertEquals(3L, TenantContext.getTenantId());
        });

        thread1.start();
        thread2.start();

        thread1.join();
        thread2.join();

        // Thread principal ainda com valor original
        assertEquals(1L, TenantContext.getTenantId());
    }
}
