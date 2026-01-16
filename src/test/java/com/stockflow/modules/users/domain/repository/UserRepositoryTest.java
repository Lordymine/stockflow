package com.stockflow.modules.users.domain.repository;

import com.stockflow.modules.users.domain.model.Role;
import com.stockflow.modules.users.domain.model.RoleEnum;
import com.stockflow.modules.users.domain.model.User;
import com.stockflow.modules.users.domain.model.UserRole;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@ActiveProfiles("test")
@DisplayName("User Repository Tests")
class UserRepositoryTest {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private UserRoleRepository userRoleRepository;

    @Autowired
    private TestEntityManager entityManager;

    private User user1;
    private User user2;
    private Role adminRole;

    @BeforeEach
    void setUp() {
        // Clean
        userRepository.deleteAll();
        roleRepository.deleteAll();

        // Create roles
        adminRole = new Role();
        adminRole.setName(RoleEnum.ADMIN);
        adminRole.setDescription("Administrator");
        adminRole.setTenantId(1L);
        adminRole = roleRepository.save(adminRole);

        // Create users
        user1 = new User();
        user1.setName("User One");
        user1.setEmail("user1@stockflow.com");
        user1.setPassword("password1");
        user1.setTenantId(1L);
        user1.setIsActive(true);

        user2 = new User();
        user2.setName("User Two");
        user2.setEmail("user2@stockflow.com");
        user2.setPassword("password2");
        user2.setTenantId(1L);
        user2.setIsActive(false);

        user1 = userRepository.save(user1);
        user2 = userRepository.save(user2);
    }

    @Test
    @DisplayName("Deve buscar user por email e tenant")
    void shouldFindByEmailAndTenantId() {
        Optional<User> found = userRepository.findByEmailAndTenantId("user1@stockflow.com", 1L);

        assertTrue(found.isPresent());
        assertEquals("user1@stockflow.com", found.get().getEmail());
    }

    @Test
    @DisplayName("Deve buscar apenas users ativos")
    void shouldFindOnlyActiveUsers() {
        List<User> activeUsers = userRepository.findByTenantIdAndIsActiveTrue(1L);

        assertEquals(1, activeUsers.size());
        assertEquals("user1@stockflow.com", activeUsers.get(0).getEmail());
    }

    @Test
    @DisplayName("Deve buscar users por nome")
    void shouldFindByNameContaining() {
        List<User> found = userRepository.findByTenantIdAndNameContainingIgnoreCase(1L, "user");

        assertEquals(2, found.size());
    }

    @Test
    @DisplayName("Deve buscar users com role específica")
    void shouldFindByRole() {
        // Assign role to user1
        UserRole userRole = UserRole.builder()
            .user(user1)
            .role(adminRole)
            .build();
        userRoleRepository.save(userRole);

        List<User> usersWithAdminRole = userRepository.findByTenantIdAndRole(1L, RoleEnum.ADMIN);

        assertEquals(1, usersWithAdminRole.size());
        assertEquals("user1@stockflow.com", usersWithAdminRole.get(0).getEmail());
    }

    @Test
    @DisplayName("Deve verificar se email existe no tenant")
    void shouldCheckEmailExistsInTenant() {
        assertTrue(userRepository.existsByEmailAndTenantId("user1@stockflow.com", 1L));
        assertFalse(userRepository.existsByEmailAndTenantId("nonexistent@stockflow.com", 1L));
    }

    @Test
    @DisplayName("Deve contar users ativos")
    void shouldCountActiveUsers() {
        long count = userRepository.countByTenantIdAndIsActiveTrue(1L);

        assertEquals(1, count);
    }

    @Test
    @DisplayName("Deve deletar roles de um user")
    void shouldDeleteUserRoles() {
        // Assign role
        UserRole userRole = UserRole.builder()
            .user(user1)
            .role(adminRole)
            .build();
        userRoleRepository.save(userRole);

        assertEquals(1, userRoleRepository.countByUserId(user1.getId()));

        // Delete
        userRoleRepository.deleteByUserId(user1.getId());

        assertEquals(0, userRoleRepository.countByUserId(user1.getId()));
    }
}
