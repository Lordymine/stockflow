package com.stockflow.modules.dashboard.application.service;

import com.stockflow.modules.branches.domain.model.Branch;
import com.stockflow.modules.branches.domain.repository.BranchRepository;
import com.stockflow.modules.dashboard.application.dto.DashboardOverviewResponse;
import com.stockflow.modules.dashboard.domain.model.DashboardMetrics;
import com.stockflow.modules.dashboard.domain.model.TopProductMovement;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

/**
 * Unit tests for {@link DashboardServiceImpl}.
 *
 * <p>Tests the service layer logic without external dependencies:</p>
 * <ul>
 *   <li>Fetching tenant-wide metrics from repository</li>
 *   <li>Fetching branch-specific metrics from repository</li>
 *   <li>Building dashboard overview responses</li>
 *   <li>Caching behavior (delegated to Spring)</li>
 *   <li>Interaction with repository layer</li>
 * </ul>
 *
 * <p><strong>Test Strategy:</strong></p>
 * <ul>
 *   <li>Uses Mockito to mock repository dependencies</li>
 *   <li>Verifies correct interaction with repository</li>
 *   <li>Validates response DTO construction</li>
 *   <li>Tests both tenant-wide and branch-specific scenarios</li>
 * </ul>
 *
 * @see DashboardServiceImpl
 * @see DashboardRepository
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("DashboardServiceImpl - Unit Tests")
class DashboardServiceImplTest {

    @Mock
    private DashboardCacheService dashboardCacheService;

    @Mock
    private BranchRepository branchRepository;

    @InjectMocks
    private DashboardServiceImpl dashboardService;

    private Long tenantId;
    private Long branchId;
    private DashboardMetrics tenantMetrics;
    private DashboardMetrics branchMetrics;
    private List<TopProductMovement> topProducts;
    private List<TopProductMovement> branchTopProducts;

    @BeforeEach
    void setUp() {
        tenantId = 1L;
        branchId = 10L;

        // Setup tenant metrics
        tenantMetrics = new DashboardMetrics(
            150,  // totalActiveProducts
            12,   // lowStockItems
            5430, // totalMovements
            230   // recentMovements
        );

        // Setup branch metrics
        branchMetrics = new DashboardMetrics(
            80,   // totalActiveProducts
            5,    // lowStockItems
            1250, // totalMovements
            85    // recentMovements
        );

        // Setup top products for tenant
        topProducts = List.of(
            new TopProductMovement(1L, "Wireless Mouse", "WM-001", 45, 320),
            new TopProductMovement(2L, "USB-C Cable", "UC-002", 38, 285),
            new TopProductMovement(3L, "Keyboard RGB", "KR-003", 32, 195)
        );

        // Setup top products for branch
        branchTopProducts = List.of(
            new TopProductMovement(1L, "Wireless Mouse", "WM-001", 20, 150),
            new TopProductMovement(2L, "USB-C Cable", "UC-002", 15, 110)
        );
    }

    @Test
    @DisplayName("getOverview() - Should return metrics from repository")
    void getOverview_ShouldReturnMetricsFromRepository() {
        // Given - Repository returns metrics and top products
        when(dashboardCacheService.getMetrics(tenantId)).thenReturn(tenantMetrics);
        when(dashboardCacheService.getTopProducts(tenantId)).thenReturn(topProducts);

        // When - Service is called
        DashboardOverviewResponse response = dashboardService.getOverview(tenantId);

        // Then - Response is correctly built
        assertThat(response).isNotNull();
        assertThat(response.metrics().totalActiveProducts()).isEqualTo(150);
        assertThat(response.metrics().lowStockItems()).isEqualTo(12);
        assertThat(response.metrics().totalMovements()).isEqualTo(5430);
        assertThat(response.metrics().recentMovements()).isEqualTo(230);

        assertThat(response.topProducts()).hasSize(3);
        assertThat(response.topProducts().get(0).productId()).isEqualTo(1L);
        assertThat(response.topProducts().get(0).productName()).isEqualTo("Wireless Mouse");
        assertThat(response.topProducts().get(0).movementCount()).isEqualTo(45);
        assertThat(response.topProducts().get(0).totalQuantity()).isEqualTo(320);

        // Verify repository interactions
        verify(dashboardCacheService, times(1)).getMetrics(tenantId);
        verify(dashboardCacheService, times(1)).getTopProducts(tenantId);
        verifyNoMoreInteractions(dashboardCacheService);
        verifyNoInteractions(branchRepository);
    }

    @Test
    @DisplayName("getOverview() - Should cache result (delegated to Spring)")
    void getOverview_ShouldCacheResult() {
        // Given - Repository returns metrics
        when(dashboardCacheService.getMetrics(tenantId)).thenReturn(tenantMetrics);
        when(dashboardCacheService.getTopProducts(tenantId)).thenReturn(topProducts);

        // When - Service is called twice (should use cache on second call)
        DashboardOverviewResponse response1 = dashboardService.getOverview(tenantId);
        DashboardOverviewResponse response2 = dashboardService.getOverview(tenantId);

        // Then - Responses are equal
        assertThat(response1).isNotNull();
        assertThat(response2).isNotNull();
        assertThat(response1.metrics().totalActiveProducts())
            .isEqualTo(response2.metrics().totalActiveProducts());

        // Note: In a real scenario with Spring AOP, cache would prevent second repository call
        // In unit tests without Spring context, we verify the service logic is correct
        verify(dashboardCacheService, times(2)).getMetrics(tenantId);
        verify(dashboardCacheService, times(2)).getTopProducts(tenantId);
        verifyNoInteractions(branchRepository);
    }

    @Test
    @DisplayName("getOverviewByBranch() - Should return branch metrics from repository")
    void getOverviewByBranch_ShouldReturnBranchMetrics() {
        // Given - Repository returns branch metrics
        Branch branch = new Branch(tenantId, "Branch A", "BR-A");
        branch.setId(branchId);
        when(branchRepository.findByIdAndTenantIdIncludingInactive(branchId, tenantId))
            .thenReturn(Optional.of(branch));
        when(dashboardCacheService.getMetricsByBranch(tenantId, branchId)).thenReturn(branchMetrics);
        when(dashboardCacheService.getTopProductsByBranch(tenantId, branchId))
            .thenReturn(branchTopProducts);

        // When - Service is called
        DashboardOverviewResponse response = dashboardService.getOverviewByBranch(tenantId, branchId);

        // Then - Response is correctly built with branch data
        assertThat(response).isNotNull();
        assertThat(response.metrics().totalActiveProducts()).isEqualTo(80);
        assertThat(response.metrics().lowStockItems()).isEqualTo(5);
        assertThat(response.metrics().totalMovements()).isEqualTo(1250);
        assertThat(response.metrics().recentMovements()).isEqualTo(85);

        assertThat(response.topProducts()).hasSize(2);
        assertThat(response.topProducts().get(0).productId()).isEqualTo(1L);
        assertThat(response.topProducts().get(0).productName()).isEqualTo("Wireless Mouse");
        assertThat(response.topProducts().get(0).movementCount()).isEqualTo(20);
        assertThat(response.topProducts().get(0).totalQuantity()).isEqualTo(150);

        // Verify repository interactions
        verify(branchRepository, times(1)).findByIdAndTenantIdIncludingInactive(branchId, tenantId);
        verify(dashboardCacheService, times(1)).getMetricsByBranch(tenantId, branchId);
        verify(dashboardCacheService, times(1)).getTopProductsByBranch(tenantId, branchId);
        verifyNoMoreInteractions(dashboardCacheService);
    }

    @Test
    @DisplayName("getOverviewByBranch() - Should cache result with branch key (delegated to Spring)")
    void getOverviewByBranch_ShouldCacheResultWithBranchKey() {
        // Given - Repository returns branch metrics
        Branch branch = new Branch(tenantId, "Branch A", "BR-A");
        branch.setId(branchId);
        when(branchRepository.findByIdAndTenantIdIncludingInactive(branchId, tenantId))
            .thenReturn(Optional.of(branch));
        when(dashboardCacheService.getMetricsByBranch(tenantId, branchId)).thenReturn(branchMetrics);
        when(dashboardCacheService.getTopProductsByBranch(tenantId, branchId))
            .thenReturn(branchTopProducts);

        // When - Service is called twice for same branch
        DashboardOverviewResponse response1 = dashboardService.getOverviewByBranch(tenantId, branchId);
        DashboardOverviewResponse response2 = dashboardService.getOverviewByBranch(tenantId, branchId);

        // Then - Responses are equal
        assertThat(response1).isNotNull();
        assertThat(response2).isNotNull();
        assertThat(response1.metrics().totalActiveProducts())
            .isEqualTo(response2.metrics().totalActiveProducts());
        assertThat(response1.metrics().lowStockItems())
            .isEqualTo(response2.metrics().lowStockItems());

        // Note: In real scenario with Spring AOP, cache would use key "tenantId:branchId"
        verify(branchRepository, times(2)).findByIdAndTenantIdIncludingInactive(branchId, tenantId);
        verify(dashboardCacheService, times(2)).getMetricsByBranch(tenantId, branchId);
        verify(dashboardCacheService, times(2)).getTopProductsByBranch(tenantId, branchId);
    }

    @Test
    @DisplayName("getOverview() - Should handle empty top products list")
    void getOverview_ShouldHandleEmptyTopProductsList() {
        // Given - Repository returns metrics but no top products
        when(dashboardCacheService.getMetrics(tenantId)).thenReturn(tenantMetrics);
        when(dashboardCacheService.getTopProducts(tenantId)).thenReturn(List.of());

        // When - Service is called
        DashboardOverviewResponse response = dashboardService.getOverview(tenantId);

        // Then - Response has metrics but empty top products
        assertThat(response).isNotNull();
        assertThat(response.metrics().totalActiveProducts()).isEqualTo(150);
        assertThat(response.topProducts()).isEmpty();

        verify(dashboardCacheService, times(1)).getMetrics(tenantId);
        verify(dashboardCacheService, times(1)).getTopProducts(tenantId);
        verifyNoInteractions(branchRepository);
    }

    @Test
    @DisplayName("getOverviewByBranch() - Should handle different branches independently")
    void getOverviewByBranch_ShouldHandleDifferentBranchesIndependently() {
        // Given - Different branches have different metrics
        Long branch1Id = 10L;
        Long branch2Id = 20L;

        DashboardMetrics branch1Metrics = new DashboardMetrics(80, 5, 1250, 85);
        DashboardMetrics branch2Metrics = new DashboardMetrics(60, 3, 980, 62);

        List<TopProductMovement> branch1TopProducts = List.of(
            new TopProductMovement(1L, "Wireless Mouse", "WM-001", 20, 150)
        );

        List<TopProductMovement> branch2TopProducts = List.of(
            new TopProductMovement(2L, "USB-C Cable", "UC-002", 18, 125)
        );

        Branch branch1 = new Branch(tenantId, "Branch 1", "BR-1");
        branch1.setId(branch1Id);
        Branch branch2 = new Branch(tenantId, "Branch 2", "BR-2");
        branch2.setId(branch2Id);
        when(branchRepository.findByIdAndTenantIdIncludingInactive(branch1Id, tenantId))
            .thenReturn(Optional.of(branch1));
        when(branchRepository.findByIdAndTenantIdIncludingInactive(branch2Id, tenantId))
            .thenReturn(Optional.of(branch2));
        when(dashboardCacheService.getMetricsByBranch(tenantId, branch1Id)).thenReturn(branch1Metrics);
        when(dashboardCacheService.getMetricsByBranch(tenantId, branch2Id)).thenReturn(branch2Metrics);
        when(dashboardCacheService.getTopProductsByBranch(tenantId, branch1Id))
            .thenReturn(branch1TopProducts);
        when(dashboardCacheService.getTopProductsByBranch(tenantId, branch2Id))
            .thenReturn(branch2TopProducts);

        // When - Service is called for different branches
        DashboardOverviewResponse response1 = dashboardService.getOverviewByBranch(tenantId, branch1Id);
        DashboardOverviewResponse response2 = dashboardService.getOverviewByBranch(tenantId, branch2Id);

        // Then - Responses are independent and correct
        assertThat(response1.metrics().totalActiveProducts()).isEqualTo(80);
        assertThat(response2.metrics().totalActiveProducts()).isEqualTo(60);

        assertThat(response1.topProducts().get(0).productName()).isEqualTo("Wireless Mouse");
        assertThat(response2.topProducts().get(0).productName()).isEqualTo("USB-C Cable");

        verify(branchRepository, times(1)).findByIdAndTenantIdIncludingInactive(branch1Id, tenantId);
        verify(branchRepository, times(1)).findByIdAndTenantIdIncludingInactive(branch2Id, tenantId);
        verify(dashboardCacheService, times(1)).getMetricsByBranch(tenantId, branch1Id);
        verify(dashboardCacheService, times(1)).getMetricsByBranch(tenantId, branch2Id);
        verify(dashboardCacheService, times(1)).getTopProductsByBranch(tenantId, branch1Id);
        verify(dashboardCacheService, times(1)).getTopProductsByBranch(tenantId, branch2Id);
    }

    @Test
    @DisplayName("getOverview() - Should validate repository interaction order")
    void getOverview_ShouldValidateRepositoryInteractionOrder() {
        // Given - Repository returns metrics
        when(dashboardCacheService.getMetrics(tenantId)).thenReturn(tenantMetrics);
        when(dashboardCacheService.getTopProducts(tenantId)).thenReturn(topProducts);

        // When - Service is called
        dashboardService.getOverview(tenantId);

        // Then - Repository methods are called in correct order
        var inOrder = inOrder(dashboardCacheService);
        inOrder.verify(dashboardCacheService).getMetrics(tenantId);
        inOrder.verify(dashboardCacheService).getTopProducts(tenantId);
        inOrder.verifyNoMoreInteractions();
        verifyNoInteractions(branchRepository);
    }

    @Test
    @DisplayName("getOverviewByBranch() - Should validate repository interaction order")
    void getOverviewByBranch_ShouldValidateRepositoryInteractionOrder() {
        // Given - Repository returns branch metrics
        Branch branch = new Branch(tenantId, "Branch A", "BR-A");
        branch.setId(branchId);
        when(branchRepository.findByIdAndTenantIdIncludingInactive(branchId, tenantId))
            .thenReturn(Optional.of(branch));
        when(dashboardCacheService.getMetricsByBranch(tenantId, branchId)).thenReturn(branchMetrics);
        when(dashboardCacheService.getTopProductsByBranch(tenantId, branchId))
            .thenReturn(branchTopProducts);

        // When - Service is called
        dashboardService.getOverviewByBranch(tenantId, branchId);

        // Then - Repository methods are called in correct order
        var inOrder = inOrder(branchRepository, dashboardCacheService);
        inOrder.verify(branchRepository).findByIdAndTenantIdIncludingInactive(branchId, tenantId);
        inOrder.verify(dashboardCacheService).getMetricsByBranch(tenantId, branchId);
        inOrder.verify(dashboardCacheService).getTopProductsByBranch(tenantId, branchId);
        inOrder.verifyNoMoreInteractions();
    }
}
