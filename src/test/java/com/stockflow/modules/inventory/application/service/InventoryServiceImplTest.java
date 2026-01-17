package com.stockflow.modules.inventory.application.service;

import com.stockflow.modules.branches.domain.model.Branch;
import com.stockflow.modules.branches.domain.repository.BranchRepository;
import com.stockflow.modules.catalog.domain.model.Product;
import com.stockflow.modules.catalog.domain.repository.ProductRepository;
import com.stockflow.modules.inventory.application.dto.StockMovementRequest;
import com.stockflow.modules.inventory.application.dto.TransferStockRequest;
import com.stockflow.modules.inventory.application.mapper.InventoryMapper;
import com.stockflow.modules.inventory.domain.model.BranchProductStock;
import com.stockflow.modules.inventory.domain.model.MovementReason;
import com.stockflow.modules.inventory.domain.model.MovementType;
import com.stockflow.modules.inventory.domain.repository.BranchProductStockRepository;
import com.stockflow.modules.inventory.domain.repository.StockMovementRepository;
import com.stockflow.shared.domain.exception.InsufficientStockException;
import com.stockflow.shared.domain.exception.ValidationException;
import com.stockflow.shared.infrastructure.security.TenantContext;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("InventoryServiceImpl - Unit Tests")
class InventoryServiceImplTest {

    @Mock
    private BranchProductStockRepository stockRepository;

    @Mock
    private StockMovementRepository movementRepository;

    @Mock
    private BranchRepository branchRepository;

    @Mock
    private ProductRepository productRepository;

    @Mock
    private InventoryMapper inventoryMapper;

    @InjectMocks
    private InventoryServiceImpl inventoryService;

    @AfterEach
    void tearDown() {
        TenantContext.clear();
    }

    @Test
    @DisplayName("createMovement - Should fail when stock is insufficient for OUT movement")
    void createMovement_ShouldFailWhenStockInsufficient() {
        Long tenantId = 1L;
        Long branchId = 10L;
        Long productId = 20L;

        TenantContext.setTenantId(tenantId);

        Branch branch = new Branch(tenantId, "Branch A", "BR-A");
        branch.setId(branchId);

        Product product = new Product(tenantId, "Product A", "SKU-1", Product.UnitOfMeasure.UN);
        product.setId(productId);
        product.setActive(true);

        BranchProductStock stock = new BranchProductStock(tenantId, branchId, productId, 5);

        StockMovementRequest request = new StockMovementRequest(
            branchId,
            productId,
            MovementType.OUT,
            MovementReason.SALE,
            10,
            "Sale beyond stock"
        );

        when(branchRepository.findByIdAndTenantIdIncludingInactive(branchId, tenantId))
            .thenReturn(Optional.of(branch));
        when(productRepository.findByIdAndTenantIdIncludingInactive(productId, tenantId))
            .thenReturn(Optional.of(product));
        when(stockRepository.findByTenantIdAndBranchIdAndProductId(branchId, productId, tenantId))
            .thenReturn(Optional.of(stock));

        assertThatThrownBy(() -> inventoryService.createMovement(request))
            .isInstanceOf(InsufficientStockException.class);

        verifyNoInteractions(movementRepository);
        verifyNoInteractions(inventoryMapper);
    }

    @Test
    @DisplayName("transferStock - Should fail when source and destination are the same")
    void transferStock_ShouldFailWhenSameBranch() {
        Long tenantId = 1L;
        Long branchId = 10L;

        TenantContext.setTenantId(tenantId);

        TransferStockRequest request = new TransferStockRequest(
            branchId,
            branchId,
            30L,
            5,
            "Invalid transfer"
        );

        assertThatThrownBy(() -> inventoryService.transferStock(request))
            .isInstanceOf(ValidationException.class)
            .hasMessageContaining("Source and destination branches cannot be the same");

        verifyNoInteractions(branchRepository, productRepository, stockRepository, movementRepository);
    }
}
