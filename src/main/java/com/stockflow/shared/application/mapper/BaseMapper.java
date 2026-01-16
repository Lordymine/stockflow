package com.stockflow.shared.application.mapper;

import org.mapstruct.MappingTarget;

import java.util.List;

/**
 * Base mapper interface for all MapStruct mappers.
 *
 * <p>Provides common mapping methods that all domain mappers should support.</p>
 *
 * <p>All specific mappers should extend this interface to inherit
 * standard mapping behavior and maintain consistency.</p>
 *
 * @param <D> the DTO type
 * @param <E> the entity type
 */
public interface BaseMapper<D, E> {

    /**
     * Converts an entity to its DTO representation.
     *
     * @param entity the entity to convert
     * @return the corresponding DTO
     */
    D toDto(E entity);

    /**
     * Converts a DTO to its entity representation.
     *
     * @param dto the DTO to convert
     * @return the corresponding entity
     */
    E toEntity(D dto);

    /**
     * Converts a list of entities to a list of DTOs.
     *
     * @param entities the list of entities to convert
     * @return the corresponding list of DTOs
     */
    default List<D> toDtoList(List<E> entities) {
        return entities.stream()
            .map(this::toDto)
            .toList();
    }

    /**
     * Converts a list of DTOs to a list of entities.
     *
     * @param dtos the list of DTOs to convert
     * @return the corresponding list of entities
     */
    default List<E> toEntityList(List<D> dtos) {
        return dtos.stream()
            .map(this::toEntity)
            .toList();
    }

    /**
     * Updates an existing entity with data from a DTO.
     *
     * <p>This is useful for partial updates where you want to merge
     * DTO data into an existing entity instance.</p>
     *
     * @param dto    the source DTO
     * @param entity the target entity to update
     */
    default void updateEntityFromDto(D dto, @MappingTarget E entity) {
        // Default implementation does nothing
        // Override in specific mappers if needed
    }
}
