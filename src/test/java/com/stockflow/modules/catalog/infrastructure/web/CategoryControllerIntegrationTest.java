package com.stockflow.modules.catalog.infrastructure.web;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.stockflow.modules.catalog.application.dto.CategoryRequest;
import com.stockflow.modules.catalog.domain.repository.CategoryRepository;
import com.stockflow.modules.tenant.domain.repository.TenantRepository;
import com.stockflow.modules.users.domain.repository.UserRepository;
import com.stockflow.shared.infrastructure.security.TenantContext;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.hamcrest.Matchers.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Integration tests for Category endpoints.
 *
 * <p>Tests the complete category CRUD operations:</p>
 * <ul>
 *   <li>Create category</li>
 *   <li>Find all categories with pagination</li>
 *   <li>Find category by ID</li>
 *   <li>Update category</li>
 *   <li>Delete category (soft delete)</li>
 * </ul>
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class CategoryControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private TenantRepository tenantRepository;

    @Autowired
    private UserRepository userRepository;

    private Long testTenantId = 1L;

    @BeforeEach
    void setUp() {
        // Clean up database before each test
        categoryRepository.deleteAll();
        userRepository.deleteAll();
        tenantRepository.deleteAll();

        // Set tenant context for tests
        TenantContext.setTenantId(testTenantId);
    }

    @AfterEach
    void tearDown() {
        // Clear tenant context
        TenantContext.clear();
    }

    @Test
    @WithMockUser(roles = {"ADMIN"})
    @DisplayName("POST /api/catalog/categories - Should create category successfully")
    void testCreateCategory_Success() throws Exception {
        // Arrange
        CategoryRequest request = new CategoryRequest("Eletrônicos");

        // Act & Assert
        mockMvc.perform(post("/api/catalog/categories")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").isNumber())
                .andExpect(jsonPath("$.tenantId").value(testTenantId))
                .andExpect(jsonPath("$.name").value("Eletrônicos"))
                .andExpect(jsonPath("$.isActive").value(true))
                .andExpect(jsonPath("$.createdAt").isNotEmpty())
                .andExpect(jsonPath("$.version").value(0));
    }

    @Test
    @WithMockUser(roles = {"ADMIN"})
    @DisplayName("POST /api/catalog/categories - Should fail when name is too short")
    void testCreateCategory_NameTooShort() throws Exception {
        // Arrange
        CategoryRequest request = new CategoryRequest("A");

        // Act & Assert
        mockMvc.perform(post("/api/catalog/categories")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(roles = {"ADMIN"})
    @DisplayName("POST /api/catalog/categories - Should fail when name already exists")
    void testCreateCategory_NameAlreadyExists() throws Exception {
        // Arrange - Create first category
        CategoryRequest firstRequest = new CategoryRequest("Eletrônicos");
        mockMvc.perform(post("/api/catalog/categories")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(firstRequest)))
                .andExpect(status().isCreated());

        // Act & Assert - Try to create duplicate
        CategoryRequest duplicateRequest = new CategoryRequest("Eletrônicos");
        mockMvc.perform(post("/api/catalog/categories")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(duplicateRequest)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.error.code").value("CATEGORY_NAME_ALREADY_EXISTS"));
    }

    @Test
    @WithMockUser(roles = {"EMPLOYEE"})
    @DisplayName("POST /api/catalog/categories - Should forbid access for non-admin users")
    void testCreateCategory_Forbidden() throws Exception {
        // Arrange
        CategoryRequest request = new CategoryRequest("Eletrônicos");

        // Act & Assert
        mockMvc.perform(post("/api/catalog/categories")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = {"ADMIN"})
    @DisplayName("GET /api/catalog/categories - Should return paginated list")
    void testFindAllCategories_Success() throws Exception {
        // Arrange - Create multiple categories
        mockMvc.perform(post("/api/catalog/categories")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(new CategoryRequest("Eletrônicos"))));

        mockMvc.perform(post("/api/catalog/categories")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(new CategoryRequest("Móveis"))));

        mockMvc.perform(post("/api/catalog/categories")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(new CategoryRequest("Roupas"))));

        // Act & Assert
        mockMvc.perform(get("/api/catalog/categories")
                .param("page", "0")
                .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content", hasSize(3)))
                .andExpect(jsonPath("$.content[0].name").exists())
                .andExpect(jsonPath("$.totalElements").value(3))
                .andExpect(jsonPath("$.totalPages").value(1))
                .andExpect(jsonPath("$.pageNumber").value(0));
    }

    @Test
    @WithMockUser(roles = {"ADMIN"})
    @DisplayName("GET /api/catalog/categories/{id} - Should return category by ID")
    void testFindCategoryById_Success() throws Exception {
        // Arrange - Create category
        var result = mockMvc.perform(post("/api/catalog/categories")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(new CategoryRequest("Eletrônicos"))))
                .andExpect(status().isCreated())
                .andReturn();

        String response = result.getResponse().getContentAsString();
        Long categoryId = objectMapper.readTree(response).get("id").asLong();

        // Act & Assert
        mockMvc.perform(get("/api/catalog/categories/" + categoryId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(categoryId))
                .andExpect(jsonPath("$.name").value("Eletrônicos"))
                .andExpect(jsonPath("$.isActive").value(true));
    }

    @Test
    @WithMockUser(roles = {"ADMIN"})
    @DisplayName("GET /api/catalog/categories/{id} - Should return 404 when not found")
    void testFindCategoryById_NotFound() throws Exception {
        // Act & Assert
        mockMvc.perform(get("/api/catalog/categories/99999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.error.code").value("CATEGORY_NOT_FOUND"));
    }

    @Test
    @WithMockUser(roles = {"ADMIN"})
    @DisplayName("PUT /api/catalog/categories/{id} - Should update category successfully")
    void testUpdateCategory_Success() throws Exception {
        // Arrange - Create category
        var result = mockMvc.perform(post("/api/catalog/categories")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(new CategoryRequest("Eletrônicos"))))
                .andExpect(status().isCreated())
                .andReturn();

        String response = result.getResponse().getContentAsString();
        Long categoryId = objectMapper.readTree(response).get("id").asLong();

        // Act & Assert - Update category
        CategoryRequest updateRequest = new CategoryRequest("Eletrônicos e Gadgets");
        mockMvc.perform(put("/api/catalog/categories/" + categoryId)
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(categoryId))
                .andExpect(jsonPath("$.name").value("Eletrônicos e Gadgets"))
                .andExpect(jsonPath("$.version").value(1)); // Version incremented
    }

    @Test
    @WithMockUser(roles = {"ADMIN"})
    @DisplayName("PUT /api/catalog/categories/{id} - Should fail when name already exists")
    void testUpdateCategory_NameAlreadyExists() throws Exception {
        // Arrange - Create two categories
        mockMvc.perform(post("/api/catalog/categories")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(new CategoryRequest("Eletrônicos"))));

        var result = mockMvc.perform(post("/api/catalog/categories")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(new CategoryRequest("Móveis"))))
                .andExpect(status().isCreated())
                .andReturn();

        String response = result.getResponse().getContentAsString();
        Long categoryId = objectMapper.readTree(response).get("id").asLong();

        // Act & Assert - Try to update with duplicate name
        mockMvc.perform(put("/api/catalog/categories/" + categoryId)
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(new CategoryRequest("Eletrônicos"))))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.error.code").value("CATEGORY_NAME_ALREADY_EXISTS"));
    }

    @Test
    @WithMockUser(roles = {"EMPLOYEE"})
    @DisplayName("PUT /api/catalog/categories/{id} - Should forbid access for non-admin users")
    void testUpdateCategory_Forbidden() throws Exception {
        // Arrange
        CategoryRequest request = new CategoryRequest("Eletrônicos Atualizados");

        // Act & Assert
        mockMvc.perform(put("/api/catalog/categories/1")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = {"ADMIN"})
    @DisplayName("DELETE /api/catalog/categories/{id} - Should soft delete category successfully")
    void testDeleteCategory_Success() throws Exception {
        // Arrange - Create category
        var result = mockMvc.perform(post("/api/catalog/categories")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(new CategoryRequest("Eletrônicos"))))
                .andExpect(status().isCreated())
                .andReturn();

        String response = result.getResponse().getContentAsString();
        Long categoryId = objectMapper.readTree(response).get("id").asLong();

        // Act & Assert - Delete category
        mockMvc.perform(delete("/api/catalog/categories/" + categoryId)
                .with(csrf()))
                .andExpect(status().isNoContent());

        // Verify category is soft deleted (not found in active queries)
        mockMvc.perform(get("/api/catalog/categories/" + categoryId))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(roles = {"ADMIN"})
    @DisplayName("DELETE /api/catalog/categories/{id} - Should return 404 when not found")
    void testDeleteCategory_NotFound() throws Exception {
        // Act & Assert
        mockMvc.perform(delete("/api/catalog/categories/99999")
                .with(csrf()))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.error.code").value("CATEGORY_NOT_FOUND"));
    }

    @Test
    @WithMockUser(roles = {"EMPLOYEE"})
    @DisplayName("DELETE /api/catalog/categories/{id} - Should forbid access for non-admin users")
    void testDeleteCategory_Forbidden() throws Exception {
        // Act & Assert
        mockMvc.perform(delete("/api/catalog/categories/1")
                .with(csrf()))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("GET /api/catalog/categories - Should return 401 when not authenticated")
    void testFindAllCategories_Unauthenticated() throws Exception {
        // Act & Assert
        mockMvc.perform(get("/api/catalog/categories"))
                .andExpect(status().isUnauthorized());
    }
}
