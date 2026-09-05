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

    public ClaimsData extract(String refreshToken) {

        if (refreshToken == null || refreshToken.isBlank()) {
            log.warn("Refresh token is null or blank");
            throw new InvalidJwtToken("Refresh token is null or blank");
        }

        Claims claims = jwtService.validateToken(refreshToken);

        String username = claims.getSubject();
        String userId = claims.get(ClaimConstants.USER_ID, String.class);
        String status = claims.get(ClaimConstants.STATUS, String.class);

        if (username == null || username.isBlank()
                || userId == null || userId.isBlank()
                || status == null || status.isBlank()) {
            log.warn("Required claims are missing from refresh token");
            log.info("username [{}], userId [{}], status [{}]", username, userId, status);
            throw new InvalidJwtToken("Required claims are missing");
        }

        List<?> rawRoles = claims.get(ClaimConstants.ROLES, List.class);

        if (rawRoles == null || rawRoles.isEmpty()) {
            log.warn("Roles claim is missing or empty");
            throw new InvalidJwtToken("Invalid roles list");
        }

        List<String> rolesNames = rawRoles.stream()
                .map(Object::toString)
                .toList();

        log.debug(
                "Claims extracted for username [{}], status [{}], roles [{}]",
                username,
                status,
                rolesNames
        );

        return ClaimsData.builder()
                .username(username)
                .userId(userId)
                .status(status)
                .rolesNames(rolesNames)
                .build();
    }

    public ServerHttpRequest serverRequestBuilder(
            ServerWebExchange exchange,
            ClaimsData claimsData) {

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