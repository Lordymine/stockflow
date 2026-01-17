package com.stockflow.modules.inventory.infrastructure.web;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.stockflow.modules.inventory.application.dto.StockMovementCreateRequest;
import com.stockflow.modules.inventory.application.dto.TransferStockRequest;
import com.stockflow.modules.inventory.domain.model.MovementReason;
import com.stockflow.modules.inventory.domain.model.MovementType;
import com.stockflow.modules.inventory.domain.repository.BranchProductStockRepository;
import com.stockflow.modules.inventory.domain.repository.StockMovementRepository;
import com.stockflow.modules.branches.domain.model.Branch;
import com.stockflow.modules.branches.domain.repository.BranchRepository;
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
import org.springframework.test.web.servlet.request.RequestPostProcessor;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

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
@Transactional
class InventoryControllerIntegrationTest extends TestcontainersIntegrationTest {

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
    private TenantRepository tenantRepository;

    @Autowired
    private UserRepository userRepository;

    private Long testTenantId;
    private Long branch1Id;
    private Long branch2Id;
    private Long testProductId;
    private RequestPostProcessor adminUser;
    private RequestPostProcessor staffUser;

    @BeforeEach
    void setUp() throws Exception {
        // Clean up
        stockRepository.deleteAll();
        movementRepository.deleteAll();
        branchRepository.deleteAll();
        userRepository.deleteAll();

        Tenant tenant = tenantRepository.save(new Tenant("Test Tenant", "test-tenant"));
        testTenantId = tenant.getId();

        // Create two test branches
        Branch branch1 = new Branch(testTenantId, "Filial Centro", "CENTRO");
        branch1.setAddress("Rua A, 123");
        branch1 = branchRepository.save(branch1);
        branch1Id = branch1.getId();

        Branch branch2 = new Branch(testTenantId, "Filial Norte", "NORTE");
        branch2.setAddress("Rua B, 456");
        branch2 = branchRepository.save(branch2);
        branch2Id = branch2.getId();

        adminUser = TestSecurityUtils.admin(testTenantId, List.of(branch1Id, branch2Id));
        staffUser = TestSecurityUtils.staff(testTenantId, List.of(branch1Id));

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

        var result = mockMvc.perform(post("/api/v1/products")
                .with(adminUser)
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(productJson))
                .andExpect(status().isCreated())
                .andReturn();

        String response = result.getResponse().getContentAsString();
        testProductId = objectMapper.readTree(response).get("data").get("id").asLong();

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

    @Test
    @DisplayName("GET /api/v1/branches/{branchId}/stock - Should return stock by branch")
    void testGetStockByBranch_WithPagination_Success() throws Exception {
        // Act & Assert
        mockMvc.perform(get("/api/v1/branches/" + branch1Id + "/stock")
                .with(adminUser)
                .param("page", "0")
                .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.items").isArray())
                .andExpect(jsonPath("$.data.items", hasSize(1)))
                .andExpect(jsonPath("$.data.items[0].branchId").value(branch1Id))
                .andExpect(jsonPath("$.data.items[0].productId").value(testProductId))
                .andExpect(jsonPath("$.data.items[0].quantity").value(100))
                .andExpect(jsonPath("$.meta.totalItems").value(1));
    }

    @Test
    @DisplayName("GET /api/v1/branches/{branchId}/stock - Should return stock by branch")
    void testGetStockByBranch_Success() throws Exception {
        // Act & Assert
        mockMvc.perform(get("/api/v1/branches/" + branch2Id + "/stock")
                .with(adminUser))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.items").isArray())
                .andExpect(jsonPath("$.data.items", hasSize(1)))
                .andExpect(jsonPath("$.data.items[0].branchId").value(branch2Id))
                .andExpect(jsonPath("$.data.items[0].productId").value(testProductId))
                .andExpect(jsonPath("$.data.items[0].quantity").value(50));
    }

    @Test
    @DisplayName("GET /api/v1/branches/{branchId}/stock/{productId} - Should return specific stock")
    void testGetSpecificStock_Success() throws Exception {
        // Act & Assert
        mockMvc.perform(get("/api/v1/branches/" + branch1Id + "/stock/" + testProductId)
                .with(adminUser))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.branchId").value(branch1Id))
                .andExpect(jsonPath("$.data.productId").value(testProductId))
                .andExpect(jsonPath("$.data.quantity").value(100))
                .andExpect(jsonPath("$.data.version").isNumber());
    }

    @Test
    @DisplayName("POST /api/v1/branches/{branchId}/movements - Should create IN movement")
    void testCreateMovement_IN_Success() throws Exception {
        // Arrange
        StockMovementCreateRequest request = new StockMovementCreateRequest(
            testProductId,
            MovementType.IN,
            MovementReason.PURCHASE,
            50,
            "Initial stock purchase"
        );

        // Act & Assert
        mockMvc.perform(post("/api/v1/branches/" + branch1Id + "/movements")
                .with(adminUser)
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.branchId").value(branch1Id))
                .andExpect(jsonPath("$.data.productId").value(testProductId))
                .andExpect(jsonPath("$.data.type").value("IN"))
                .andExpect(jsonPath("$.data.reason").value("PURCHASE"))
                .andExpect(jsonPath("$.data.quantity").value(50))
                .andExpect(jsonPath("$.data.createdByUserId").isNumber());

        // Verify stock was updated
        mockMvc.perform(get("/api/v1/branches/" + branch1Id + "/stock/" + testProductId)
                .with(adminUser))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.quantity").value(150)); // 100 + 50
    }

    @Test
    @DisplayName("POST /api/v1/branches/{branchId}/movements - Should create OUT movement with sufficient stock")
    void testCreateMovement_OUT_Success() throws Exception {
        // Arrange
        StockMovementCreateRequest request = new StockMovementCreateRequest(
            testProductId,
            MovementType.OUT,
            MovementReason.SALE,
            30,
            "Sale to customer"
        );

        // Act & Assert
        mockMvc.perform(post("/api/v1/branches/" + branch1Id + "/movements")
                .with(adminUser)
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.type").value("OUT"))
                .andExpect(jsonPath("$.data.quantity").value(30));

        // Verify stock was decreased
        mockMvc.perform(get("/api/v1/branches/" + branch1Id + "/stock/" + testProductId)
                .with(adminUser))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.quantity").value(70)); // 100 - 30
    }

    @Test
    @DisplayName("POST /api/v1/branches/{branchId}/movements - Should fail OUT movement with insufficient stock")
    void testCreateMovement_OUT_InsufficientStock() throws Exception {
        // Arrange
        StockMovementCreateRequest request = new StockMovementCreateRequest(
            testProductId,
            MovementType.OUT,
            MovementReason.SALE,
            150, // More than available (100)
            "Invalid sale"
        );

        // Act & Assert
        mockMvc.perform(post("/api/v1/branches/" + branch1Id + "/movements")
                .with(adminUser)
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.error.code").value("STOCK_INSUFFICIENT"))
                .andExpect(jsonPath("$.error.message").value(containsString("Insufficient stock")));
    }

    @Test
    @DisplayName("POST /api/v1/transfers - Should transfer stock between branches")
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
        mockMvc.perform(post("/api/v1/transfers")
                .with(adminUser)
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.sourceMovementId").isNumber())
                .andExpect(jsonPath("$.data.destinationMovementId").isNumber());

        // Verify stocks were updated
        mockMvc.perform(get("/api/v1/branches/" + branch1Id + "/stock/" + testProductId)
                .with(adminUser))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.quantity").value(70)); // 100 - 30

        mockMvc.perform(get("/api/v1/branches/" + branch2Id + "/stock/" + testProductId)
                .with(adminUser))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.quantity").value(80)); // 50 + 30
    }

    @Test
    @DisplayName("POST /api/v1/transfers - Should fail with insufficient stock")
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
        mockMvc.perform(post("/api/v1/transfers")
                .with(adminUser)
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.error.code").value("STOCK_INSUFFICIENT"));
    }

    @Test
    @DisplayName("GET /api/v1/branches/{branchId}/movements - Should return movement history")
    void testGetMovementHistory_Success() throws Exception {
        // Arrange - Create a movement first
        StockMovementCreateRequest request = new StockMovementCreateRequest(
            testProductId,
            MovementType.IN,
            MovementReason.PURCHASE,
            25,
            "Test movement"
        );

        mockMvc.perform(post("/api/v1/branches/" + branch1Id + "/movements")
                .with(adminUser)
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)));

        // Act & Assert
        mockMvc.perform(get("/api/v1/branches/" + branch1Id + "/movements")
                .with(adminUser)
                .param("page", "0")
                .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.items").isArray())
                .andExpect(jsonPath("$.data.items", hasSize(greaterThanOrEqualTo(1))))
                .andExpect(jsonPath("$.data.items[0].type").value("IN"))
                .andExpect(jsonPath("$.data.items[0].reason").value("PURCHASE"))
                .andExpect(jsonPath("$.data.items[0].quantity").value(25));
    }

    @Test
    @DisplayName("GET /api/v1/branches/{branchId}/stock - Should allow authenticated users to view stock")
    void testGetStock_Authenticated() throws Exception {
        // Act & Assert
        mockMvc.perform(get("/api/v1/branches/" + branch1Id + "/stock")
                .with(staffUser)
                .param("page", "0")
                .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.items").isArray());
    }

    @Test
    @DisplayName("POST /api/v1/branches/{branchId}/movements - Should forbid invalid staff movement")
    void testCreateMovement_Forbidden() throws Exception {
        // Arrange
        StockMovementCreateRequest request = new StockMovementCreateRequest(
            testProductId,
            MovementType.ADJUSTMENT,
            MovementReason.ADJUSTMENT_IN,
            10,
            null
        );

        // Act & Assert
        mockMvc.perform(post("/api/v1/branches/" + branch1Id + "/movements")
                .with(staffUser)
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.error.code").value("INSUFFICIENT_PRIVILEGES"));
    }

    @Test
    @DisplayName("POST /api/v1/branches/{branchId}/movements - Should validate positive quantity")
    void testCreateMovement_InvalidQuantity() throws Exception {
        // Arrange
        StockMovementCreateRequest request = new StockMovementCreateRequest(
            testProductId,
            MovementType.IN,
            MovementReason.PURCHASE,
            0, // Invalid: quantity must be positive
            null
        );

        // Act & Assert
        mockMvc.perform(post("/api/v1/branches/" + branch1Id + "/movements")
                .with(adminUser)
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("POST /api/v1/branches/{branchId}/movements - Should validate branch belongs to tenant")
    void testCreateMovement_BranchFromDifferentTenant() throws Exception {
        // Arrange
        StockMovementCreateRequest request = new StockMovementCreateRequest(
            testProductId,
            MovementType.IN,
            MovementReason.PURCHASE,
            10,
            null
        );

        // Act & Assert
        RequestPostProcessor adminWithInvalidBranch = TestSecurityUtils.admin(
            testTenantId,
            List.of(branch1Id, branch2Id, 999L)
        );

        mockMvc.perform(post("/api/v1/branches/999/movements")
                .with(adminWithInvalidBranch)
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.error.code").value("BRANCH_NOT_FOUND"));
    }
}
