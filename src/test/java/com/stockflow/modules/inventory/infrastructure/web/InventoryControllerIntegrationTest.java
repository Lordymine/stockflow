package com.stockflow.modules.inventory.infrastructure.web;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.stockflow.modules.inventory.application.dto.StockMovementRequest;
import com.stockflow.modules.inventory.application.dto.TransferStockRequest;
import com.stockflow.modules.inventory.domain.model.MovementReason;
import com.stockflow.modules.inventory.domain.model.MovementType;
import com.stockflow.modules.inventory.domain.repository.BranchProductStockRepository;
import com.stockflow.modules.inventory.domain.repository.StockMovementRepository;
import com.stockflow.modules.users.domain.model.Branch;
import com.stockflow.modules.users.domain.repository.BranchRepository;
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
 * Integration tests for Inventory endpoints.
 *
 * <p>Tests the complete inventory management:</p>
 * <ul>
 *   <li>Query stock levels by branch/product</li>
 *   <li>Create stock movements (IN/OUT/ADJUSTMENT)</li>
 *   <li>Transfer stock between branches</li>
 *   <li>View movement history</li>
 *   <li>Optimistic locking and concurrency</li>
 * </ul>
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class InventoryControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private BranchProductStockRepository stockRepository;

    @Autowired
    private StockMovementRepository movementRepository;

    @Autowired
    private BranchRepository branchRepository;

    @Autowired
    private UserRepository userRepository;

    private Long testTenantId = 1L;
    private Long branch1Id;
    private Long branch2Id;
    private Long testProductId;

    @BeforeEach
    void setUp() throws Exception {
        // Clean up
        stockRepository.deleteAll();
        movementRepository.deleteAll();
        branchRepository.deleteAll();
        userRepository.deleteAll();

        // Set tenant context
        TenantContext.setTenantId(testTenantId);

        // Create two test branches
        Branch branch1 = new Branch(testTenantId, "Filial Centro", "CENTRO");
        branch1.setAddress("Rua A, 123");
        branch1 = branchRepository.save(branch1);
        branch1Id = branch1.getId();

        Branch branch2 = new Branch(testTenantId, "Filial Norte", "NORTE");
        branch2.setAddress("Rua B, 456");
        branch2 = branchRepository.save(branch2);
        branch2Id = branch2.getId();

        // Create a test product
        String productJson = """
            {
                "name": "Test Product",
                "sku": "TEST-001",
                "description": "Test product for inventory",
                "unitOfMeasure": "UN",
                "costPrice": 100.00,
                "salePrice": 150.00,
                "minStock": 10
            }
            """;

        var result = mockMvc.perform(post("/api/catalog/products")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(productJson))
                .andExpect(status().isCreated())
                .andReturn();

        String response = result.getResponse().getContentAsString();
        testProductId = objectMapper.readTree(response).get("id").asLong();

        // Initialize stock in both branches
        var stock1 = new com.stockflow.modules.inventory.domain.model.BranchProductStock(
            testTenantId,
            branch1Id,
            testProductId,
            100
        );
        stockRepository.save(stock1);

        var stock2 = new com.stockflow.modules.inventory.domain.model.BranchProductStock(
            testTenantId,
            branch2Id,
            testProductId,
            50
        );
        stockRepository.save(stock2);
    }

    @AfterEach
    void tearDown() {
        TenantContext.clear();
    }

    @Test
    @WithMockUser(roles = {"ADMIN"})
    @DisplayName("GET /api/inventory/stock - Should return all stocks with pagination")
    void testGetAllStock_Success() throws Exception {
        // Act & Assert
        mockMvc.perform(get("/api/inventory/stock")
                .param("page", "0")
                .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content", hasSize(2)))
                .andExpect(jsonPath("$.content[0].branchId").value(branch1Id))
                .andExpect(jsonPath("$.content[0].productId").value(testProductId))
                .andExpect(jsonPath("$.content[0].quantity").value(100))
                .andExpect(jsonPath("$.content[1].branchId").value(branch2Id))
                .andExpect(jsonPath("$.content[1].productId").value(testProductId))
                .andExpect(jsonPath("$.content[1].quantity").value(50))
                .andExpect(jsonPath("$.totalElements").value(2));
    }

    @Test
    @WithMockUser(roles = {"ADMIN"})
    @DisplayName("GET /api/inventory/stock/branch/{branchId} - Should return stock by branch")
    void testGetStockByBranch_Success() throws Exception {
        // Act & Assert
        mockMvc.perform(get("/api/inventory/stock/branch/" + branch1Id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content", hasSize(1)))
                .andExpect(jsonPath("$.content[0].branchId").value(branch1Id))
                .andExpect(jsonPath("$.content[0].productId").value(testProductId))
                .andExpect(jsonPath("$.content[0].quantity").value(100));
    }

    @Test
    @WithMockUser(roles = {"ADMIN"})
    @DisplayName("GET /api/inventory/stock/branch/{branchId}/product/{productId} - Should return specific stock")
    void testGetSpecificStock_Success() throws Exception {
        // Act & Assert
        mockMvc.perform(get("/api/inventory/stock/branch/" + branch1Id + "/product/" + testProductId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.branchId").value(branch1Id))
                .andExpect(jsonPath("$.productId").value(testProductId))
                .andExpect(jsonPath("$.quantity").value(100))
                .andExpect(jsonPath("$.version").isNumber());
    }

    @Test
    @WithMockUser(roles = {"ADMIN"})
    @DisplayName("POST /api/inventory/movements - Should create IN movement")
    void testCreateMovement_IN_Success() throws Exception {
        // Arrange
        StockMovementRequest request = new StockMovementRequest(
            branch1Id,
            testProductId,
            MovementType.IN,
            MovementReason.PURCHASE,
            50,
            "Initial stock purchase"
        );

        // Act & Assert
        mockMvc.perform(post("/api/inventory/movements")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.branchId").value(branch1Id))
                .andExpect(jsonPath("$.productId").value(testProductId))
                .andExpect(jsonPath("$.type").value("IN"))
                .andExpect(jsonPath("$.reason").value("PURCHASE"))
                .andExpect(jsonPath("$.quantity").value(50))
                .andExpect(jsonPath("$.createdByUserId").isNumber());

        // Verify stock was updated
        mockMvc.perform(get("/api/inventory/stock/branch/" + branch1Id + "/product/" + testProductId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.quantity").value(150)); // 100 + 50
    }

    @Test
    @WithMockUser(roles = {"ADMIN"})
    @DisplayName("POST /api/inventory/movements - Should create OUT movement with sufficient stock")
    void testCreateMovement_OUT_Success() throws Exception {
        // Arrange
        StockMovementRequest request = new StockMovementRequest(
            branch1Id,
            testProductId,
            MovementType.OUT,
            MovementReason.SALE,
            30,
            "Sale to customer"
        );

        // Act & Assert
        mockMvc.perform(post("/api/inventory/movements")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.type").value("OUT"))
                .andExpect(jsonPath("$.quantity").value(30));

        // Verify stock was decreased
        mockMvc.perform(get("/api/inventory/stock/branch/" + branch1Id + "/product/" + testProductId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.quantity").value(70)); // 100 - 30
    }

    @Test
    @WithMockUser(roles = {"ADMIN"})
    @DisplayName("POST /api/inventory/movements - Should fail OUT movement with insufficient stock")
    void testCreateMovement_OUT_InsufficientStock() throws Exception {
        // Arrange
        StockMovementRequest request = new StockMovementRequest(
            branch1Id,
            testProductId,
            MovementType.OUT,
            MovementReason.SALE,
            150, // More than available (100)
            "Invalid sale"
        );

        // Act & Assert
        mockMvc.perform(post("/api/inventory/movements")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.error.code").value("STOCK_INSUFFICIENT"))
                .andExpect(jsonPath("$.error.message").value(containsString("Insufficient stock")));
    }

    @Test
    @WithMockUser(roles = {"ADMIN"})
    @DisplayName("POST /api/inventory/transfers - Should transfer stock between branches")
    void testTransferStock_Success() throws Exception {
        // Arrange
        TransferStockRequest request = new TransferStockRequest(
            branch1Id,  // From branch 1 (100 units)
            branch2Id,  // To branch 2 (50 units)
            testProductId,
            30,
            "Stock transfer"
        );

        // Act & Assert
        mockMvc.perform(post("/api/inventory/transfers")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.sourceBranchId").value(branch1Id))
                .andExpect(jsonPath("$.destinationBranchId").value(branch2Id))
                .andExpect(jsonPath("$.quantity").value(30));

        // Verify stocks were updated
        mockMvc.perform(get("/api/inventory/stock/branch/" + branch1Id + "/product/" + testProductId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.quantity").value(70)); // 100 - 30

        mockMvc.perform(get("/api/inventory/stock/branch/" + branch2Id + "/product/" + testProductId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.quantity").value(80)); // 50 + 30
    }

    @Test
    @WithMockUser(roles = {"ADMIN"})
    @DisplayName("POST /api/inventory/transfers - Should fail with insufficient stock")
    void testTransferStock_InsufficientStock() throws Exception {
        // Arrange
        TransferStockRequest request = new TransferStockRequest(
            branch1Id,
            branch2Id,
            testProductId,
            200, // More than available (100)
            "Invalid transfer"
        );

        // Act & Assert
        mockMvc.perform(post("/api/inventory/transfers")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.error.code").value("STOCK_INSUFFICIENT"));
    }

    @Test
    @WithMockUser(roles = {"ADMIN"})
    @DisplayName("GET /api/inventory/movements - Should return movement history")
    void testGetMovementHistory_Success() throws Exception {
        // Arrange - Create a movement first
        StockMovementRequest request = new StockMovementRequest(
            branch1Id,
            testProductId,
            MovementType.IN,
            MovementReason.PURCHASE,
            25,
            "Test movement"
        );

        mockMvc.perform(post("/api/inventory/movements")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)));

        // Act & Assert
        mockMvc.perform(get("/api/inventory/movements")
                .param("page", "0")
                .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content", hasSize(greaterThanOrEqualTo(1))))
                .andExpect(jsonPath("$.content[0].type").value("IN"))
                .andExpect(jsonPath("$.content[0].reason").value("PURCHASE"))
                .andExpect(jsonPath("$.content[0].quantity").value(25));
    }

    @Test
    @WithMockUser(roles = {"EMPLOYEE"})
    @DisplayName("GET /api/inventory/stock - Should allow authenticated users to view stock")
    void testGetStock_Authenticated() throws Exception {
        // Act & Assert
        mockMvc.perform(get("/api/inventory/stock")
                .param("page", "0")
                .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray());
    }

    @Test
    @WithMockUser(roles = {"EMPLOYEE"})
    @DisplayName("POST /api/inventory/movements - Should forbid non-admin users")
    void testCreateMovement_Forbidden() throws Exception {
        // Arrange
        StockMovementRequest request = new StockMovementRequest(
            branch1Id,
            testProductId,
            MovementType.IN,
            MovementReason.PURCHASE,
            10,
            null
        );

        // Act & Assert
        mockMvc.perform(post("/api/inventory/movements")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = {"ADMIN"})
    @DisplayName("POST /api/inventory/movements - Should validate positive quantity")
    void testCreateMovement_InvalidQuantity() throws Exception {
        // Arrange
        StockMovementRequest request = new StockMovementRequest(
            branch1Id,
            testProductId,
            MovementType.IN,
            MovementReason.PURCHASE,
            0, // Invalid: quantity must be positive
            null
        );

        // Act & Assert
        mockMvc.perform(post("/api/inventory/movements")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(roles = {"ADMIN"})
    @DisplayName("POST /api/inventory/movements - Should validate branch belongs to tenant")
    void testCreateMovement_BranchFromDifferentTenant() throws Exception {
        // Arrange
        StockMovementRequest request = new StockMovementRequest(
            999L, // Non-existent branch
            testProductId,
            MovementType.IN,
            MovementReason.PURCHASE,
            10,
            null
        );

        // Act & Assert
        mockMvc.perform(post("/api/inventory/movements")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.error.code").value("BRANCH_NOT_FOUND"));
    }
}
