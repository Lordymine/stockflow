package com.stockflow.shared.infrastructure.persistence;

import com.stockflow.modules.users.application.service.BranchAccessService;
import com.stockflow.shared.infrastructure.security.TenantContext;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.TypedQuery;
import jakarta.persistence.criteria.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@Repository
@RequiredArgsConstructor
@Slf4j
public class BranchFilteringRepository {

    @PersistenceContext
    private final EntityManager entityManager;

    private final BranchAccessService branchAccessService;

    public <T> Page<T> findAllWithBranchFilter(
        Class<T> entityClass,
        String branchFieldName,
        Pageable pageable
    ) {
        Long tenantId = TenantContext.getTenantId();
        Set<Long> accessibleBranches = branchAccessService.getCurrentUserAccessibleBranches();

        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<T> query = cb.createQuery(entityClass);
        Root<T> root = query.from(entityClass);

        // Build predicates
        List<Predicate> predicates = new ArrayList<>();

        // Tenant filter
        predicates.add(cb.equal(root.get("tenantId"), tenantId));

        // Branch filter
        if (accessibleBranches.contains(-1L)) {
            // Admin: no branch filter
            log.debug("Admin user - applying no branch filter");
        } else {
            predicates.add(root.get(branchFieldName).in(accessibleBranches));
            log.debug("Filtering by accessible branches: {}", accessibleBranches);
        }

        query.where(predicates.toArray(new Predicate[0]));

        // Apply sorting
        if (pageable.getSort().isSorted()) {
            List<Order> orders = new ArrayList<>();
            pageable.getSort().forEach(order -> {
                Path<?> path = root.get(order.getProperty());
                orders.add(order.isAscending() ? cb.asc(path) : cb.desc(path));
            });
            query.orderBy(orders);
        }

        // Execute query
        TypedQuery<T> typedQuery = entityManager.createQuery(query);
        typedQuery.setFirstResult((int) pageable.getOffset());
        typedQuery.setMaxResults(pageable.getPageSize());

        List<T> results = typedQuery.getResultList();

        // Count total
        CriteriaQuery<Long> countQuery = cb.createQuery(Long.class);
        Root<T> countRoot = countQuery.from(entityClass);
        countQuery.select(cb.count(countRoot));
        countQuery.where(predicates.toArray(new Predicate[0]));

        Long total = entityManager.createQuery(countQuery).getSingleResult();

        return new PageImpl<>(results, pageable, total);
    }

    public <T> List<T> findAllWithBranchFilter(
        Class<T> entityClass,
        String branchFieldName
    ) {
        Long tenantId = TenantContext.getTenantId();
        Set<Long> accessibleBranches = branchAccessService.getCurrentUserAccessibleBranches();

        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<T> query = cb.createQuery(entityClass);
        Root<T> root = query.from(entityClass);

        // Build predicates
        List<Predicate> predicates = new ArrayList<>();

        // Tenant filter
        predicates.add(cb.equal(root.get("tenantId"), tenantId));

        // Branch filter
        if (accessibleBranches.contains(-1L)) {
            // Admin: no branch filter
            log.debug("Admin user - applying no branch filter");
        } else {
            predicates.add(root.get(branchFieldName).in(accessibleBranches));
            log.debug("Filtering by accessible branches: {}", accessibleBranches);
        }

        query.where(predicates.toArray(new Predicate[0]));

        return entityManager.createQuery(query).getResultList();
    }
}
