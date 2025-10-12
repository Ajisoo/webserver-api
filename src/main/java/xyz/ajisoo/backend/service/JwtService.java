package xyz.ajisoo.backend.service;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.Key;
import java.util.Date;
import java.util.function.Function;

@Service
public class JwtService {

    @Value("${cookie.verification}")
    private boolean verify;

    @Value("${jwt.secret.path}")
    private String secretKeyPath;

    @Value("${jwt.access-token.validity}")
    private long accessTokenValidity;

    @Value("${jwt.refresh-token.validity}")
    private long refreshTokenValidity;

    private String loadedSecret;

    public String generateAccessToken(String subject) {
        return generateToken(subject, accessTokenValidity);
    }

    public String generateRefreshToken(String subject) {
        return generateToken(subject, refreshTokenValidity);
    }

    private String generateToken(String subject, long validity) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + validity);

        return Jwts.builder()
                .setSubject(subject)
                .setIssuedAt(now)
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
