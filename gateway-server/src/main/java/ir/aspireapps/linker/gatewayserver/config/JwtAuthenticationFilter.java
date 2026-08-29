package ir.aspireapps.linker.gatewayserver.config;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
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
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter implements GlobalFilter, Ordered {
    private static final List<String> PUBLIC_PATHS = List.of(
            "/ir/aspireapps/visit",
            "/ir/aspireapps/linker/auth/api/v1/register",
            "/ir/aspireapps/linker/auth/api/v1/login",
            "/ir/aspireapps/linker/auth/api/v1/refresh",
            "/ir/aspireapps/linker/gateway/api/v1/anything",
            "/ir/aspireapps/linker/links/api/v1/visit/",
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
        log.info(("=================================================================="));
        if (exchange.getRequest().getURI().getPath().contains("/web/")) {
            return processWebFilter(exchange, chain);
        }

        String path = exchange.getRequest().getURI().getPath();
        if (isPublic(path)) return chain.filter(exchange);

        String authHeader = exchange.getRequest().getHeaders().getFirst(HttpHeaders.AUTHORIZATION);
        if (authHeader == null || !authHeader.startsWith("Bearer "))
            return unauthorized(exchange);
        String accessToken = authHeader.substring("Bearer ".length());
        Claims claims = null;
        claims = jwtService.validateToken(accessToken);
        String username = claims.getSubject();
        String userId = claims.get("userId").toString();
        String status = claims.get("Status").toString();
        List<?> roles = claims.get("roles", List.class);
        List<String> rolesNames = roles.stream().map(Object::toString).toList();
        ServerHttpRequest request = exchange
                .getRequest()
                .mutate()
                .header("X-USERNAME", username)
                .header("X-USER-ID", userId)
                .header("X-USER-STATE", status)
                .header("X-USER-ROLES", String.join(",", rolesNames))
                .build();

        return chain.filter(exchange.mutate().request(request).build());
    }

    private Mono<Void> processWebFilter(ServerWebExchange exchange, GatewayFilterChain chain) {
        String path = exchange.getRequest().getURI().getPath();
        log.info("This is a web request, so web filter will process it");
        log.info("exchange request path is {}", path);

        if (isPublic(path)) {
            log.info("This is a public path, so filter chain release here");
            return chain.filter(exchange);
        }

        String accessToken = null;
        HttpCookie accessCookie = exchange.getRequest().getCookies().getFirst("ACCESS_TOKEN");
        accessToken = accessCookie != null ? accessCookie.getValue() : null;

        String refreshToken = null;
        HttpCookie refreshCookie = exchange.getRequest().getCookies().getFirst("REFRESH_TOKEN");
        refreshToken = refreshCookie != null ? refreshCookie.getValue() : null;

        log.info("ACCESS_TOKEN: {}", accessToken);
        log.info("REFRESH_TOKEN: {}", refreshToken);
        if (refreshToken == null || refreshToken.isEmpty()) return redirectToLogin(exchange, false);
        if (accessToken == null || accessToken.isEmpty()) return redirectToRefresh(exchange, refreshToken);

        Claims claims = null;
        try {
            claims = jwtService.validateToken(accessToken);
        } catch (ExpiredJwtException e) {
            return redirectToRefresh(exchange, refreshToken);
        }
        List<?> roles = claims.get("roles", List.class);
        List<String> rolesNames = roles.stream().map(Object::toString).toList();
        ServerHttpRequest request = exchange
                .getRequest()
                .mutate()
                .header("X-USERNAME", claims.getSubject())
                .header("X-USER-ID", claims.get("userId").toString())
                .header("X-USER-STATUS", claims.get("status").toString())
                .header("X-USER-ROLES", String.join(",", rolesNames))
                .build();
        return chain.filter(exchange.mutate().request(request).build());
    }

    private Mono<Void> redirectToRefresh(ServerWebExchange exchange, String refreshToken) {
        UserRefreshRequest refreshRequest = UserRefreshRequest.builder()
                .refreshToken(refreshToken)
                .returnUrl(exchange.getRequest().getURI().getRawPath())
                .build();
        String redirectPath = "http://user-service/ir/aspireapps/linker/auth/web/v1/refresh";
        return webClientBuilder
                .build()
                .post()
                .uri(redirectPath)
                .bodyValue(refreshRequest)
                .exchangeToMono(response -> {
                    if (response.statusCode() == HttpStatus.FORBIDDEN)
                        return redirectToLogin(exchange, true);

                    ServerHttpResponse gatewayResponse = exchange.getResponse();
                    List<String> cookies = response.headers().header(HttpHeaders.SET_COOKIE);
                    gatewayResponse.getHeaders().put(HttpHeaders.SET_COOKIE, cookies);
                    gatewayResponse.setStatusCode(HttpStatus.SEE_OTHER);
                    gatewayResponse.getHeaders().setLocation(URI.create(refreshRequest.returnUrl()));
                    return gatewayResponse.setComplete();
                });
    }

    private Mono<Void> redirectToLogin(ServerWebExchange exchange, boolean redirectToProfile) {
        ServerHttpResponse response = exchange.getResponse();
        response.setStatusCode(HttpStatus.SEE_OTHER);
        response.getHeaders().setLocation(
// TODO: for implementation redirect to other pages that contains @RequestBody data I should use something like Redis to save it's data and send it's data into a path variable, then after redirecting I must load data from Redis and do a complete redirect with data
//
//                redirectToProfile
//                    ?
                URI.create("/ir/aspireapps/linker/auth/web/v1/login")
//                    :
//                        URI.create("/ir/aspireapps/linker/auth/web/v1/login?returnUrl=" + exchange.getRequest().getURI().getPath())
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
