package com.stockflow.modules.catalog.infrastructure.web;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.stockflow.modules.catalog.application.dto.ProductRequest;
import com.stockflow.modules.catalog.domain.repository.CategoryRepository;
import com.stockflow.modules.catalog.domain.repository.ProductRepository;
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

import java.math.BigDecimal;

import static org.hamcrest.Matchers.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Integration tests for Product endpoints.
 *
 * <p>Tests the complete product CRUD operations:</p>
 * <ul>
 *   <li>Create product</li>
 *   <li>Find all products with pagination</li>
 *   <li>Find product by ID</li>
 *   <li>Find products by category</li>
 *   <li>Update product</li>
 *   <li>Delete product (soft delete)</li>
 * </ul>
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class ProductControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private TenantRepository tenantRepository;

    @Autowired
    private UserRepository userRepository;

    private Long testTenantId = 1L;
    private Long testCategoryId;

    @BeforeEach
    void setUp() throws Exception {
        // Clean up database
        productRepository.deleteAll();
        categoryRepository.deleteAll();
        userRepository.deleteAll();
        tenantRepository.deleteAll();

        // Set tenant context
        TenantContext.setTenantId(testTenantId);

        // Create a test category
        String categoryJson = """
            {
                "name": "Eletrônicos"
            }
            """;

        var result = mockMvc.perform(post("/api/catalog/categories")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(categoryJson))
                .andExpect(status().isCreated())
                .andReturn();

        String response = result.getResponse().getContentAsString();
        testCategoryId = objectMapper.readTree(response).get("id").asLong();
    }

    @AfterEach
    void tearDown() {
        TenantContext.clear();
    }

    @Test
    @WithMockUser(roles = {"ADMIN"})
    @DisplayName("POST /api/catalog/products - Should create product successfully")
    void testCreateProduct_Success() throws Exception {
        // Arrange
        ProductRequest request = new ProductRequest(
            "Smartphone XYZ",
            "SMART-12345",
            "Smartphone de alta qualidade",
            "7891234567890",
            "UN",
            "https://example.com/image.jpg",
            new BigDecimal("1000.00"),
            new BigDecimal("1500.00"),
            10,
            testCategoryId
        );

        // Act & Assert
        mockMvc.perform(post("/api/catalog/products")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").isNumber())
                .andExpect(jsonPath("$.tenantId").value(testTenantId))
                .andExpect(jsonPath("$.name").value("Smartphone XYZ"))
                .andExpect(jsonPath("$.sku").value("SMART-12345"))
                .andExpect(jsonPath("$.unitOfMeasure").value("UN"))
                .andExpect(jsonPath("$.costPrice").value(1000.00))
                .andExpect(jsonPath("$.salePrice").value(1500.00))
                .andExpect(jsonPath("$.minStock").value(10))
                .andExpect(jsonPath("$.categoryId").value(testCategoryId))
                .andExpect(jsonPath("$.isActive").value(true))
                .andExpect(jsonPath("$.version").value(0));
    }

    @Test
    @WithMockUser(roles = {"ADMIN"})
    @DisplayName("POST /api/catalog/products - Should fail when SKU already exists")
    void testCreateProduct_SkuAlreadyExists() throws Exception {
        // Arrange - Create first product
        ProductRequest firstRequest = new ProductRequest(
            "Smartphone XYZ",
            "SMART-12345",
            "Smartphone de alta qualidade",
            null,
            "UN",
            null,
            new BigDecimal("1000.00"),
            new BigDecimal("1500.00"),
            10,
            null
        );

        mockMvc.perform(post("/api/catalog/products")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(firstRequest)))
                .andExpect(status().isCreated());

        // Act & Assert - Try to create duplicate SKU
        ProductRequest duplicateRequest = new ProductRequest(
            "Another Product",
            "SMART-12345",
            "Different description",
            null,
            "UN",
            null,
            new BigDecimal("500.00"),
            new BigDecimal("800.00"),
            5,
            null
        );

        mockMvc.perform(post("/api/catalog/products")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(duplicateRequest)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.error.code").value("PRODUCT_SKU_ALREADY_EXISTS"));
    }

    @Test
    @WithMockUser(roles = {"ADMIN"})
    @DisplayName("POST /api/catalog/products - Should fail when cost price exceeds sale price")
    void testCreateProduct_CostPriceExceedsSalePrice() throws Exception {
        // Arrange
        ProductRequest request = new ProductRequest(
            "Invalid Product",
            "INVALID-001",
            "Product with invalid pricing",
            null,
            "UN",
            null,
            new BigDecimal("2000.00"),
            new BigDecimal("1500.00"),
            10,
            null
        );

        // Act & Assert
        mockMvc.perform(post("/api/catalog/products")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.error.code").value("VALIDATION_ERROR"));
    }

    @Test
    @WithMockUser(roles = {"EMPLOYEE"})
    @DisplayName("POST /api/catalog/products - Should forbid access for non-admin users")
    void testCreateProduct_Forbidden() throws Exception {
        // Arrange
        ProductRequest request = new ProductRequest(
            "Smartphone XYZ",
            "SMART-12345",
            "Smartphone",
            null,
            "UN",
            null,
            new BigDecimal("1000.00"),
            new BigDecimal("1500.00"),
            10,
            null
        );

        // Act & Assert
        mockMvc.perform(post("/api/catalog/products")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = {"ADMIN"})
    @DisplayName("GET /api/catalog/products - Should return paginated list")
    void testFindAllProducts_Success() throws Exception {
        // Arrange - Create multiple products
        for (int i = 1; i <= 3; i++) {
            ProductRequest request = new ProductRequest(
                "Product " + i,
                "PROD-" + String.format("%03d", i),
                "Description " + i,
                null,
                "UN",
                null,
                new BigDecimal("100.00"),
                new BigDecimal("150.00"),
                10,
                null
            );
            mockMvc.perform(post("/api/catalog/products")
                    .with(csrf())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isCreated());
        }

        // Act & Assert
        mockMvc.perform(get("/api/catalog/products")
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
    @DisplayName("GET /api/catalog/products/{id} - Should return product by ID")
    void testFindProductById_Success() throws Exception {
        // Arrange - Create product
        ProductRequest request = new ProductRequest(
            "Smartphone XYZ",
            "SMART-12345",
            "Smartphone",
            null,
            "UN",
            null,
            new BigDecimal("1000.00"),
            new BigDecimal("1500.00"),
            10,
            testCategoryId
        );

        var result = mockMvc.perform(post("/api/catalog/products")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andReturn();

        String response = result.getResponse().getContentAsString();
        Long productId = objectMapper.readTree(response).get("id").asLong();

        // Act & Assert
        mockMvc.perform(get("/api/catalog/products/" + productId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(productId))
                .andExpect(jsonPath("$.name").value("Smartphone XYZ"))
                .andExpect(jsonPath("$.sku").value("SMART-12345"))
                .andExpect(jsonPath("$.isActive").value(true));
    }

    @Test
    @WithMockUser(roles = {"ADMIN"})
    @DisplayName("GET /api/catalog/products/category/{categoryId} - Should return products by category")
    void testFindProductsByCategory_Success() throws Exception {
        // Arrange - Create products in category
        ProductRequest request1 = new ProductRequest(
            "Product 1",
            "PROD-001",
            "Description 1",
            null,
            "UN",
            null,
            new BigDecimal("100.00"),
            new BigDecimal("150.00"),
            10,
            testCategoryId
        );

        ProductRequest request2 = new ProductRequest(
            "Product 2",
            "PROD-002",
            "Description 2",
            null,
            "UN",
            null,
            new BigDecimal("200.00"),
            new BigDecimal("250.00"),
            5,
            testCategoryId
        );

        mockMvc.perform(post("/api/catalog/products")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request1)));

        mockMvc.perform(post("/api/catalog/products")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request2)));

        // Act & Assert
        mockMvc.perform(get("/api/catalog/products/category/" + testCategoryId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content", hasSize(2)))
                .andExpect(jsonPath("$.content[0].categoryId").value(testCategoryId))
                .andExpect(jsonPath("$.content[1].categoryId").value(testCategoryId));
    }

    @Test
    @WithMockUser(roles = {"ADMIN"})
    @DisplayName("PUT /api/catalog/products/{id} - Should update product successfully")
    void testUpdateProduct_Success() throws Exception {
        // Arrange - Create product
        ProductRequest createRequest = new ProductRequest(
            "Smartphone XYZ",
            "SMART-12345",
            "Original description",
            null,
            "UN",
            null,
            new BigDecimal("1000.00"),
            new BigDecimal("1500.00"),
            10,
            null
        );

        var result = mockMvc.perform(post("/api/catalog/products")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createRequest)))
                .andExpect(status().isCreated())
                .andReturn();

        String response = result.getResponse().getContentAsString();
        Long productId = objectMapper.readTree(response).get("id").asLong();

        // Act & Assert - Update product
        ProductRequest updateRequest = new ProductRequest(
            "Smartphone XYZ Updated",
            "SMART-12345",
            "Updated description",
            "7891234567890",
            "UN",
            "https://example.com/new-image.jpg",
            new BigDecimal("1200.00"),
            new BigDecimal("1800.00"),
            15,
            testCategoryId
        );

        mockMvc.perform(put("/api/catalog/products/" + productId)
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(productId))
                .andExpect(jsonPath("$.name").value("Smartphone XYZ Updated"))
                .andExpect(jsonPath("$.description").value("Updated description"))
                .andExpect(jsonPath("$.costPrice").value(1200.00))
                .andExpect(jsonPath("$.salePrice").value(1800.00))
                .andExpect(jsonPath("$.minStock").value(15))
                .andExpect(jsonPath("$.version").value(1)); // Version incremented
    }

    @Test
    @WithMockUser(roles = {"ADMIN"})
    @DisplayName("DELETE /api/catalog/products/{id} - Should soft delete product successfully")
    void testDeleteProduct_Success() throws Exception {
        // Arrange - Create product
        ProductRequest request = new ProductRequest(
            "Smartphone XYZ",
            "SMART-12345",
            "Smartphone",
            null,
            "UN",
            null,
            new BigDecimal("1000.00"),
            new BigDecimal("1500.00"),
            10,
            null
        );

        var result = mockMvc.perform(post("/api/catalog/products")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andReturn();

        String response = result.getResponse().getContentAsString();
        Long productId = objectMapper.readTree(response).get("id").asLong();

        // Act & Assert - Delete product
        mockMvc.perform(delete("/api/catalog/products/" + productId)
                .with(csrf()))
                .andExpect(status().isNoContent());

        // Verify product is soft deleted
        mockMvc.perform(get("/api/catalog/products/" + productId))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.error.code").value("PRODUCT_NOT_FOUND"));
    }

    @Test
    @DisplayName("GET /api/catalog/products - Should return 401 when not authenticated")
    void testFindAllProducts_Unauthenticated() throws Exception {
        // Act & Assert
        mockMvc.perform(get("/api/catalog/products"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(roles = {"ADMIN"})
    @DisplayName("POST /api/catalog/products - Should validate required fields")
    void testCreateProduct_ValidationErrors() throws Exception {
        // Arrange - Empty name
        ProductRequest request = new ProductRequest(
            "",
            "SKU-123",
            "Description",
            null,
            "UN",
            null,
            new BigDecimal("100.00"),
            new BigDecimal("150.00"),
            10,
            null
        );

        // Act & Assert
        mockMvc.perform(post("/api/catalog/products")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(roles = {"ADMIN"})
    @DisplayName("GET /api/catalog/products/search - Should search products by name")
    void testSearchProducts_ByName() throws Exception {
        // Arrange - Create products
        ProductRequest request1 = new ProductRequest(
            "Laptop Dell XYZ",
            "LAPTOP-001",
            "Powerful laptop",
            null,
            "UN",
            null,
            new BigDecimal("2000.00"),
            new BigDecimal("2500.00"),
            5,
            null
        );

        ProductRequest request2 = new ProductRequest(
            "Mouse Logitech",
            "MOUSE-001",
            "Wireless mouse",
            null,
            "UN",
            null,
            new BigDecimal("50.00"),
            new BigDecimal("80.00"),
            20,
            null
        );

        mockMvc.perform(post("/api/catalog/products")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request1)));

        mockMvc.perform(post("/api/catalog/products")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request2)));

        // Act & Assert - Search for "laptop"
        mockMvc.perform(get("/api/catalog/products/search")
                .param("search", "laptop"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content", hasSize(1)))
                .andExpect(jsonPath("$.content[0].name").value("Laptop Dell XYZ"))
                .andExpect(jsonPath("$.content[0].sku").value("LAPTOP-001"));
    }

    @Test
    @WithMockUser(roles = {"ADMIN"})
    @DisplayName("GET /api/catalog/products/search - Should filter by category")
    void testSearchProducts_ByCategory() throws Exception {
        // Arrange - Create products in category
        ProductRequest request1 = new ProductRequest(
            "Product 1",
            "PROD-001",
            "Description 1",
            null,
            "UN",
            null,
            new BigDecimal("100.00"),
            new BigDecimal("150.00"),
            10,
            testCategoryId
        );

        ProductRequest request2 = new ProductRequest(
            "Product 2",
            "PROD-002",
            "Description 2",
            null,
            "UN",
            null,
            new BigDecimal("200.00"),
            new BigDecimal("250.00"),
            5,
            null
        );

        mockMvc.perform(post("/api/catalog/products")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request1)));

        mockMvc.perform(post("/api/catalog/products")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request2)));

        // Act & Assert - Filter by category
        mockMvc.perform(get("/api/catalog/products/search")
                .param("categoryId", String.valueOf(testCategoryId)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content", hasSize(1)))
                .andExpect(jsonPath("$.content[0].categoryId").value(testCategoryId));
    }

    @Test
    @WithMockUser(roles = {"ADMIN"})
    @DisplayName("GET /api/catalog/products/search - Should filter by price range")
    void testSearchProducts_ByPriceRange() throws Exception {
        // Arrange - Create products with different prices
        ProductRequest request1 = new ProductRequest(
            "Cheap Product",
            "CHEAP-001",
            "Low cost product",
            null,
            "UN",
            null,
            new BigDecimal("50.00"),
            new BigDecimal("100.00"),
            10,
            null
        );

        ProductRequest request2 = new ProductRequest(
            "Expensive Product",
            "EXP-001",
            "High cost product",
            null,
            "UN",
            null,
            new BigDecimal("500.00"),
            new BigDecimal("800.00"),
            5,
            null
        );

        mockMvc.perform(post("/api/catalog/products")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request1)));

        mockMvc.perform(post("/api/catalog/products")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request2)));

        // Act & Assert - Filter by price range
        mockMvc.perform(get("/api/catalog/products/search")
                .param("minPrice", "150.00")
                .param("maxPrice", "1000.00"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content", hasSize(1)))
                .andExpect(jsonPath("$.content[0].name").value("Expensive Product"));
    }

    @Test
    @WithMockUser(roles = {"ADMIN"})
    @DisplayName("GET /api/catalog/products/search - Should sort by price descending")
    void testSearchProducts_SortByPriceDescending() throws Exception {
        // Arrange - Create products
        for (int i = 1; i <= 3; i++) {
            ProductRequest request = new ProductRequest(
                "Product " + i,
                "PROD-" + String.format("%03d", i),
                "Description " + i,
                null,
                "UN",
                null,
                new BigDecimal(String.valueOf(i * 100.00)),
                new BigDecimal(String.valueOf(i * 150.00)),
                10,
                null
            );
            mockMvc.perform(post("/api/catalog/products")
                    .with(csrf())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)));
        }

        // Act & Assert - Sort by price descending
        mockMvc.perform(get("/api/catalog/products/search")
                .param("sortBy", "salePrice")
                .param("sortOrder", "DESC"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content", hasSize(3)))
                .andExpect(jsonPath("$.content[0].salePrice").value(450.00))
                .andExpect(jsonPath("$.content[1].salePrice").value(300.00))
                .andExpect(jsonPath("$.content[2].salePrice").value(150.00));
    }

    @Test
    @WithMockUser(roles = {"ADMIN"})
    @DisplayName("GET /api/catalog/products/search - Should filter only active products")
    void testSearchProducts_OnlyActive() throws Exception {
        // Arrange - Create active product
        ProductRequest request = new ProductRequest(
            "Active Product",
            "ACTIVE-001",
            "Active description",
            null,
            "UN",
            null,
            new BigDecimal("100.00"),
            new BigDecimal("150.00"),
            10,
            null
        );

        var result = mockMvc.perform(post("/api/catalog/products")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andReturn();

        String response = result.getResponse().getContentAsString();
        Long productId = objectMapper.readTree(response).get("id").asLong();

        // Soft delete one product
        mockMvc.perform(delete("/api/catalog/products/" + productId)
                .with(csrf()))
                .andExpect(status().isNoContent());

        // Act & Assert - Search only active
        mockMvc.perform(get("/api/catalog/products/search")
                .param("isActive", "true"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content", hasSize(0)));
    }
}
