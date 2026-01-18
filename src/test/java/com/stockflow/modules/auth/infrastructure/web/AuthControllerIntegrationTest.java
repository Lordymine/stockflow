package com.stockflow.modules.auth.infrastructure.web;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.stockflow.modules.auth.application.dto.LoginRequest;
import com.stockflow.modules.auth.application.dto.RefreshTokenRequest;
import com.stockflow.modules.auth.application.dto.LogoutRequest;
import com.stockflow.modules.auth.application.dto.SignupRequest;
import com.stockflow.modules.auth.application.dto.SignupResponse;
import com.stockflow.modules.tenants.domain.repository.TenantRepository;
import com.stockflow.modules.users.domain.repository.UserRepository;
import com.stockflow.shared.testing.H2IntegrationTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Integration tests for authentication endpoints.
 *
 * <p>
 * Tests the complete authentication flow:
 * </p>
 * <ul>
 * <li>Signup (tenant and admin creation)</li>
 * <li>Login with valid credentials</li>
 * <li>Login with invalid credentials</li>
 * <li>Token refresh</li>
 * <li>Logout</li>
 * </ul>
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_CLASS)
@Transactional
class AuthControllerIntegrationTest extends H2IntegrationTest {

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
        @DisplayName("POST /api/v1/auth/signup - Should create tenant and admin")
        void testSignup_Success() throws Exception {
                // Arrange
                SignupRequest request = new SignupRequest(
                                "Test Company",
                                "test-company",
                                "Admin User",
                                "admin@testcompany.com",
                                "SecurePassword123!");

                // Act & Assert
                mockMvc.perform(post("/api/v1/auth/signup")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request)))
                                .andExpect(status().isCreated())
                                .andExpect(jsonPath("$.data.tenant.id").isNumber())
                                .andExpect(jsonPath("$.data.tenant.name").value("Test Company"))
                                .andExpect(jsonPath("$.data.tenant.slug").value("test-company"))
                                .andExpect(jsonPath("$.data.user.id").isNumber())
                                .andExpect(jsonPath("$.data.user.name").value("Admin User"))
                                .andExpect(jsonPath("$.data.user.email").value("admin@testcompany.com"))
                                .andExpect(jsonPath("$.data.user.roles").value(hasItem("ADMIN")))
                                .andExpect(jsonPath("$.data.tokens.accessToken").isNotEmpty())
                                .andExpect(jsonPath("$.data.tokens.refreshToken").isNotEmpty())
                                .andExpect(jsonPath("$.data.tokens.tokenType").value("Bearer"))
                                .andExpect(jsonPath("$.data.tokens.expiresIn").value(900L)); // 15 minutes
        }

        @Test
        @DisplayName("POST /api/v1/auth/signup - Should fail when tenant slug already exists")
        void testSignup_TenantSlugAlreadyExists() throws Exception {
                // Arrange - Create initial tenant
                SignupRequest firstRequest = new SignupRequest(
                                "Test Company",
                                "test-company",
                                "Admin User",
                                "admin@testcompany.com",
                                "SecurePassword123!");

                mockMvc.perform(post("/api/v1/auth/signup")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(firstRequest)))
                                .andExpect(status().isCreated());

                // Act & Assert - Try to sign up with the same slug
                mockMvc.perform(post("/api/v1/auth/signup")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(new SignupRequest(
                                                "Another Company",
                                                "test-company",
                                                "Another Admin",
                                                "admin@anothercompany.com",
                                                "SecurePassword456!"))))
                                .andExpect(status().isConflict())
                                .andExpect(jsonPath("$.success").value(false))
                                .andExpect(jsonPath("$.error.code").value("TENANT_SLUG_ALREADY_EXISTS"));
        }

        @Test
        @DisplayName("POST /api/v1/auth/login - Should authenticate valid credentials")
        void testLogin_Success() throws Exception {
                // Arrange - Signup first
                SignupRequest signupRequest = new SignupRequest(
                                "Test Company",
                                "test-company",
                                "Admin User",
                                "admin@testcompany.com",
                                "SecurePassword123!");

                MvcResult result = mockMvc.perform(post("/api/v1/auth/signup")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(signupRequest)))
                                .andExpect(status().isCreated())
                                .andReturn();

                // Act - Login with credentials
                LoginRequest loginRequest = new LoginRequest(
                                "admin@testcompany.com",
                                "SecurePassword123!");

                mockMvc.perform(post("/api/v1/auth/login")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(loginRequest)))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.data.accessToken").isNotEmpty())
                                .andExpect(jsonPath("$.data.refreshToken").isNotEmpty())
                                .andExpect(jsonPath("$.data.tokenType").value("Bearer"))
                                .andExpect(jsonPath("$.data.expiresIn").value(900L))
                                .andExpect(jsonPath("$.data.user.id").isNumber())
                                .andExpect(jsonPath("$.data.user.email").value("admin@testcompany.com"))
                                .andExpect(jsonPath("$.data.user.roles").value(hasItem("ADMIN")));
        }

        @Test
        @DisplayName("POST /api/v1/auth/login - Should fail with invalid credentials")
        void testLogin_InvalidCredentials() throws Exception {
                // Arrange - Signup first
                SignupRequest signupRequest = new SignupRequest(
                                "Test Company",
                                "test-company",
                                "Admin User",
                                "admin@testcompany.com",
                                "SecurePassword123!");

                mockMvc.perform(post("/api/v1/auth/signup")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(signupRequest)))
                                .andExpect(status().isCreated());

                // Act & Assert - Login with wrong password
                LoginRequest loginRequest = new LoginRequest(
                                "admin@testcompany.com",
                                "WrongPassword123!");

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
                // Arrange - Signup and get refresh token
                SignupRequest signupRequest = new SignupRequest(
                                "Test Company",
                                "test-company",
                                "Admin User",
                                "admin@testcompany.com",
                                "SecurePassword123!");

                MvcResult signupResult = mockMvc.perform(post("/api/v1/auth/signup")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(signupRequest)))
                                .andExpect(status().isCreated())
                                .andReturn();

                String responseJson = signupResult.getResponse().getContentAsString();
                SignupResponse signupResponse = objectMapper.treeToValue(
                                objectMapper.readTree(responseJson).get("data"),
                                SignupResponse.class);
                String refreshToken = signupResponse.tokens().refreshToken();

                // Act - Refresh token
                RefreshTokenRequest refreshRequest = new RefreshTokenRequest(refreshToken);

                mockMvc.perform(post("/api/v1/auth/refresh")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(refreshRequest)))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.data.accessToken").isNotEmpty())
                                .andExpect(jsonPath("$.data.refreshToken").isNotEmpty())
                                .andExpect(jsonPath("$.data.tokenType").value("Bearer"))
                                .andExpect(jsonPath("$.data.expiresIn").value(900L));
        }

        @Test
        @DisplayName("POST /api/v1/auth/logout - Should revoke refresh token")
        void testLogout_Success() throws Exception {
                // Arrange - Signup and get refresh token
                SignupRequest signupRequest = new SignupRequest(
                                "Test Company",
                                "test-company",
                                "Admin User",
                                "admin@testcompany.com",
                                "SecurePassword123!");

                MvcResult signupResult = mockMvc.perform(post("/api/v1/auth/signup")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(signupRequest)))
                                .andExpect(status().isCreated())
                                .andReturn();

                String responseJson = signupResult.getResponse().getContentAsString();
                SignupResponse signupResponse = objectMapper.treeToValue(
                                objectMapper.readTree(responseJson).get("data"),
                                SignupResponse.class);
                String refreshToken = signupResponse.tokens().refreshToken();

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
                // Arrange - Signup and get access token
                SignupRequest signupRequest = new SignupRequest(
                                "Test Company",
                                "test-company",
                                "Admin User",
                                "admin@testcompany.com",
                                "SecurePassword123!");

                MvcResult signupResult = mockMvc.perform(post("/api/v1/auth/signup")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(signupRequest)))
                                .andExpect(status().isCreated())
                                .andReturn();

                String responseJson = signupResult.getResponse().getContentAsString();
                SignupResponse signupResponse = objectMapper.treeToValue(
                                objectMapper.readTree(responseJson).get("data"),
                                SignupResponse.class);
                String accessToken = signupResponse.tokens().accessToken();

                // Act & Assert - Get current tenant
                mockMvc.perform(get("/api/v1/tenants/me")
                                .header("Authorization", "Bearer " + accessToken))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.data.id").isNumber())
                                .andExpect(jsonPath("$.data.name").value("Test Company"))
                                .andExpect(jsonPath("$.data.slug").value("test-company"))
                                .andExpect(jsonPath("$.data.isActive").value(true));
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
