package com.n11bootcamp.jwtornek.auth;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import java.security.Key;
import java.util.Date;
import org.springframework.stereotype.Service;

@Service
public class TokenManager {

    private static final long ACCESS_TOKEN_VALIDITY = 5 * 60 * 1000;
    private static final long REFRESH_TOKEN_VALIDITY = 24 * 60 * 60 * 1000;

    private final Key key = Keys.secretKeyFor(SignatureAlgorithm.HS256);

    public String generateAccessToken(String username) {
        return generateToken(username, ACCESS_TOKEN_VALIDITY, "access");
    }

    public String generateRefreshToken(String username) {
        return generateToken(username, REFRESH_TOKEN_VALIDITY, "refresh");
    }

    private String generateToken(String username, long validity, String tokenType) {
        return Jwts.builder()
                .setSubject(username)
                .setIssuer("www.opendart.com")
                .claim("type", tokenType)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + validity))
                .signWith(key)
                .compact();
    }

    public boolean tokenValidate(String token) {
        return getUsernameToken(token) != null && !isExpired(token);
    }

    public boolean isRefreshToken(String token) {
        Claims claims = getClaims(token);
        return "refresh".equals(claims.get("type"));
    }

    public String getUsernameToken(String token) {
        return getClaims(token).getSubject();
    }

    private boolean isExpired(String token) {
        return getClaims(token).getExpiration().before(new Date());
    }

    private Claims getClaims(String token) {
        return Jwts.parser()
                .setSigningKey(key)
                .parseClaimsJws(token)
                .getBody();
    }
}
