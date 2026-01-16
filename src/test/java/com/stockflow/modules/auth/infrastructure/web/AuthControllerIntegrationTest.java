package com.stockflow.modules.auth.infrastructure.web;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.stockflow.modules.auth.application.dto.BootstrapRequest;
import com.stockflow.modules.auth.application.dto.LoginRequest;
import com.stockflow.modules.auth.application.dto.LoginResponse;
import com.stockflow.modules.auth.application.dto.RefreshTokenRequest;
import com.stockflow.modules.auth.application.dto.LogoutRequest;
import com.stockflow.modules.auth.application.dto.RefreshTokenResponse;
import com.stockflow.modules.auth.application.dto.BootstrapResponse;
import com.stockflow.modules.tenant.domain.repository.TenantRepository;
import com.stockflow.modules.users.domain.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;

import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Integration tests for authentication endpoints.
 *
 * <p>Tests the complete authentication flow:</p>
 * <ul>
 *   <li>Bootstrap (initial tenant and admin creation)</li>
 *   <li>Login with valid credentials</li>
 *   <li>Login with invalid credentials</li>
 *   <li>Token refresh</li>
 *   <li>Logout</li>
 * </ul>
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class AuthControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private TenantRepository tenantRepository;

    @Autowired
    private UserRepository userRepository;

    @BeforeEach
    void setUp() {
        // Clean up database before each test
        userRepository.deleteAll();
        tenantRepository.deleteAll();
    }

    @Test
    @DisplayName("POST /api/v1/auth/bootstrap - Should create initial tenant and admin")
    void testBootstrap_Success() throws Exception {
        // Arrange
        BootstrapRequest request = new BootstrapRequest(
            "Test Company",
            "test-company",
            "Admin User",
            "admin@testcompany.com",
            "SecurePassword123!"
        );

        // Act & Assert
        mockMvc.perform(post("/api/v1/auth/bootstrap")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.tenant.id").isNumber())
                .andExpect(jsonPath("$.tenant.name").value("Test Company"))
                .andExpect(jsonPath("$.tenant.slug").value("test-company"))
                .andExpect(jsonPath("$.user.id").isNumber())
                .andExpect(jsonPath("$.user.name").value("Admin User"))
                .andExpect(jsonPath("$.user.email").value("admin@testcompany.com"))
                .andExpect(jsonPath("$.user.roles").value(hasItem("ADMIN")))
                .andExpect(jsonPath("$.tokens.accessToken").isNotEmpty())
                .andExpect(jsonPath("$.tokens.refreshToken").isNotEmpty())
                .andExpect(jsonPath("$.tokens.tokenType").value("Bearer"))
                .andExpect(jsonPath("$.tokens.expiresIn").value(900L)); // 15 minutes
    }

    @Test
    @DisplayName("POST /api/v1/auth/bootstrap - Should fail when tenant already exists")
    void testBootstrap_TenantAlreadyExists() throws Exception {
        // Arrange - Create initial tenant
        BootstrapRequest firstRequest = new BootstrapRequest(
            "Test Company",
            "test-company",
            "Admin User",
            "admin@testcompany.com",
            "SecurePassword123!"
        );

        mockMvc.perform(post("/api/v1/auth/bootstrap")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(firstRequest)))
                .andExpect(status().isCreated());

        // Act & Assert - Try to bootstrap again
        BootstrapRequest secondRequest = new BootstrapRequest(
            "Another Company",
            "another-company",
            "Another Admin",
            "admin@anothercompany.com",
            "SecurePassword456!"
        );

        mockMvc.perform(post("/api/v1/auth/bootstrap")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(secondRequest)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.error.code").value("RESOURCE_ALREADY_EXISTS"));
    }

    @Test
    @DisplayName("POST /api/v1/auth/login - Should authenticate valid credentials")
    void testLogin_Success() throws Exception {
        // Arrange - Bootstrap first
        BootstrapRequest bootstrapRequest = new BootstrapRequest(
            "Test Company",
            "test-company",
            "Admin User",
            "admin@testcompany.com",
            "SecurePassword123!"
        );

        MvcResult result = mockMvc.perform(post("/api/v1/auth/bootstrap")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(bootstrapRequest)))
                .andExpect(status().isCreated())
                .andReturn();

        // Act - Login with credentials
        LoginRequest loginRequest = new LoginRequest(
            "admin@testcompany.com",
            "SecurePassword123!"
        );

        mockMvc.perform(post("/api/v1/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").isNotEmpty())
                .andExpect(jsonPath("$.refreshToken").isNotEmpty())
                .andExpect(jsonPath("$.tokenType").value("Bearer"))
                .andExpect(jsonPath("$.expiresIn").value(900L))
                .andExpect(jsonPath("$.user.id").isNumber())
                .andExpect(jsonPath("$.user.email").value("admin@testcompany.com"))
                .andExpect(jsonPath("$.user.roles").value(hasItem("ADMIN")));
    }

    @Test
    @DisplayName("POST /api/v1/auth/login - Should fail with invalid credentials")
    void testLogin_InvalidCredentials() throws Exception {
        // Arrange - Bootstrap first
        BootstrapRequest bootstrapRequest = new BootstrapRequest(
            "Test Company",
            "test-company",
            "Admin User",
            "admin@testcompany.com",
            "SecurePassword123!"
        );

        mockMvc.perform(post("/api/v1/auth/bootstrap")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(bootstrapRequest)))
                .andExpect(status().isCreated());

        // Act & Assert - Login with wrong password
        LoginRequest loginRequest = new LoginRequest(
            "admin@testcompany.com",
            "WrongPassword123!"
        );

        mockMvc.perform(post("/api/v1/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.error.code").value("AUTH_INVALID_CREDENTIALS"));
    }

    @Test
    @DisplayName("POST /api/v1/auth/refresh - Should refresh access token")
    void testRefreshToken_Success() throws Exception {
        // Arrange - Bootstrap and get refresh token
        BootstrapRequest bootstrapRequest = new BootstrapRequest(
            "Test Company",
            "test-company",
            "Admin User",
            "admin@testcompany.com",
            "SecurePassword123!"
        );

        MvcResult bootstrapResult = mockMvc.perform(post("/api/v1/auth/bootstrap")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(bootstrapRequest)))
                .andExpect(status().isCreated())
                .andReturn();

        String responseJson = bootstrapResult.getResponse().getContentAsString();
        BootstrapResponse bootstrapResponse = objectMapper.readValue(responseJson, BootstrapResponse.class);
        String refreshToken = bootstrapResponse.tokens().refreshToken();

        // Act - Refresh token
        RefreshTokenRequest refreshRequest = new RefreshTokenRequest(refreshToken);

        mockMvc.perform(post("/api/v1/auth/refresh")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(refreshRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").isNotEmpty())
                .andExpect(jsonPath("$.refreshToken").isNotEmpty())
                .andExpect(jsonPath("$.tokenType").value("Bearer"))
                .andExpect(jsonPath("$.expiresIn").value(900L));
    }

    @Test
    @DisplayName("POST /api/v1/auth/logout - Should revoke refresh token")
    void testLogout_Success() throws Exception {
        // Arrange - Bootstrap and get refresh token
        BootstrapRequest bootstrapRequest = new BootstrapRequest(
            "Test Company",
            "test-company",
            "Admin User",
            "admin@testcompany.com",
            "SecurePassword123!"
        );

        MvcResult bootstrapResult = mockMvc.perform(post("/api/v1/auth/bootstrap")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(bootstrapRequest)))
                .andExpect(status().isCreated())
                .andReturn();

        String responseJson = bootstrapResult.getResponse().getContentAsString();
        BootstrapResponse bootstrapResponse = objectMapper.readValue(responseJson, BootstrapResponse.class);
        String refreshToken = bootstrapResponse.tokens().refreshToken();

        // Act - Logout
        LogoutRequest logoutRequest = new LogoutRequest(refreshToken);

        mockMvc.perform(post("/api/v1/auth/logout")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(logoutRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));

        // Assert - Try to use the same refresh token again (should fail)
        RefreshTokenRequest refreshRequest = new RefreshTokenRequest(refreshToken);

        mockMvc.perform(post("/api/v1/auth/refresh")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(refreshRequest)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    @DisplayName("GET /api/v1/tenants/me - Should get current tenant")
    void testGetCurrentTenant_Success() throws Exception {
        // Arrange - Bootstrap and get access token
        BootstrapRequest bootstrapRequest = new BootstrapRequest(
            "Test Company",
            "test-company",
            "Admin User",
            "admin@testcompany.com",
            "SecurePassword123!"
        );

        MvcResult bootstrapResult = mockMvc.perform(post("/api/v1/auth/bootstrap")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(bootstrapRequest)))
                .andExpect(status().isCreated())
                .andReturn();

        String responseJson = bootstrapResult.getResponse().getContentAsString();
        BootstrapResponse bootstrapResponse = objectMapper.readValue(responseJson, BootstrapResponse.class);
        String accessToken = bootstrapResponse.tokens().accessToken();

        // Act & Assert - Get current tenant
        mockMvc.perform(get("/api/v1/tenants/me")
                .header("Authorization", "Bearer " + accessToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").isNumber())
                .andExpect(jsonPath("$.name").value("Test Company"))
                .andExpect(jsonPath("$.slug").value("test-company"))
                .andExpect(jsonPath("$.isActive").value(true));
    }

    @Test
    @DisplayName("GET /api/v1/tenants/me - Should fail without authentication")
    void testGetCurrentTenant_Unauthorized() throws Exception {
        // Act & Assert - Try to get tenant without token
        mockMvc.perform(get("/api/v1/tenants/me"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.error.code").value("AUTH_TOKEN_INVALID"));
    }
}
