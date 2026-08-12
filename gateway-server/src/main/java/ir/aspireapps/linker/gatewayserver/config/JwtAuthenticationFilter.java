package ir.aspireapps.linker.gatewayserver.config;

import io.jsonwebtoken.Claims;
import ir.aspireapps.linker.gatewayserver.service.JwtService;
import lombok.RequiredArgsConstructor;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.List;

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

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {

        String path = exchange.getRequest().getURI().getPath();
        if (isPublic(path)) {
            return chain.filter(exchange);
        }

        String authHeader = exchange.getRequest().getHeaders().getFirst(HttpHeaders.AUTHORIZATION);
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return unauthorized(exchange);
        }

        String token = authHeader.substring("Bearer ".length());
        Claims claims = jwtService.validateToken(token);
        String username = claims.getSubject();
        List<?> roles = claims.get("roles", List.class);
        if (username == null || roles == null) {
            return unauthorized(exchange);
        }
        List<String> rolesNames = roles.stream().map(Object::toString).toList();
        if (isAdmin(path) && !rolesNames.contains("ROLE_ADMIN")) {
            return forbidden(exchange);
        }

        ServerHttpRequest request = exchange
                .getRequest()
                .mutate()
                .header("X-USERNAME", username)
                .header("X-USER-ROLES", String.join(",", rolesNames))
                .build();
        return chain.filter(exchange.mutate().request(request).build());
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
