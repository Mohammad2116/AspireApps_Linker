package ir.aspireapps.linker.gatewayserver.utility;

import io.jsonwebtoken.Claims;
import ir.aspireapps.linker.common.error.InvalidJwtToken;
import ir.aspireapps.linker.common.utility.ClaimConstants;
import ir.aspireapps.linker.common.utility.HeaderConstants;
import ir.aspireapps.linker.gatewayserver.service.JwtService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;

import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class ClaimsDataManager {
    private final JwtService jwtService;

    public ClaimsData Extractor(String refreshToken) {
        if (refreshToken == null || refreshToken.isEmpty()) {
            log.warn("Refresh token is null or empty");
            throw new InvalidJwtToken("Refresh token is null or empty");
        }

        Claims claims;
        claims = jwtService.validateToken(refreshToken);
        if (claims == null) {
            log.warn("Invalid refresh token");
            throw new InvalidJwtToken("Invalid refresh token");
        }

        List<?> rawRoles = claims.get(ClaimConstants.ROLES, List.class);
        if (rawRoles == null || rawRoles.isEmpty()) {
            throw new InvalidJwtToken("Invalid roles list");
        }
        log.info("Username [{}], status[{}], ... extracted from refreshToken using ClaimsDataManager", claims.getSubject(), claims.get(ClaimConstants.STATUS));
        return ClaimsData.builder()
                .username(claims.getSubject())
                .userId(claims.get(ClaimConstants.USER_ID).toString())
                .status(claims.get(ClaimConstants.STATUS).toString())
                .rolesNames(rawRoles.stream().map(Object::toString).toList())
                .build();
    }

    public ServerHttpRequest serverRequestBuilder(ServerWebExchange exchange, ClaimsData claimsData) {
        return exchange
                .getRequest()
                .mutate()
                .header(HeaderConstants.X_USERNAME, claimsData.username())
                .header(HeaderConstants.X_USER_ID, claimsData.userId())
                .header(HeaderConstants.X_USER_STATUS, claimsData.status())
                .header(HeaderConstants.X_USER_ROLES, String.join(",", claimsData.rolesNames()))
                .build();
    }
}

