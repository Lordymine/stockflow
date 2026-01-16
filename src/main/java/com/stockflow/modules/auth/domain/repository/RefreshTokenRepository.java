package com.stockflow.modules.auth.domain.repository;

import com.stockflow.modules.auth.domain.model.RefreshToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {

    /**
     * Busca refresh token pelo token string
     */
    Optional<RefreshToken> findByToken(String token);

    /**
     * Busca todos os refresh tokens ativos de um usuário
     */
    @Query("SELECT rt FROM RefreshToken rt " +
           "WHERE rt.userId = :userId " +
           "AND rt.isRevoked = false " +
           "AND rt.expiryDate > :now")
    List<RefreshToken> findActiveTokensByUserId(
        @Param("userId") Long userId,
        @Param("now") LocalDateTime now
    );

    /**
     * Busca refresh token ativo pelo token string
     */
    @Query("SELECT rt FROM RefreshToken rt " +
           "WHERE rt.token = :token " +
           "AND rt.isRevoked = false " +
           "AND rt.expiryDate > :now")
    Optional<RefreshToken> findActiveByToken(
        @Param("token") String token,
        @Param("now") LocalDateTime now
    );

    /**
     * Verifica se token existe e está ativo
     */
    @Query("SELECT CASE WHEN COUNT(rt) > 0 THEN true ELSE false END " +
           "FROM RefreshToken rt " +
           "WHERE rt.token = :token " +
           "AND rt.isRevoked = false " +
           "AND rt.expiryDate > :now")
    boolean isTokenActive(
        @Param("token") String token,
        @Param("now") LocalDateTime now
    );

    /**
     * Deleta todos os refresh tokens expirados
     */
    @Query("DELETE FROM RefreshToken rt " +
           "WHERE rt.expiryDate < :date")
    void deleteExpiredTokens(@Param("date") LocalDateTime date);

    /**
     * Deleta todos os refresh tokens de um usuário
     */
    void deleteByUserId(Long userId);

    /**
     * Deleta todos os refresh tokens revogados
     */
    void deleteByIsRevokedTrue();

    /**
     * Conta refresh tokens ativos de um usuário
     */
    @Query("SELECT COUNT(rt) FROM RefreshToken rt " +
           "WHERE rt.userId = :userId " +
           "AND rt.isRevoked = false " +
           "AND rt.expiryDate > :now")
    long countActiveTokensByUserId(
        @Param("userId") Long userId,
        @Param("now") LocalDateTime now
    );
}
