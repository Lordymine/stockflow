package com.stockflow.modules.auth.domain.repository;

import com.stockflow.modules.auth.domain.model.RefreshToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Repository interface for RefreshToken entity.
 *
 * <p>Provides data access operations for refresh tokens with tenant isolation.</p>
 */
@Repository
public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {

    /**
     * Finds a valid (non-expired, non-revoked) refresh token by its hash.
     *
     * @param tokenHash the hash of the token
     * @return Optional containing the token if found and valid
     */
    @Query("SELECT rt FROM RefreshToken rt WHERE rt.tokenHash = :tokenHash AND rt.revokedAt IS NULL AND rt.expiresAt > :now")
    Optional<RefreshToken> findValidByTokenHash(@Param("tokenHash") String tokenHash, @Param("now") LocalDateTime now);

    /**
     * Finds all valid tokens for a user.
     *
     * @param userId the user ID
     * @param now    current timestamp
     * @return list of valid tokens
     */
    @Query("SELECT rt FROM RefreshToken rt WHERE rt.userId = :userId AND rt.revokedAt IS NULL AND rt.expiresAt > :now")
    List<RefreshToken> findValidByUserId(@Param("userId") Long userId, @Param("now") LocalDateTime now);

    /**
     * Finds all tokens (including expired/revoked) for a user.
     *
     * @param userId the user ID
     * @return list of all tokens
     */
    List<RefreshToken> findByUserId(Long userId);

    /**
     * Revokes all valid tokens for a user.
     *
     * @param userId the user ID
     * @param now    current timestamp
     * @return number of tokens revoked
     */
    @Query("UPDATE RefreshToken rt SET rt.revokedAt = :now WHERE rt.userId = :userId AND rt.revokedAt IS NULL")
    int revokeAllByUserId(@Param("userId") Long userId, @Param("now") LocalDateTime now);

    /**
     * Deletes all expired tokens.
     *
     * @param now current timestamp
     * @return number of tokens deleted
     */
    @Query("DELETE FROM RefreshToken rt WHERE rt.expiresAt < :now")
    int deleteExpired(@Param("now") LocalDateTime now);

    /**
     * Deletes all revoked tokens older than the specified date.
     * Useful for cleanup of old revoked tokens.
     *
     * @param date the cutoff date
     * @return number of tokens deleted
     */
    @Query("DELETE FROM RefreshToken rt WHERE rt.revokedAt IS NOT NULL AND rt.revokedAt < :date")
    int deleteRevokedOlderThan(@Param("date") LocalDateTime date);
}
