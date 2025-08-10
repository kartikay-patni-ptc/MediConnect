package com.example.demo.utils;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.util.Base64;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

/**
 * Utility class for generating and validating JWT tokens.
 */
@Component
public class JwtUtil {

    private Key signingKey;
    private final long expirationTime;

    /**
     * Constructor that initializes the signing key using a secret key from properties.
     *
     * @param base64Secret The Base64-encoded secret key loaded from application.properties/yml
     * @param expirationTime The JWT expiration time in milliseconds
     */
    public JwtUtil(@Value("${jwt.secret}") String base64Secret,
                   @Value("${jwt.expiration}") long expirationTime) {
        try {
            this.signingKey = Keys.hmacShaKeyFor(Base64.getDecoder().decode(base64Secret));
        } catch (IllegalArgumentException e) {
            // Fallback: use the secret directly as bytes if Base64 decoding fails
            byte[] keyBytes = base64Secret.getBytes();
            // Ensure minimum key length for HS256 (256 bits = 32 bytes)
            if (keyBytes.length < 32) {
                byte[] newKeyBytes = new byte[32];
                System.arraycopy(keyBytes, 0, newKeyBytes, 0, Math.min(keyBytes.length, 32));
                // Fill remaining bytes with the original key repeated
                for (int i = keyBytes.length; i < 32; i++) {
                    newKeyBytes[i] = keyBytes[i % keyBytes.length];
                }
                keyBytes = newKeyBytes;
            }
            this.signingKey = Keys.hmacShaKeyFor(keyBytes);
        }
        this.expirationTime = expirationTime;
    }

    /**
     * Generates a JWT token for the provided user details.
     *
     * @param userDetails Authenticated user details
     * @return JWT token
     */
    public String generateToken(UserDetails userDetails) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("role", userDetails.getAuthorities().iterator().next().getAuthority());
        return createToken(claims, userDetails.getUsername());
    }

    /**
     * Extracts username from the JWT token.
     *
     * @param token JWT token
     * @return Username
     */
    public String getUsernameFromToken(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    /**
     * Validates the token against the provided user details.
     *
     * @param token JWT token
     * @param userDetails Authenticated user details
     * @return True if valid, else false
     */
    public boolean validateToken(String token, UserDetails userDetails) {
        final String username = getUsernameFromToken(token);
        return username.equals(userDetails.getUsername()) && !isTokenExpired(token);
    }

    /**
     * Checks whether the token has expired.
     *
     * @param token JWT token
     * @return True if expired
     */
    public boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date());
    }

    // --- Private Methods ---

    private Date extractExpiration(String token) {
        return extractAllClaims(token).getExpiration();
    }

    private <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    private Claims extractAllClaims(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(signingKey)
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    private String createToken(Map<String, Object> claims, String subject) {
        return Jwts.builder()
                .setClaims(claims)
                .setSubject(subject)
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis() + expirationTime))
                .signWith(signingKey, SignatureAlgorithm.HS256)
                .compact();
    }
}