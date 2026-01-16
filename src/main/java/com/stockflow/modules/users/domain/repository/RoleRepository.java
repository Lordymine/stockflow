package com.stockflow.modules.users.domain.repository;

import com.stockflow.modules.users.domain.model.Role;
import com.stockflow.modules.users.domain.model.RoleEnum;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface RoleRepository extends JpaRepository<Role, Long> {

    /**
     * Busca role por nome e tenant
     */
    Optional<Role> findByNameAndTenantId(RoleEnum name, Long tenantId);

    /**
     * Busca todas as roles do tenant
     */
    List<Role> findByTenantId(Long tenantId);

    /**
     * Verifica se role existe no tenant
     */
    boolean existsByNameAndTenantId(RoleEnum name, Long tenantId);

    /**
     * Conta roles do tenant
     */
    long countByTenantId(Long tenantId);
}
