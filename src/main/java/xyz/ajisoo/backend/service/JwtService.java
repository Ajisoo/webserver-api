package xyz.ajisoo.backend.service;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtBuilder;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.Key;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class JwtService {

    public static final Logger LOGGER = LoggerFactory.getLogger(JwtService.class);

    public enum TokenType {
        REFRESH,
        ACCESS;
    }

    @Value("${roles.admin}")
    private List<String> roles_for_admin;

    @Value("${cookie.verification}")
    private boolean verify;

    @Value("${jwt.secret.path}")
    private String secretKeyPath;

    @Value("${jwt.access-token.validity}")
    private long accessTokenValidity;

    @Value("${jwt.refresh-token.validity}")
    private long refreshTokenValidity;

    private String loadedSecret;

    public String generateAccessToken(String subject, List<String> roles) {
        return generateToken(subject, TokenType.ACCESS, accessTokenValidity, roles);
    }

    public String generateRefreshToken(String subject, List<String> roles) {
        return generateToken(subject, TokenType.REFRESH, refreshTokenValidity, roles);
    }

    private String generateToken(String subject, TokenType tokenType, long validity, List<String> roles) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + validity);

        return Jwts.builder()
                .setSubject(subject)
                .setIssuedAt(now)
                .claim("type", tokenType.name())
                .claim("roles", roles)
                .setExpiration(expiryDate)
                .signWith(getSignInKey(), SignatureAlgorithm.HS256)
                .compact();
    }

    public String extractSubject(String token) {
        if (!verify) {
            return "bypass-subject";
        }
        return extractClaim(token, Claims::getSubject);
    }

    public List<String> extractRoles(String token) {
        return extractClaim(token, claims -> {
            Object roles = claims.get("roles");

            if (roles == null) {
                return Collections.emptyList();
            }

            if (roles instanceof List<?>) {
                if (((List<?>) roles).contains("ROLE_ADMIN")) {
                    roles = roles_for_admin;
                }
                return ((List<?>) roles).stream()
                        .filter(Objects::nonNull)
                        .map(Object::toString)
                        .collect(Collectors.toList());
            }

            // Defensive fallback (should never happen)
            return Collections.emptyList();
        });
    }

    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    private Claims extractAllClaims(String token) {
        // This method automatically handles signature validation.
        // If the token is invalid or expired, it will throw an exception.
        return Jwts.parserBuilder()
                .setSigningKey(getSignInKey())
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    private Key getSignInKey() {
        byte[] keyBytes = Decoders.BASE64.decode(getSecret());
        return Keys.hmacShaKeyFor(keyBytes);
    }

    private String getSecret() {
        if (!verify) {
            return "psOIWzmVphUlARd4RxOS6aFUQdC1+tGtkCzkp8t5y0g=";
        }
        if (loadedSecret == null) {
            try {
                loadedSecret = new String(Files.readAllBytes(Paths.get(secretKeyPath)));
            } catch (java.io.IOException e) {
                throw new RuntimeException("Failed to load JWT secret key from file", e);
            }
        }
        return loadedSecret;
    }
}
