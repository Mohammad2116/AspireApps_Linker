package ir.aspireapps.linker.userservice.service;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import ir.aspireapps.linker.userservice.model.User;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;
import java.util.List;

@Component
public class JwtService {
    private final long expirationMs;
    private final SecretKey secretKey;

    public JwtService(
            @Value("${security.jwt.access-token-secret-key}") String secretKey,
            @Value("${security.jwt.access-token-expiration-ms}") long expirationMs) {
        this.expirationMs = expirationMs;
        this.secretKey = Keys.hmacShaKeyFor(secretKey.getBytes(StandardCharsets.UTF_8));
    }

    public String generateAccessToken(User user) {
        Instant now = Instant.now();
        return Jwts.builder()
                .subject(user.getUsername())
                .claim("userId", user.getId())
                .claim("roles", List.of(user.getRole().name()))
                .claim("status", user.getStatus().name())
                .issuedAt(Date.from(now))
                .expiration(Date.from(now.plusMillis(expirationMs)))
                .signWith(secretKey)
                .compact();
    }

    public long accessTokenExpirationSeconds() {
        return expirationMs / 1000;
    }
}
