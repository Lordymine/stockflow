package com.stockflow.modules.catalog.application.mapper;

import com.stockflow.modules.catalog.application.dto.CategoryRequest;
import com.stockflow.modules.catalog.application.dto.CategoryResponse;
import com.stockflow.modules.catalog.domain.model.Category;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;

/**
 * MapStruct mapper for Category entity and DTOs.
 *
 * <p>Provides mapping between domain entities and DTOs, following the
 * mapper pattern to keep domain logic isolated from presentation layer.</p>
 */
@Mapper(componentModel = "spring")
public interface CategoryMapper {

    /**
     * Converts a Category entity to CategoryResponse DTO.
     *
     * @param category the category entity
     * @return the category response DTO
     */
    CategoryResponse toResponse(Category category);

    /**
     * Converts a CategoryRequest DTO to Category entity.
     *
     * @param request  the category request DTO
     * @param tenantId the tenant ID to set
     * @return the category entity
     */
    Category toEntity(CategoryRequest request, Long tenantId);

    /**
     * Updates a Category entity from CategoryRequest DTO.
     *
     * @param request the category request DTO
     * @param entity  the target entity to update
     */
    void updateEntityFromRequest(CategoryRequest request, @MappingTarget Category entity);
}
