package com.stockflow.shared.infrastructure.security;

import com.stockflow.modules.users.domain.model.RoleEnum;
import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Service for JWT token generation and validation.
 *
 * <p>This service handles:
 * <ul>
 *   <li>Generation of access tokens with user claims</li>
 *   <li>Generation of refresh tokens</li>
 *   <li>Validation and parsing of JWT tokens</li>
 *   <li>Extraction of claims (tenant ID, user ID, roles, branches)</li>
 * </ul>
 *
 * <p>Following ADR-0003, access tokens have a short TTL (15 minutes) and
 * contain tenant, user, roles, and branch IDs as claims.</p>
 */
@Service
public class JwtService {

    private static final Logger logger = LoggerFactory.getLogger(JwtService.class);

    /**
     * Claim key for tenant ID.
     */
    public static final String TENANT_ID_CLAIM = "tenantId";

    /**
     * Claim key for user ID.
     */
    public static final String USER_ID_CLAIM = "userId";

    /**
     * Claim key for user email.
     */
    public static final String EMAIL_CLAIM = "email";

    /**
     * Claim key for user roles.
     */
    public static final String ROLES_CLAIM = "roles";

    /**
     * Claim key for branch IDs.
     */
    public static final String BRANCHES_CLAIM = "branches";

    @Value("${app.jwt.secret}")
    private String jwtSecret;

    @Value("${app.jwt.expiration:900000}") // 15 minutes default
    private long jwtExpiration;

    @Value("${app.jwt.refresh-expiration:604800000}") // 7 days default
    private long refreshExpiration;

    private SecretKey signingKey;

    @PostConstruct
    public void init() {
        // Ensure the secret is long enough for HS256
        byte[] keyBytes = jwtSecret.getBytes(StandardCharsets.UTF_8);
        this.signingKey = Keys.hmacShaKeyFor(keyBytes);
        logger.info("JWT Service initialized with access token TTL: {} ms ({} minutes)",
            jwtExpiration, jwtExpiration / 60000);
    }

    /**
     * Generates an access token for a user.
     *
     * @param tenantId the tenant ID
     * @param userId   the user ID
     * @param email    the user's email
     * @param roles    list of role names
     * @param branchIds list of branch IDs the user has access to
     * @return the JWT access token
     */
    public String generateAccessToken(Long tenantId, Long userId, String email,
                                      List<String> roles, List<Long> branchIds) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + jwtExpiration);

        String rolesString = roles != null ? String.join(",", roles) : "";
        String branchesString = branchIds != null
            ? branchIds.stream().map(String::valueOf).collect(Collectors.joining(","))
            : "";

        return Jwts.builder()
            .subject(email)
            .claim(TENANT_ID_CLAIM, tenantId)
            .claim(USER_ID_CLAIM, userId)
            .claim(EMAIL_CLAIM, email)
            .claim(ROLES_CLAIM, rolesString)
            .claim(BRANCHES_CLAIM, branchesString)
            .issuedAt(now)
            .expiration(expiryDate)
            .signWith(signingKey)
            .compact();
    }

    /**
     * Generates a refresh token.
     * Refresh tokens are simpler and only contain user identification.
     *
     * @param tenantId the tenant ID
     * @param userId   the user ID
     * @return the refresh token
     */
    public String generateRefreshToken(Long tenantId, Long userId) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + refreshExpiration);

        return Jwts.builder()
            .claim(TENANT_ID_CLAIM, tenantId)
            .claim(USER_ID_CLAIM, userId)
            .issuedAt(now)
            .expiration(expiryDate)
            .signWith(signingKey)
            .compact();
    }

    /**
     * Validates a JWT token.
     *
     * @param token the token to validate
     * @return true if the token is valid
     */
    public boolean validateToken(String token) {
        try {
            Jwts.parser()
                .verifyWith(signingKey)
                .build()
                .parseSignedClaims(token);
            return true;
        } catch (SecurityException ex) {
            logger.error("Invalid JWT signature: {}", ex.getMessage());
        } catch (MalformedJwtException ex) {
            logger.error("Invalid JWT token: {}", ex.getMessage());
        } catch (ExpiredJwtException ex) {
            logger.error("Expired JWT token: {}", ex.getMessage());
        } catch (UnsupportedJwtException ex) {
            logger.error("Unsupported JWT token: {}", ex.getMessage());
        } catch (IllegalArgumentException ex) {
            logger.error("JWT claims string is empty: {}", ex.getMessage());
        }
        return false;
    }

    /**
     * Extracts all claims from a token.
     *
     * @param token the JWT token
     * @return the claims
     * @throws ExpiredJwtException if the token is expired
     * @throws JwtException       if the token is invalid
     */
    public Claims extractAllClaims(String token) {
        return Jwts.parser()
            .verifyWith(signingKey)
            .build()
            .parseSignedClaims(token)
            .getPayload();
    }

    /**
     * Extracts the tenant ID from a token.
     *
     * @param token the JWT token
     * @return the tenant ID
     */
    public Long extractTenantId(String token) {
        Claims claims = extractAllClaims(token);
        return claims.get(TENANT_ID_CLAIM, Long.class);
    }

    /**
     * Extracts the user ID from a token.
     *
     * @param token the JWT token
     * @return the user ID
     */
    public Long extractUserId(String token) {
        Claims claims = extractAllClaims(token);
        return claims.get(USER_ID_CLAIM, Long.class);
    }

    /**
     * Extracts the email from a token.
     *
     * @param token the JWT token
     * @return the email
     */
    public String extractEmail(String token) {
        Claims claims = extractAllClaims(token);
        return claims.get(EMAIL_CLAIM, String.class);
    }

    /**
     * Extracts the roles from a token.
     *
     * @param token the JWT token
     * @return list of role names
     */
    public List<String> extractRoles(String token) {
        Claims claims = extractAllClaims(token);
        String rolesString = claims.get(ROLES_CLAIM, String.class);
        if (rolesString == null || rolesString.isEmpty()) {
            return List.of();
        }
        return List.of(rolesString.split(","));
    }

    /**
     * Extracts the branch IDs from a token.
     *
     * @param token the JWT token
     * @return list of branch IDs
     */
    public List<Long> extractBranchIds(String token) {
        Claims claims = extractAllClaims(token);
        String branchesString = claims.get(BRANCHES_CLAIM, String.class);
        if (branchesString == null || branchesString.isEmpty()) {
            return List.of();
        }
        return List.of(branchesString.split(",")).stream()
            .map(Long::valueOf)
            .collect(Collectors.toList());
    }

    /**
     * Gets the expiration date from a token.
     *
     * @param token the JWT token
     * @return the expiration date
     */
    public Date extractExpiration(String token) {
        return extractAllClaims(token).getExpiration();
    }

    /**
     * Checks if a token is expired.
     *
     * @param token the JWT token
     * @return true if the token is expired
     */
    public boolean isTokenExpired(String token) {
        try {
            Date expiration = extractExpiration(token);
            return expiration.before(new Date());
        } catch (Exception e) {
            return true;
        }
    }

    /**
     * Gets the access token expiration time in milliseconds.
     *
     * @return the expiration time
     */
    public long getAccessTokenExpiration() {
        return jwtExpiration;
    }

    /**
     * Gets the refresh token expiration time in milliseconds.
     *
     * @return the expiration time
     */
    public long getRefreshTokenExpiration() {
        return refreshExpiration;
    }
}
