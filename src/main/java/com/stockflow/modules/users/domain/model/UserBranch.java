package com.stockflow.modules.users.domain.model;

import com.stockflow.shared.domain.model.BaseEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Objects;

/**
 * Relacionamento entre User e Branch para ABAC (Attribute-Based Access Control).
 * Permite escopo de acesso por filial para usuários não-admin.
 */
@Entity
@Table(name = "user_branches",
    indexes = {
        @Index(name = "idx_user_branches_user", columnList = "user_id"),
        @Index(name = "idx_user_branches_branch", columnList = "branch_id"),
        @Index(name = "idx_user_branches_tenant", columnList = "tenant_id")
    },
    uniqueConstraints = {
        @UniqueConstraint(name = "uk_user_branches_user_branch", columnNames = {"user_id", "branch_id"})
    }
)
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserBranch extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "branch_id", nullable = false)
    private Long branchId;

    @Column(name = "assigned_at", nullable = false)
    private LocalDateTime assignedAt;

    @Column(name = "assigned_by")
    private Long assignedBy;

    @PrePersist
    protected void onCreate() {
        if (assignedAt == null) {
            assignedAt = LocalDateTime.now();
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        UserBranch userBranch = (UserBranch) o;
        return Objects.equals(user, userBranch.user) &&
               Objects.equals(branchId, userBranch.branchId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(user, branchId);
    }

    @Override
    public String toString() {
        return "UserBranch{" +
                "id=" + getId() +
                ", userId=" + (user != null ? user.getId() : null) +
                ", branchId=" + branchId +
                ", assignedAt=" + assignedAt +
                '}';
    }
}
