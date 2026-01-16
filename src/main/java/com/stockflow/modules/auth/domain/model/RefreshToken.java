package com.stockflow.modules.auth.domain.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.Objects;

@Entity
@Table(name = "refresh_tokens",
    indexes = {
        @Index(name = "idx_refresh_tokens_token", columnList = "token"),
        @Index(name = "idx_refresh_tokens_user_id", columnList = "user_id"),
        @Index(name = "idx_refresh_tokens_tenant_id", columnList = "tenant_id"),
        @Index(name = "idx_refresh_tokens_expiry_date", columnList = "expiry_date"),
        @Index(name = "idx_refresh_tokens_is_revoked", columnList = "is_revoked"),
        @Index(name = "idx_refresh_tokens_user_valid", columnList = "user_id,is_revoked,expiry_date")
    },
    uniqueConstraints = {
        @UniqueConstraint(name = "uk_refresh_tokens_token", columnNames = {"token"})
    }
)
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RefreshToken {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "token", nullable = false, unique = true, length = 512)
    private String token;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "tenant_id", nullable = false)
    private Long tenantId;

    @Column(name = "expiry_date", nullable = false)
    private LocalDateTime expiryDate;

    @Column(name = "is_revoked", nullable = false)
    @Builder.Default
    private Boolean isRevoked = false;

    @Column(name = "created_at", nullable = false, updatable = false)
    @CreationTimestamp
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    @UpdateTimestamp
    private LocalDateTime updatedAt;

    /**
     * Verifica se o token está expirado
     */
    public boolean isExpired() {
        return LocalDateTime.now().isAfter(expiryDate);
    }

    /**
     * Verifica se o token está ativo (não revogado e não expirado)
     */
    public boolean isActive() {
        return !isRevoked && !isExpired();
    }

    /**
     * Revoga o token
     */
    public void revoke() {
        this.isRevoked = true;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        RefreshToken that = (RefreshToken) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "RefreshToken{" +
                "id=" + id +
                ", userId=" + userId +
                ", tenantId=" + tenantId +
                ", isRevoked=" + isRevoked +
                ", expiryDate=" + expiryDate +
                ", isActive=" + isActive() +
                '}';
    }
}
