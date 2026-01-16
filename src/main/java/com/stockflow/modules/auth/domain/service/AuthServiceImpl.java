package com.stockflow.modules.auth.domain.service;

import com.stockflow.modules.auth.application.dto.*;
import com.stockflow.modules.auth.domain.model.RefreshToken;
import com.stockflow.modules.auth.domain.repository.RefreshTokenRepository;
import com.stockflow.shared.domain.exception.NotFoundException;
import com.stockflow.shared.infrastructure.security.JwtService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class AuthServiceImpl implements AuthService {

    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final RefreshTokenRepository refreshTokenRepository;

    @Override
    public LoginResponse login(LoginRequest request) {
        log.info("Login attempt for email: {}", request.email());

        // Autentica usuário
        Authentication authentication = authenticationManager.authenticate(
            new UsernamePasswordAuthenticationToken(
                request.email(),
                request.password()
            )
        );

        // Obtém UserDetails do usuário autenticado
        UserDetails userDetails = (UserDetails) authentication.getPrincipal();

        // TODO: Extrair userId e tenantId do usuário (Sprint 04)
        // Por enquanto, valores placeholder
        Long userId = 1L;
        Long tenantId = 1L;

        // Prepara claims extras
        Map<String, Object> extraClaims = new HashMap<>();
        extraClaims.put("userId", userId);
        extraClaims.put("tenantId", tenantId);

        // Gera access token
        String accessToken = jwtService.generateToken(extraClaims, userDetails);

        // Gera refresh token
        String refreshTokenString = generateRefreshTokenString();

        // Persiste refresh token no banco
        RefreshToken refreshToken = RefreshToken.builder()
            .token(refreshTokenString)
            .userId(userId)
            .tenantId(tenantId)
            .expiryDate(LocalDateTime.now().plusDays(7)) // 7 dias
            .isRevoked(false)
            .build();

        refreshTokenRepository.save(refreshToken);

        log.info("User {} logged in successfully", request.email());

        // Retorna response
        return new LoginResponse(
            accessToken,
            refreshTokenString,
            "Bearer",
            jwtService.getJwtExpirationSeconds(),
            userId,
            tenantId,
            request.email()
        );
    }

    @Override
    public RefreshTokenResponse refreshToken(RefreshTokenRequest request) {
        log.info("Token refresh attempt");

        // Busca refresh token no banco
        RefreshToken refreshToken = refreshTokenRepository
            .findActiveByToken(request.refreshToken(), LocalDateTime.now())
            .orElseThrow(() -> new NotFoundException("Invalid or expired refresh token"));

        // TODO: Carregar usuário do banco (Sprint 04)
        // Por enquanto, usa um UserDetails placeholder
        // Necessário para gerar novo access token

        // Cria novo access token
        Map<String, Object> extraClaims = new HashMap<>();
        extraClaims.put("userId", refreshToken.getUserId());
        extraClaims.put("tenantId", refreshToken.getTenantId());

        // TODO: Obter UserDetails do usuário (Sprint 04)
        // Por enquanto, gera token com email hardcoded
        UserDetails userDetails = org.springframework.security.core.userdetails.User
            .withUsername("admin@stockflow.com")
            .password("")
            .authorities(java.util.Collections.emptyList())
            .build();

        String newAccessToken = jwtService.generateToken(extraClaims, userDetails);

        // Gera novo refresh token (rotação de token)
        String newRefreshTokenString = generateRefreshTokenString();

        // Revoga token antigo
        refreshToken.revoke();
        refreshTokenRepository.save(refreshToken);

        // Cria novo refresh token
        RefreshToken newRefreshToken = RefreshToken.builder()
            .token(newRefreshTokenString)
            .userId(refreshToken.getUserId())
            .tenantId(refreshToken.getTenantId())
            .expiryDate(LocalDateTime.now().plusDays(7))
            .isRevoked(false)
            .build();

        refreshTokenRepository.save(newRefreshToken);

        log.info("Token refreshed successfully");

        return new RefreshTokenResponse(
            newAccessToken,
            newRefreshTokenString,
            "Bearer",
            jwtService.getJwtExpirationSeconds()
        );
    }

    @Override
    public void logout(LogoutRequest request) {
        log.info("Logout attempt");

        // Busca refresh token
        RefreshToken refreshToken = refreshTokenRepository
            .findByToken(request.refreshToken())
            .orElseThrow(() -> new NotFoundException("Refresh token not found"));

        // Revoga token
        refreshToken.revoke();
        refreshTokenRepository.save(refreshToken);

        log.info("User logged out successfully");
    }

    /**
     * Gera string única para refresh token (UUID)
     */
    private String generateRefreshTokenString() {
        return UUID.randomUUID().toString();
    }
}
