package ir.aspireapps.linker.gatewayserver.service;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import ir.aspireapps.linker.common.error.InvalidJwtToken;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;

@Slf4j
@Component
public class JwtService {
    private final SecretKey secretKey;

    public JwtService(@Value("${security.jwt.secretkey}") String secretKey) {
        this.secretKey = Keys.hmacShaKeyFor(secretKey.getBytes(StandardCharsets.UTF_8));
    }

    public Claims validateToken(String token) throws ExpiredJwtException {
        try {
            return Jwts.parser()
                    .verifyWith(this.secretKey)
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();
        } catch (ExpiredJwtException e) {
            log.info("JwtService cannot validate expired token");
            throw new InvalidJwtToken("Expired token");
        } catch (RuntimeException e) {
            log.info("JwtService cannot validate token");
            throw new InvalidJwtToken("JwtService cannot validate token");
        }
    }
}
