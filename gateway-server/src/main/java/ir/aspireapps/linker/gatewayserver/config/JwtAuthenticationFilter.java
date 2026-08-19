package ir.aspireapps.linker.gatewayserver.config;

import io.jsonwebtoken.Claims;
import ir.aspireapps.linker.common.dto.UserRefreshRequest;
import ir.aspireapps.linker.gatewayserver.service.JwtService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.HttpCookie;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.net.URI;
import java.time.Instant;
import java.util.Date;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter implements GlobalFilter, Ordered {
    private static final List<String> PUBLIC_PATHS = List.of(
            "/ir/aspireapps/linker/auth/api/v1/register",
            "/ir/aspireapps/linker/auth/api/v1/login",
            "/ir/aspireapps/linker/auth/api/v1/refresh",
            "/ir/aspireapps/linker/gateway/api/v1/anything",
            "/actuator",
            "/ir/aspireapps/linker/auth/web/v1/register",
            "/ir/aspireapps/linker/auth/web/v1/login",
            "/ir/aspireapps/linker/auth/web/v1/refresh",
            "/ir/aspireapps/linker/gateway/web/v1/anything"
    );
    private static final List<String> ADMIN_PATHS = List.of(
            "/ir/aspireapps/linker/api/v1/admin/**"
    );
    private final JwtService jwtService;
    private final WebClient.Builder webClientBuilder;

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        String path = exchange.getRequest().getURI().getPath();
        boolean webConnection = path.contains("/web/");

        if (isPublic(path)) {
            log.info("Public path called at: {}", path);
            return chain.filter(exchange);
        }
        log.info("Authenticated path called at: {}", path);

        String token;
        String refreshToken = null;
        if (webConnection) {

            HttpCookie cookie = exchange.getRequest().getCookies().getFirst("ACCESS_TOKEN");
            if (cookie == null)
                return redirectToLogin(exchange);
            token = cookie.getValue();

            HttpCookie refreshCookie = exchange.getRequest().getCookies().getFirst("REFRESH_TOKEN");
            if (refreshCookie != null)
                refreshToken = refreshCookie.getValue();

        } else {
            String authHeader = exchange.getRequest().getHeaders().getFirst(HttpHeaders.AUTHORIZATION);
            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                return unauthorized(exchange);
            }
            token = authHeader.substring("Bearer ".length());
        }

        Claims claims = jwtService.validateToken(token);
        String username = claims.getSubject();
        String userId = claims.get("userId").toString();
        List<?> roles = claims.get("roles", List.class);
        String userSubscriptionStatus = claims.get("status").toString();
        List<String> rolesNames = roles.stream().map(Object::toString).toList();

        if (webConnection) {
            if (claims.getExpiration().before(Date.from(Instant.now()))) {
                if (refreshToken == null) {
                    return redirectToLogin(exchange);
                } else {
                    return redirectToRefresh(exchange, refreshToken);
                }
            }
        } else {
            if (username == null) {
                return unauthorized(exchange);
            }
            if (isAdmin(path) && !rolesNames.contains("ROLE_ADMIN")) {
                return forbidden(exchange);
            }
        }

        ServerHttpRequest request = exchange
                .getRequest()
                .mutate()
                .header("X-USER-ID", userId)
                .header("X-USERNAME", username)
                .header("X-USER-ROLES", String.join(",", rolesNames))
                .header("X-USER-STATUS", userSubscriptionStatus)
                .build();
        return chain.filter(exchange.mutate().request(request).build());
    }

    private Mono<Void> redirectToRefresh(ServerWebExchange exchange,
                                         String refreshToken) {
        UserRefreshRequest request = UserRefreshRequest.builder()
                .refreshToken(refreshToken)
                .returnUrl(exchange.getRequest().getPath().value())
                .build();
        return webClientBuilder
                .build()
                .post()
                .uri("http://user-service/ir/aspireapps/linker/auth/v1/refresh")
                .bodyValue(request)
                .exchangeToMono(response -> {
                    ServerHttpResponse gatewayResponse =
                            exchange.getResponse();
                    List<String> cookies = response.headers()
                            .header(HttpHeaders.SET_COOKIE);
                    gatewayResponse.getHeaders()
                            .put(HttpHeaders.SET_COOKIE, cookies);
                    gatewayResponse.setStatusCode(HttpStatus.SEE_OTHER);
                    gatewayResponse.getHeaders().setLocation(URI.create(request.returnUrl()));
                    return gatewayResponse.setComplete();
                });
    }

    private Mono<Void> redirectToLogin(ServerWebExchange exchange) {
        ServerHttpResponse response = exchange.getResponse();
        response.setStatusCode(HttpStatus.SEE_OTHER);
        response.getHeaders().setLocation(
                URI.create("/ir/aspireapps/linker/auth/web/v1/login?returnUrl=" + exchange.getRequest().getPath())
        );
        return response.setComplete();
    }

    @Override
    public int getOrder() {
        return -1;
    }

    private boolean isPublic(String path) {
        return PUBLIC_PATHS.stream().anyMatch(path::startsWith);
    }

    private boolean isAdmin(String path) {
        return ADMIN_PATHS.stream().anyMatch(path::startsWith);
    }

    private Mono<Void> forbidden(ServerWebExchange exchange) {
        exchange.getResponse().setStatusCode(HttpStatus.FORBIDDEN);
        return exchange.getResponse().setComplete();
    }

    private Mono<Void> unauthorized(ServerWebExchange exchange) {
        exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
        return exchange.getResponse().setComplete();
    }
}
