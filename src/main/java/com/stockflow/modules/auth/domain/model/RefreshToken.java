package com.stockflow.modules.auth.domain.model;

import com.stockflow.shared.domain.model.BaseEntity;
import jakarta.persistence.*;

import java.time.LocalDateTime;

/**
 * RefreshToken entity representing a persisted refresh token for JWT rotation.
 *
 * <p>Refresh tokens are stored as hashes in the database to enable:
 * <ul>
 *   <li>Token revocation (logout)</li>
 *   <li>Token expiration validation</li>
 *   <li>Token rotation (new token on refresh)</li>
 *   <li>Detection of token reuse attempts</li>
 * </ul>
 *
 * <p>Following ADR-0003, refresh tokens have a long TTL (7 days) and are
 * hashed before storage for security.</p>
 */
@Entity
@Table(name = "refresh_tokens")
public class RefreshToken extends BaseEntity {

    /**
     * The user who owns this refresh token.
     */
    @Column(name = "user_id", nullable = false)
    private Long userId;

    /**
     * Hash of the refresh token value.
     * Never store the raw token, only a one-way hash.
     */
    @Column(name = "token_hash", nullable = false)
    private String tokenHash;

    /**
     * Expiration timestamp for this refresh token.
     * Tokens past this date cannot be used for refresh.
     */
    @Column(name = "expires_at", nullable = false)
    private LocalDateTime expiresAt;

    /**
     * Timestamp when the token was revoked (logout).
     * Null if the token is still valid.
     */
    @Column(name = "revoked_at")
    private LocalDateTime revokedAt;

    /**
     * Default constructor for JPA.
     */
    protected RefreshToken() {
    }

    /**
     * Constructor for creating a new refresh token.
     *
     * @param tenantId  the tenant ID
     * @param userId    the user ID
     * @param tokenHash the hash of the token
     * @param expiresAt the expiration timestamp
     */
    public RefreshToken(Long tenantId, Long userId, String tokenHash, LocalDateTime expiresAt) {
        setTenantId(tenantId);
        this.userId = userId;
        this.tokenHash = tokenHash;
        this.expiresAt = expiresAt;
    }

    /**
     * Checks if the token is expired.
     *
     * @return true if the token has expired
     */
    public boolean isExpired() {
        return LocalDateTime.now().isAfter(expiresAt);
    }

    /**
     * Checks if the token is revoked.
     *
     * @return true if the token has been revoked
     */
    public boolean isRevoked() {
        return revokedAt != null;
    }

    /**
     * Checks if the token is valid (not expired and not revoked).
     *
     * @return true if the token is valid
     */
    public boolean isValid() {
        return !isExpired() && !isRevoked();
    }

    /**
     * Revokes the token by setting the revoked timestamp.
     * This is called during logout or token rotation.
     */
    public void revoke() {
        this.revokedAt = LocalDateTime.now();
    }

    // Getters and Setters

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getTokenHash() {
        return tokenHash;
    }

    public void setTokenHash(String tokenHash) {
        this.tokenHash = tokenHash;
    }

    public LocalDateTime getExpiresAt() {
        return expiresAt;
    }

    public void setExpiresAt(LocalDateTime expiresAt) {
        this.expiresAt = expiresAt;
    }

    public LocalDateTime getRevokedAt() {
        return revokedAt;
    }

    public void setRevokedAt(LocalDateTime revokedAt) {
        this.revokedAt = revokedAt;
    }

    @Override
    public String toString() {
        return String.format("RefreshToken[id=%d, tenantId=%d, userId=%d, expiresAt=%s, isRevoked=%s]",
            getId(), getTenantId(), userId, expiresAt, isRevoked());
    }
}
