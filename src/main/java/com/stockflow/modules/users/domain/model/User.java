package com.stockflow.modules.users.domain.model;

import com.stockflow.shared.domain.model.BaseEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;
import java.util.Objects;

/**
 * User do sistema com RBAC.
 *
 * TODO: No Sprint 04.6 será adicionado relacionamento com Branches
 */
@Entity
@Table(name = "users",
    indexes = {
        @Index(name = "idx_users_email", columnList = "email"),
        @Index(name = "idx_users_tenant", columnList = "tenant_id"),
        @Index(name = "idx_users_active", columnList = "is_active")
    },
    uniqueConstraints = {
        @UniqueConstraint(name = "uk_users_email", columnNames = {"email", "tenant_id"})
    }
)
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class User extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "name", nullable = false, length = 255)
    private String name;

    @Column(name = "email", nullable = false, length = 255)
    private String email;

    @Column(name = "password", nullable = false, length = 255)
    private String password;

    @Column(name = "phone", length = 20)
    private String phone;

    @Column(name = "is_active", nullable = false)
    @Builder.Default
    private Boolean isActive = true;

    @Column(name = "is_account_locked", nullable = false)
    @Builder.Default
    private Boolean isAccountLocked = false;

    @Column(name = "failed_login_attempts")
    @Builder.Default
    private Integer failedLoginAttempts = 0;

    @Column(name = "last_login_at")
    private LocalDateTime lastLoginAt;

    @Column(name = "password_changed_at")
    private LocalDateTime passwordChangedAt;

    @Column(name = "created_at", nullable = false, updatable = false)
    @CreationTimestamp
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    @UpdateTimestamp
    private LocalDateTime updatedAt;

    // Bidirectional relationship com UserRole
    @Builder.Default
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<UserRole> userRoles = new HashSet<>();

    // Bidirectional relationship com UserBranch
    @Builder.Default
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<UserBranch> userBranches = new HashSet<>();

    /**
     * Adiciona role ao user
     */
    public void addRole(UserRole userRole) {
        userRoles.add(userRole);
        userRole.setUser(this);
    }

    /**
     * Remove role do user
     */
    public void removeRole(UserRole userRole) {
        userRoles.remove(userRole);
        userRole.setUser(null);
    }

    /**
     * Adiciona branch ao user
     */
    public void addBranch(UserBranch userBranch) {
        userBranches.add(userBranch);
        userBranch.setUser(this);
    }

    /**
     * Remove branch do user
     */
    public void removeBranch(UserBranch userBranch) {
        userBranches.remove(userBranch);
        userBranch.setUser(null);
    }

    /**
     * Verifica se user tem role específica
     */
    public boolean hasRole(RoleEnum role) {
        return userRoles.stream()
            .anyMatch(ur -> ur.getRole().getName().equals(role));
    }

    /**
     * Retorna todas as roles do user
     */
    public Set<RoleEnum> getRoles() {
        return userRoles.stream()
            .map(ur -> ur.getRole().getName())
            .collect(java.util.stream.Collectors.toSet());
    }

    /**
     * Incrementa tentativas falhas de login
     */
    public void incrementFailedLoginAttempts() {
        this.failedLoginAttempts++;
        if (this.failedLoginAttempts >= 5) {
            this.isAccountLocked = true;
        }
    }

    /**
     * Reseta tentativas falhas de login
     */
    public void resetFailedLoginAttempts() {
        this.failedLoginAttempts = 0;
        this.isAccountLocked = false;
    }

    /**
     * Verifica se conta está bloqueada
     */
    public boolean isLocked() {
        return Boolean.TRUE.equals(isAccountLocked);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        User user = (User) o;
        return Objects.equals(id, user.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "User{" +
                "id=" + getId() +
                ", name='" + name + '\'' +
                ", email='" + email + '\'' +
                ", isActive=" + isActive +
                ", tenantId=" + getTenantId() +
                '}';
    }
}
