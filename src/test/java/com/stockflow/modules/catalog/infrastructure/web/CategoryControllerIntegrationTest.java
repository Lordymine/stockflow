package com.stockflow.modules.catalog.infrastructure.web;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.stockflow.modules.catalog.application.dto.CategoryRequest;
import com.stockflow.modules.catalog.domain.repository.CategoryRepository;
import com.stockflow.modules.tenants.domain.model.Tenant;
import com.stockflow.modules.tenants.domain.repository.TenantRepository;
import com.stockflow.modules.users.domain.repository.UserRepository;
import com.stockflow.shared.security.TestSecurityUtils;
import com.stockflow.shared.testing.TestcontainersIntegrationTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.test.web.servlet.request.RequestPostProcessor;

import java.util.List;

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
@Transactional
class CategoryControllerIntegrationTest extends TestcontainersIntegrationTest {

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

    private Long testTenantId;
    private RequestPostProcessor adminUser;
    private RequestPostProcessor staffUser;

    @BeforeEach
    void setUp() {
        // Clean up database before each test
        categoryRepository.deleteAll();
        userRepository.deleteAll();
        tenantRepository.deleteAll();

        Tenant tenant = tenantRepository.save(new Tenant("Test Tenant", "test-tenant"));
        testTenantId = tenant.getId();
        adminUser = TestSecurityUtils.admin(testTenantId, List.of());
        staffUser = TestSecurityUtils.staff(testTenantId, List.of());
    }

    @Test
    @DisplayName("POST /api/v1/categories - Should create category successfully")
    void testCreateCategory_Success() throws Exception {
        // Arrange
        CategoryRequest request = new CategoryRequest("Eletronicos");

        // Act & Assert
        mockMvc.perform(post("/api/v1/categories")
                .with(adminUser)
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.id").isNumber())
                .andExpect(jsonPath("$.data.tenantId").value(testTenantId))
                .andExpect(jsonPath("$.data.name").value("Eletronicos"))
                .andExpect(jsonPath("$.data.isActive").value(true))
                .andExpect(jsonPath("$.data.createdAt").isNotEmpty())
                .andExpect(jsonPath("$.data.version").value(0));
    }

    @Test
    @DisplayName("POST /api/v1/categories - Should fail when name is too short")
    void testCreateCategory_NameTooShort() throws Exception {
        // Arrange
        CategoryRequest request = new CategoryRequest("A");

        // Act & Assert
        mockMvc.perform(post("/api/v1/categories")
                .with(adminUser)
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("POST /api/v1/categories - Should fail when name already exists")
    void testCreateCategory_NameAlreadyExists() throws Exception {
        // Arrange - Create first category
        CategoryRequest firstRequest = new CategoryRequest("Eletronicos");
        mockMvc.perform(post("/api/v1/categories")
                .with(adminUser)
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(firstRequest)))
                .andExpect(status().isCreated());

        // Act & Assert - Try to create duplicate
        CategoryRequest duplicateRequest = new CategoryRequest("Eletronicos");
        mockMvc.perform(post("/api/v1/categories")
                .with(adminUser)
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(duplicateRequest)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.error.code").value("CATEGORY_NAME_ALREADY_EXISTS"));
    }

    @Test
    @DisplayName("POST /api/v1/categories - Should forbid access for non-admin users")
    void testCreateCategory_Forbidden() throws Exception {
        // Arrange
        CategoryRequest request = new CategoryRequest("Eletronicos");

        // Act & Assert
        mockMvc.perform(post("/api/v1/categories")
                .with(staffUser)
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("GET /api/v1/categories - Should return paginated list")
    void testFindAllCategories_Success() throws Exception {
        // Arrange - Create multiple categories
        mockMvc.perform(post("/api/v1/categories")
                .with(adminUser)
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(new CategoryRequest("Eletronicos"))));

        mockMvc.perform(post("/api/v1/categories")
                .with(adminUser)
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(new CategoryRequest("Moveis"))));

        mockMvc.perform(post("/api/v1/categories")
                .with(adminUser)
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(new CategoryRequest("Roupas"))));

        // Act & Assert
        mockMvc.perform(get("/api/v1/categories")
                .with(adminUser)
                .param("page", "0")
                .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.items").isArray())
                .andExpect(jsonPath("$.data.items", hasSize(3)))
                .andExpect(jsonPath("$.data.items[0].name").exists())
                .andExpect(jsonPath("$.meta.totalItems").value(3))
                .andExpect(jsonPath("$.meta.totalPages").value(1))
                .andExpect(jsonPath("$.meta.page").value(0));
    }

    @Test
    @DisplayName("GET /api/v1/categories/{id} - Should return category by ID")
    void testFindCategoryById_Success() throws Exception {
        // Arrange - Create category
        var result = mockMvc.perform(post("/api/v1/categories")
                .with(adminUser)
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(new CategoryRequest("Eletronicos"))))
                .andExpect(status().isCreated())
                .andReturn();

        String response = result.getResponse().getContentAsString();
        Long categoryId = objectMapper.readTree(response).get("data").get("id").asLong();

        // Act & Assert
        mockMvc.perform(get("/api/v1/categories/" + categoryId)
                .with(adminUser))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.id").value(categoryId))
                .andExpect(jsonPath("$.data.name").value("Eletronicos"))
                .andExpect(jsonPath("$.data.isActive").value(true));
    }

    @Test
    @DisplayName("GET /api/v1/categories/{id} - Should return 404 when not found")
    void testFindCategoryById_NotFound() throws Exception {
        // Act & Assert
        mockMvc.perform(get("/api/v1/categories/99999")
                .with(adminUser))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.error.code").value("CATEGORY_NOT_FOUND"));
    }

    @Test
    @DisplayName("PUT /api/v1/categories/{id} - Should update category successfully")
    void testUpdateCategory_Success() throws Exception {
        // Arrange - Create category
        var result = mockMvc.perform(post("/api/v1/categories")
                .with(adminUser)
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(new CategoryRequest("Eletronicos"))))
                .andExpect(status().isCreated())
                .andReturn();

        String response = result.getResponse().getContentAsString();
        Long categoryId = objectMapper.readTree(response).get("data").get("id").asLong();

        // Act & Assert - Update category
        CategoryRequest updateRequest = new CategoryRequest("Eletronicos e Gadgets");
        mockMvc.perform(put("/api/v1/categories/" + categoryId)
                .with(adminUser)
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.id").value(categoryId))
                .andExpect(jsonPath("$.data.name").value("Eletronicos e Gadgets"))
                .andExpect(jsonPath("$.data.version").value(1)); // Version incremented
    }

    @Test
    @DisplayName("PUT /api/v1/categories/{id} - Should fail when name already exists")
    void testUpdateCategory_NameAlreadyExists() throws Exception {
        // Arrange - Create two categories
        mockMvc.perform(post("/api/v1/categories")
                .with(adminUser)
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(new CategoryRequest("Eletronicos"))));

        var result = mockMvc.perform(post("/api/v1/categories")
                .with(adminUser)
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(new CategoryRequest("Moveis"))))
                .andExpect(status().isCreated())
                .andReturn();

        String response = result.getResponse().getContentAsString();
        Long categoryId = objectMapper.readTree(response).get("data").get("id").asLong();

        // Act & Assert - Try to update with duplicate name
        mockMvc.perform(put("/api/v1/categories/" + categoryId)
                .with(adminUser)
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(new CategoryRequest("Eletronicos"))))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.error.code").value("CATEGORY_NAME_ALREADY_EXISTS"));
    }

    @Test
    @DisplayName("PUT /api/v1/categories/{id} - Should forbid access for non-admin users")
    void testUpdateCategory_Forbidden() throws Exception {
        // Arrange
        CategoryRequest request = new CategoryRequest("Eletronicos Atualizados");

        // Act & Assert
        mockMvc.perform(put("/api/v1/categories/1")
                .with(staffUser)
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("DELETE /api/v1/categories/{id} - Should soft delete category successfully")
    void testDeleteCategory_Success() throws Exception {
        // Arrange - Create category
        var result = mockMvc.perform(post("/api/v1/categories")
                .with(adminUser)
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(new CategoryRequest("Eletronicos"))))
                .andExpect(status().isCreated())
                .andReturn();

        String response = result.getResponse().getContentAsString();
        Long categoryId = objectMapper.readTree(response).get("data").get("id").asLong();

        // Act & Assert - Delete category
        mockMvc.perform(delete("/api/v1/categories/" + categoryId)
                .with(adminUser)
                .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));

        // Verify category is soft deleted (not found in active queries)
        mockMvc.perform(get("/api/v1/categories/" + categoryId)
                .with(adminUser))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.error.code").value("CATEGORY_NOT_FOUND"));
    }

    @Test
    @DisplayName("DELETE /api/v1/categories/{id} - Should return 404 when not found")
    void testDeleteCategory_NotFound() throws Exception {
        // Act & Assert
        mockMvc.perform(delete("/api/v1/categories/99999")
                .with(adminUser)
                .with(csrf()))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.error.code").value("CATEGORY_NOT_FOUND"));
    }

    @Test
    @DisplayName("DELETE /api/v1/categories/{id} - Should forbid access for non-admin users")
    void testDeleteCategory_Forbidden() throws Exception {
        // Act & Assert
        mockMvc.perform(delete("/api/v1/categories/1")
                .with(staffUser)
                .with(csrf()))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("GET /api/v1/categories - Should return 401 when not authenticated")
    void testFindAllCategories_Unauthenticated() throws Exception {
        // Act & Assert
        mockMvc.perform(get("/api/v1/categories"))
                .andExpect(status().isUnauthorized());
    }
}





