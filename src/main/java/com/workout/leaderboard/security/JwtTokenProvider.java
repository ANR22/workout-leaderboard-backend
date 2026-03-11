package com.workout.leaderboard.security;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Date;

@Component
public class JwtTokenProvider {

    private final SecretKey key;
    private final long tokenValidityInMilliseconds;

    public JwtTokenProvider(@Value("${app.jwt.secret:MyVerySecureSecretKeyFor256BitHS256AlgorithmPleaseChangeInProduction}") String secret,
                           @Value("${app.jwt.expiration:300000}") long tokenValidityInSeconds) {
        // 300000ms = 5 minutes
        this.key = Keys.hmacShaKeyFor(secret.getBytes());
        this.tokenValidityInMilliseconds = tokenValidityInSeconds;
    }

    /**
     * Generate JWT token for the given email.
     * Token is valid for 5 minutes.
     */
    public String generateToken(String email, String fullName) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + tokenValidityInMilliseconds);

        return Jwts.builder()
                .subject(email)
                .claim("fullName", fullName)
                .issuedAt(now)
                .expiration(expiryDate)
            .signWith(key)
                .compact();
    }

    /**
     * Extract email from JWT token.
     */
    public String getEmailFromToken(String token) {
        try {
            return Jwts.parser()
                    .verifyWith(key)
                    .build()
                    .parseSignedClaims(token)
                    .getPayload()
                    .getSubject();
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * Validate JWT token.
     */
    public boolean validateToken(String token) {
        try {
            Jwts.parser()
                    .verifyWith(key)
                    .build()
                    .parseSignedClaims(token);
            return true;
        } catch (Exception e) {
            return false;
        }
    }
}
