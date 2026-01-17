package com.stockflow.modules.branches.application.mapper;

import com.stockflow.modules.branches.application.dto.BranchRequest;
import com.stockflow.modules.branches.application.dto.BranchResponse;
import com.stockflow.modules.branches.domain.model.Branch;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

/**
 * MapStruct mapper for Branch entity and DTOs.
 */
@Mapper(componentModel = "spring")
public interface BranchMapper {

    BranchResponse toResponse(Branch branch);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "version", ignore = true)
    @Mapping(target = "isActive", ignore = true)
    Branch toEntity(BranchRequest request, Long tenantId);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "tenantId", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "version", ignore = true)
    @Mapping(target = "isActive", ignore = true)
    void updateEntityFromRequest(BranchRequest request, @MappingTarget Branch branch);
}
