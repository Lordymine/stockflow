package com.stockflow.modules.users.domain.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("User Entity Tests")
class UserTest {

    @Test
    @DisplayName("Deve criar user com builder")
    void shouldCreateUserWithBuilder() {
        User user = User.builder()
            .name("João Silva")
            .email("joao@stockflow.com")
            .password("hashed_password")
            .isActive(true)
            .build();

        assertNotNull(user);
        assertEquals("João Silva", user.getName());
        assertEquals("joao@stockflow.com", user.getEmail());
    }

    @Test
    @DisplayName("Deve verificar se user tem role")
    void shouldCheckIfUserHasRole() {
        User user = User.builder()
            .build();

        Role adminRole = Role.builder()
            .name(RoleEnum.ADMIN)
            .build();

        UserRole userRole = UserRole.builder()
            .user(user)
            .role(adminRole)
            .assignedAt(LocalDateTime.now())
            .build();

        user.addRole(userRole);

        assertTrue(user.hasRole(RoleEnum.ADMIN));
        assertFalse(user.hasRole(RoleEnum.STAFF));
    }

    @Test
    @DisplayName("Deve incrementar tentativas falhas de login")
    void shouldIncrementFailedLoginAttempts() {
        User user = User.builder()
            .failedLoginAttempts(0)
            .isAccountLocked(false)
            .build();

        user.incrementFailedLoginAttempts();
        assertEquals(1, user.getFailedLoginAttempts());
        assertFalse(user.getIsAccountLocked());

        // Simular 5 tentativas
        for (int i = 1; i < 5; i++) {
            user.incrementFailedLoginAttempts();
        }

        assertEquals(5, user.getFailedLoginAttempts());
        assertTrue(user.getIsAccountLocked());
    }

    @Test
    @DisplayName("Deve resetar tentativas falhas de login")
    void shouldResetFailedLoginAttempts() {
        User user = User.builder()
            .failedLoginAttempts(5)
            .isAccountLocked(true)
            .build();

        user.resetFailedLoginAttempts();

        assertEquals(0, user.getFailedLoginAttempts());
        assertFalse(user.getIsAccountLocked());
    }

    @Test
    @DisplayName("Deve verificar se conta está bloqueada")
    void shouldCheckIfAccountIsLocked() {
        User user = User.builder()
            .isAccountLocked(true)
            .build();

        assertTrue(user.isLocked());
    }

    @Test
    @DisplayName("Deve retornar todas as roles do user")
    void shouldReturnAllUserRoles() {
        User user = User.builder()
            .build();

        Role adminRole = Role.builder()
            .name(RoleEnum.ADMIN)
            .build();

        Role staffRole = Role.builder()
            .name(RoleEnum.STAFF)
            .build();

        UserRole adminUserRole = UserRole.builder()
            .user(user)
            .role(adminRole)
            .assignedAt(LocalDateTime.now())
            .build();

        UserRole staffUserRole = UserRole.builder()
            .user(user)
            .role(staffRole)
            .assignedAt(LocalDateTime.now())
            .build();

        user.addRole(adminUserRole);
        user.addRole(staffUserRole);

        Set<RoleEnum> roles = user.getRoles();

        assertEquals(2, roles.size());
        assertTrue(roles.contains(RoleEnum.ADMIN));
        assertTrue(roles.contains(RoleEnum.STAFF));
    }
}
