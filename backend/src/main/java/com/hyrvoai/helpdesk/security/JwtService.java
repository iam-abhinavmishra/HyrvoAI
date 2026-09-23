package com.hyrvoai.helpdesk.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

@Service
public class JwtService {

    private final SecretKey secretKey;

    public JwtService(
            @Value("${jwt.secret}") String secret
    ) {

        this.secretKey =
                Keys.hmacShaKeyFor(
                        secret.getBytes(
                                StandardCharsets.UTF_8
                        )
                );
    }

    /*
     * Existing local-login method.
     */
    public String generateToken(
            String email,
            String role
    ) {

        return generateToken(
                email,
                null,
                role
        );
    }

    /*
     * OAuth2/local login method that can also
     * store the user's name inside the JWT.
     */
    public String generateToken(
            String email,
            String name,
            String role
    ) {

        var builder =
                Jwts.builder()
                        .subject(email)
                        .claim("role", role)
                        .issuedAt(new Date())
                        .expiration(
                                new Date(
                                        System.currentTimeMillis()
                                                + 1000L
                                                * 60
                                                * 60
                                                * 24
                                )
                        );

        if (name != null && !name.isBlank()) {
            builder.claim("name", name);
        }

        return builder
                .signWith(secretKey)
                .compact();
    }

    public boolean isTokenValid(
            String token
    ) {

        try {

            getClaims(token);

            return true;

        } catch (Exception exception) {

            return false;
        }
    }

    public Claims getClaims(
            String token
    ) {

        return Jwts.parser()
                .verifyWith(secretKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }
}