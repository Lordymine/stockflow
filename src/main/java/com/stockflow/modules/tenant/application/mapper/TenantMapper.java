package com.stockflow.modules.tenant.application.mapper;

import com.stockflow.modules.tenant.application.dto.TenantRequest;
import com.stockflow.modules.tenant.application.dto.TenantResponse;
import com.stockflow.modules.tenant.domain.model.Tenant;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

import java.util.List;

@Mapper(
    componentModel = "spring",
    nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE
)
public interface TenantMapper {

    Tenant toEntity(TenantRequest request);

    TenantResponse toResponse(Tenant tenant);

    List<TenantResponse> toResponseList(List<Tenant> tenants);

    void updateEntityFromRequest(TenantRequest request, @MappingTarget Tenant tenant);
}
