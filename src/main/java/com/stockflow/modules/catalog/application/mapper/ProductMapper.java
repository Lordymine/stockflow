package com.stockflow.modules.catalog.application.mapper;

import com.stockflow.modules.catalog.application.dto.ProductRequest;
import com.stockflow.modules.catalog.application.dto.ProductResponse;
import com.stockflow.modules.catalog.domain.model.Product;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.Named;

/**
 * MapStruct mapper for Product entity and DTOs.
 *
 * <p>Provides mapping between domain entities and DTOs, following the
 * mapper pattern to keep domain logic isolated from presentation layer.</p>
 */
@Mapper(componentModel = "spring")
public interface ProductMapper {

    /**
     * Converts a Product entity to ProductResponse DTO.
     *
     * @param product the product entity
     * @return the product response DTO
     */
    @Mapping(source = "unitOfMeasure", target = "unitOfMeasure", qualifiedByName = "enumToString")
    ProductResponse toResponse(Product product);

    /**
     * Converts a ProductRequest DTO to Product entity.
     *
     * @param request  the product request DTO
     * @param tenantId the tenant ID to set
     * @return the product entity
     */
    @Mapping(source = "tenantId", target = "tenantId")
    @Mapping(source = "unitOfMeasure", target = "unitOfMeasure", qualifiedByName = "stringToEnum")
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "version", ignore = true)
    @Mapping(target = "isActive", ignore = true)
    Product toEntity(ProductRequest request, Long tenantId);

    /**
     * Updates a Product entity from ProductRequest DTO.
     *
     * @param request the product request DTO
     * @param entity  the target entity to update
     */
    @Mapping(source = "unitOfMeasure", target = "unitOfMeasure", qualifiedByName = "stringToEnum")
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "tenantId", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "version", ignore = true)
    @Mapping(target = "isActive", ignore = true)
    void updateEntityFromRequest(ProductRequest request, @MappingTarget Product entity);

    /**
     * Converts UnitOfMeasure enum to String.
     *
     * @param unitOfMeasure the enum value
     * @return the string representation
     */
    @Named("enumToString")
    default String enumToString(Product.UnitOfMeasure unitOfMeasure) {
        return unitOfMeasure != null ? unitOfMeasure.name() : "UN";
    }

    /**
     * Converts String to UnitOfMeasure enum.
     *
     * @param unitOfMeasure the string value
     * @return the enum value
     */
    @Named("stringToEnum")
    default Product.UnitOfMeasure stringToEnum(String unitOfMeasure) {
        if (unitOfMeasure == null || unitOfMeasure.trim().isEmpty()) {
            return Product.UnitOfMeasure.UN;
        }
        try {
            return Product.UnitOfMeasure.valueOf(unitOfMeasure.trim().toUpperCase());
        } catch (IllegalArgumentException e) {
            return Product.UnitOfMeasure.UN;
        }
    }
}
