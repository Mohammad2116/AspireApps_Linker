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
        log.info(("=================================================================="));
        log.info("New request comes throw Gateway filter ");
        log.info("exchange: {}", exchange.toString());

        String path = exchange.getRequest().getURI().getPath();
        log.info("exchange path: {}", path);
        boolean webConnection = path.contains("/web/");
        log.info("This is a /web/ request so we will handle it using web request patterns");

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
        log.info("access token: {}", token);
        log.info("refresh token: {}", refreshToken);

        Claims claims = null;
        try {
            claims = jwtService.validateToken(token);
        } catch (ExpiredJwtException e) {
            if (webConnection) {
                if (refreshToken == null) {
                    log.info("refresh token is null so, redirecting to login");
                    return redirectToLogin(exchange);
                } else {
                    log.info("refresh token IS NOT null so, redirecting to refresh");
                    return redirectToRefresh(exchange, refreshToken);
                }
            } else {
                // TODO: throw an token expired exception
            }
        }
        String username = claims.getSubject();
        String userId = claims.get("userId").toString();
        List<?> roles = claims.get("roles", List.class);
        String userSubscriptionStatus = claims.get("status").toString();
        List<String> rolesNames = roles.stream().map(Object::toString).toList();

        log.info("Is refresh token expired: {}", claims.getExpiration().before(Date.from(Instant.now())));
        log.info("refresh token expiration: {}", claims.getExpiration());
        if (!webConnection) {
            if (username == null) {
                log.info("username is null so redirecting to unauthorized");
                return unauthorized(exchange);
            }
            if (isAdmin(path) && !rolesNames.contains("ROLE_ADMIN")) {
                log.info("user is not ADMIN so redirect to forbidden");
                return forbidden(exchange);
            }
        } else {
            // TODO: handles an inconsistency in token information and request
        }

        ServerHttpRequest request = exchange
                .getRequest()
                .mutate()
                .header("X-USER-ID", userId)
                .header("X-USERNAME", username)
                .header("X-USER-ROLES", String.join(",", rolesNames))
                .header("X-USER-STATUS", userSubscriptionStatus)
                .build();
        log.info("Create a request and send to it's target service");
        log.info("request: {}", request);
        Mono<Void> result = chain.filter(exchange.mutate().request(request).build());
        log.info("Mono<Void> result of chain.filer: {}", result.toString());
        return result;
    }

    private Mono<Void> redirectToRefresh(ServerWebExchange exchange,
                                         String refreshToken) {
        UserRefreshRequest request = UserRefreshRequest.builder()
                .refreshToken(refreshToken)
                .returnUrl(exchange.getRequest().getURI().getRawPath())
                .build();
        log.info("request created at redirectToRefresh: {}", request.toString());
        String redirectPath = "http://user-service/ir/aspireapps/linker/auth/web/v1/refresh";
        log.info("Create a request to: {}", redirectPath);
        return webClientBuilder
                .build()
                .post()
                .uri(redirectPath)
                .bodyValue(request)
                .exchangeToMono(response -> {
                    log.info("Refresh response status: {}", response.statusCode());
                    log.info("Refresh Set-Cookie: {}",
                            response.headers().header(HttpHeaders.SET_COOKIE));
                    log.info("Refresh Location: {}",
                            response.headers().header(HttpHeaders.LOCATION));

                    log.info("exchange cookies are:");
                    log.info(exchange.getResponse().getCookies().toString());

                    ServerHttpResponse gatewayResponse =
                            exchange.getResponse();
                    List<String> cookies = response.headers()
                            .header(HttpHeaders.SET_COOKIE);
                    log.info("Set Cookies are: {}", cookies.stream().toList());
                    gatewayResponse.getHeaders()
                            .put(HttpHeaders.SET_COOKIE, cookies);
                    gatewayResponse.setStatusCode(HttpStatus.SEE_OTHER);
                    gatewayResponse.getHeaders().setLocation(URI.create(request.returnUrl()));
                    log.info("return URL is: {}", URI.create(request.returnUrl()));
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
