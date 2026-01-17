package com.stockflow.modules.auth.application.service;

import com.stockflow.modules.auth.application.dto.*;
import com.stockflow.modules.auth.domain.model.RefreshToken;
import com.stockflow.modules.auth.domain.repository.RefreshTokenRepository;
import com.stockflow.modules.tenants.domain.model.Tenant;
import com.stockflow.modules.tenants.domain.repository.TenantRepository;
import com.stockflow.modules.users.domain.model.Role;
import com.stockflow.modules.users.domain.model.RoleEnum;
import com.stockflow.modules.users.domain.model.User;
import com.stockflow.modules.users.domain.repository.RoleRepository;
import com.stockflow.modules.users.domain.repository.UserRepository;
import com.stockflow.shared.domain.exception.ConflictException;
import com.stockflow.shared.domain.exception.ForbiddenException;
import com.stockflow.shared.domain.exception.UnauthorizedException;
import com.stockflow.shared.domain.exception.ValidationException;
import com.stockflow.shared.infrastructure.security.JwtService;
import com.stockflow.shared.infrastructure.security.TenantContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDateTime;
import java.util.HexFormat;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Implementation of authentication service.
 *
 * <p>Handles authentication, token management, and tenant signup.</p>
 */
@Service
public class AuthServiceImpl implements AuthService {

    private static final Logger logger = LoggerFactory.getLogger(AuthServiceImpl.class);

    private final AuthenticationManager authenticationManager;
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final TenantRepository tenantRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final JwtService jwtService;
    private final PasswordEncoder passwordEncoder;

    public AuthServiceImpl(AuthenticationManager authenticationManager,
                          UserRepository userRepository,
                          RoleRepository roleRepository,
                          TenantRepository tenantRepository,
                          RefreshTokenRepository refreshTokenRepository,
                          JwtService jwtService,
                          PasswordEncoder passwordEncoder) {
        this.authenticationManager = authenticationManager;
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.tenantRepository = tenantRepository;
        this.refreshTokenRepository = refreshTokenRepository;
        this.jwtService = jwtService;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    @Transactional
    public LoginResponse login(LoginRequest request) {
        logger.info("Login attempt for email: {}", request.email());

        // Load user from database FIRST to get tenant ID
        // This is necessary because authentication needs tenant context
        User user = userRepository.findByEmail(request.email())
            .orElseThrow(() -> new UnauthorizedException("AUTH_INVALID_CREDENTIALS",
                "Invalid email or password"));

        if (!user.isActive()) {
            throw new UnauthorizedException("AUTH_INVALID_CREDENTIALS",
                "User account is inactive");
        }

        // Set tenant context BEFORE authentication so UserDetailsService works
        Long tenantId = user.getTenantId();
        TenantContext.setTenantId(tenantId);

        // Now authenticate with Spring Security
        try {
            authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                    request.email(),
                    request.password()
                )
            );
        } catch (AuthenticationException ex) {
            throw new UnauthorizedException("AUTH_INVALID_CREDENTIALS",
                "Invalid email or password");
        }

        // Generate tokens
        String accessToken = generateAccessToken(user);
        String refreshToken = generateRefreshToken(user);

        logger.info("Login successful for user: {} in tenant: {}", user.getEmail(), tenantId);

        return new LoginResponse(
            accessToken,
            refreshToken,
            "Bearer",
            jwtService.getAccessTokenExpiration() / 1000,
            new LoginResponse.UserInfo(
                user.getId(),
                user.getName(),
                user.getEmail(),
                getRoleNames(user),
                getBranchIds(user)
            )
        );
    }

    @Override
    @Transactional
    public RefreshTokenResponse refreshToken(RefreshTokenRequest request) {
        logger.debug("Token refresh requested");

        // Validate token
        if (!jwtService.validateToken(request.refreshToken())) {
            throw new UnauthorizedException("AUTH_REFRESH_TOKEN_INVALID",
                "Invalid refresh token");
        }

        // Extract user info
        Long tenantId = jwtService.extractTenantId(request.refreshToken());
        Long userId = jwtService.extractUserId(request.refreshToken());

        // Find refresh token in database
        String tokenHash = hashToken(request.refreshToken());
        RefreshToken refreshTokenEntity = refreshTokenRepository
            .findValidByTokenHash(tokenHash, LocalDateTime.now())
            .orElseThrow(() -> new UnauthorizedException("AUTH_REFRESH_TOKEN_INVALID",
                "Refresh token not found or revoked"));

        // Load user
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new UnauthorizedException("AUTH_REFRESH_TOKEN_INVALID",
                "User not found"));

        if (!user.getTenantId().equals(tenantId)) {
            throw new ForbiddenException("FORBIDDEN_TENANT_ACCESS",
                "Tenant mismatch in refresh token");
        }

        if (!user.isActive()) {
            throw new UnauthorizedException("AUTH_REFRESH_TOKEN_INVALID",
                "User account is inactive");
        }

        // Revoke old refresh token (rotation)
        refreshTokenEntity.revoke();
        refreshTokenRepository.save(refreshTokenEntity);

        // Generate new tokens
        String accessToken = generateAccessToken(user);
        String newRefreshToken = generateRefreshToken(user);

        logger.debug("Token refresh successful for user: {}", user.getEmail());

        return new RefreshTokenResponse(
            accessToken,
            newRefreshToken,
            "Bearer",
            jwtService.getAccessTokenExpiration() / 1000
        );
    }

    @Override
    @Transactional
    public void logout(LogoutRequest request) {
        logger.debug("Logout requested");

        // Validate token
        if (!jwtService.validateToken(request.refreshToken())) {
            // Even if token is invalid, try to revoke it
            logger.warn("Logout called with invalid refresh token");
            return;
        }

        // Find and revoke refresh token
        String tokenHash = hashToken(request.refreshToken());
        RefreshToken refreshTokenEntity = refreshTokenRepository
            .findValidByTokenHash(tokenHash, LocalDateTime.now())
            .orElse(null);

        if (refreshTokenEntity != null) {
            refreshTokenEntity.revoke();
            refreshTokenRepository.save(refreshTokenEntity);
            logger.info("Refresh token revoked for user: {}", refreshTokenEntity.getUserId());
        }
    }

    @Override
    @Transactional
    public SignupResponse signup(SignupRequest request) {
        logger.info("Tenant signup initiated");

        if (tenantRepository.existsBySlug(request.tenantSlug())) {
            throw new ConflictException("TENANT_SLUG_ALREADY_EXISTS",
                "Tenant slug already exists");
        }

        if (userRepository.existsActiveByEmail(request.adminEmail())) {
            throw new ConflictException("USER_EMAIL_ALREADY_EXISTS",
                "User with this email already exists");
        }

        // Create tenant
        Tenant tenant = new Tenant(request.tenantName(), request.tenantSlug());
        tenant = tenantRepository.save(tenant);
        logger.info("Created tenant: {} (ID: {})", tenant.getName(), tenant.getId());

        // Set tenant context for user creation
        TenantContext.setTenantId(tenant.getId());

        // Create admin user
        User admin = new User(
            tenant.getId(),
            request.adminName(),
            request.adminEmail(),
            passwordEncoder.encode(request.adminPassword())
        );

        // Assign ADMIN role
        Role adminRole = roleRepository.findByName(RoleEnum.ADMIN)
            .orElseThrow(() -> new ValidationException("VALIDATION_ERROR",
                "ADMIN role not found in database"));
        admin.addRole(adminRole);

        admin = userRepository.save(admin);
        logger.info("Created admin user: {} (ID: {})", admin.getEmail(), admin.getId());

        // Generate tokens
        String accessToken = generateAccessToken(admin);
        String refreshToken = generateRefreshToken(admin);

        logger.info("Tenant signup completed successfully");

        return new SignupResponse(
            new SignupResponse.TenantInfo(
                tenant.getId(),
                tenant.getName(),
                tenant.getSlug()
            ),
            new SignupResponse.UserInfo(
                admin.getId(),
                admin.getName(),
                admin.getEmail(),
                getRoleNames(admin)
            ),
            new SignupResponse.TokenInfo(
                accessToken,
                refreshToken,
                "Bearer",
                jwtService.getAccessTokenExpiration() / 1000
            )
        );
    }

    // Private helper methods

    private String generateAccessToken(User user) {
        List<String> roles = getRoleNames(user);
        List<Long> branchIds = getBranchIds(user);

        return jwtService.generateAccessToken(
            user.getTenantId(),
            user.getId(),
            user.getEmail(),
            roles,
            branchIds
        );
    }

    private String generateRefreshToken(User user) {
        String refreshToken = jwtService.generateRefreshToken(
            user.getTenantId(),
            user.getId()
        );

        // Store hashed refresh token
        String tokenHash = hashToken(refreshToken);
        LocalDateTime expiresAt = LocalDateTime.now()
            .plusSeconds(jwtService.getRefreshTokenExpiration() / 1000);

        RefreshToken refreshTokenEntity = new RefreshToken(
            user.getTenantId(),
            user.getId(),
            tokenHash,
            expiresAt
        );

        refreshTokenRepository.save(refreshTokenEntity);

        return refreshToken;
    }

    private String hashToken(String token) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(token.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(hash);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 not available", e);
        }
    }

    private List<String> getRoleNames(User user) {
        return user.getRoles().stream()
            .map(role -> role.getName().name())
            .collect(Collectors.toList());
    }

    private List<Long> getBranchIds(User user) {
        return user.getBranches().stream()
            .map(branch -> branch.getId())
            .collect(Collectors.toList());
    }
}
