package com.stockflow.modules.inventory.application.mapper;

import com.stockflow.modules.inventory.application.dto.BranchStockResponse;
import com.stockflow.modules.inventory.application.dto.StockMovementRequest;
import com.stockflow.modules.inventory.application.dto.StockMovementResponse;
import com.stockflow.modules.inventory.domain.model.BranchProductStock;
import com.stockflow.modules.inventory.domain.model.StockMovement;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

/**
 * MapStruct mapper for Inventory entities and DTOs.
 *
 * <p>Provides mapping between domain entities and DTOs, following the
 * mapper pattern to keep domain logic isolated from presentation layer.</p>
 */
@Mapper(componentModel = "spring")
public interface InventoryMapper {

    /**
     * Converts a StockMovement entity to StockMovementResponse DTO.
     *
     * @param movement the stock movement entity
     * @return the stock movement response DTO
     */
    StockMovementResponse toResponse(StockMovement movement);

    /**
     * Converts a StockMovementRequest DTO to StockMovement entity.
     *
     * @param request   the stock movement request DTO
     * @param tenantId  the tenant ID to set
     * @param createdBy the user ID who created the movement
     * @return the stock movement entity
     */
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "tenantId", source = "tenantId")
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "version", ignore = true)
    @Mapping(target = "createdByUserId", source = "createdBy")
    StockMovement toEntity(StockMovementRequest request, Long tenantId, Long createdBy);

    /**
     * Converts a BranchProductStock entity to BranchStockResponse DTO.
     *
     * @param stock the branch product stock entity
     * @return the branch stock response DTO
     */
    BranchStockResponse toResponse(BranchProductStock stock);
}
