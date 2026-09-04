package ir.aspireapps.linker.gatewayserver.config;

import ir.aspireapps.linker.common.dto.UserRefreshRequest;
import ir.aspireapps.linker.common.error.InvalidJwtToken;
import ir.aspireapps.linker.common.utility.LoggingConstants;
import ir.aspireapps.linker.common.utility.LoggingContext;
import ir.aspireapps.linker.common.utility.LoggingEvents;
import ir.aspireapps.linker.gatewayserver.service.JwtService;
import ir.aspireapps.linker.gatewayserver.utility.ClaimsData;
import ir.aspireapps.linker.gatewayserver.utility.ClaimsDataManager;
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
import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter implements GlobalFilter, Ordered {
    private static final List<String> PUBLIC_PATHS = List.of(
            "/ir/aspireapps/linker/visit",
            "/ir/aspireapps/linker/auth/api/v1/register",
            "/ir/aspireapps/linker/auth/api/v1/login",
            "/ir/aspireapps/linker/auth/api/v1/refresh",
            "/ir/aspireapps/linker/links/api/v1/visit/",
            "/actuator",
            "/ir/aspireapps/linker/auth/web/v1/register",
            "/ir/aspireapps/linker/auth/web/v1/login",
            "/ir/aspireapps/linker/auth/web/v1/refresh"
    );
    private static final List<String> WEB_PATHS = List.of(
            "/ir/aspireapps/linker/auth/web/v1/register",
            "/ir/aspireapps/linker/auth/web/v1/login",
            "/ir/aspireapps/linker/auth/web/v1/refresh",
            "/ir/aspireapps/linker/user/web/v1/profile"
    );


    private final JwtService jwtService;
    private final WebClient.Builder webClientBuilder;
    private final ClaimsDataManager claimsDataManager;

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        log.info("\n\n========================= NEW REQUEST AT JwtAuthenticationFilter RECEIVED =========================");

        String requestId = exchange.getRequest().getHeaders().getFirst(LoggingConstants.REQUEST_ID_HEADER);
        if (requestId == null || requestId.isEmpty()) requestId = UUID.randomUUID().toString();
        LoggingContext.putRequestId(requestId);
        exchange.getResponse().getHeaders().set(LoggingConstants.REQUEST_ID_HEADER, requestId);
        exchange.getRequest().getHeaders().set(LoggingConstants.REQUEST_ID_HEADER, requestId);

        String path = exchange.getRequest().getURI().getPath();
        if (isWebPath(path)) {
            log.info("{} - START WEB path: [{}]", LoggingEvents.REQUEST_STARTED, path);
            return processWebFilter(exchange, chain);
        }

        if (isPublic(path)) {
            log.info("{} - START API PUBLIC path: [{}]", LoggingEvents.REQUEST_STARTED, path);
            return chain.filter(exchange);
        }


        String authHeader = exchange.getRequest().getHeaders().getFirst(HttpHeaders.AUTHORIZATION);
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            log.warn("{} - Authorized path [{}] \n REQUESTED with [null or not a Bearer] authHeader info", LoggingEvents.REQUEST_FAILED, path);
            log.info("\n========== REQUEST FINISHED(1) ==========");
            LoggingContext.clear();
            return unauthorized(exchange);
        }

        log.info("{} - START AUTHENTICATED PUBLIC path: [{}]", LoggingEvents.REQUEST_STARTED, path);
        String accessToken = authHeader.substring("Bearer ".length());
        try {
            ClaimsData claimsData = claimsDataManager.Extractor(accessToken);
            ServerHttpRequest request = claimsDataManager.serverRequestBuilder(exchange, claimsData);
            return chain.filter(exchange.mutate().request(request).build());
        } catch (InvalidJwtToken e) {
            log.warn("{} - Invalid JWT Token, reason: [{}]", LoggingEvents.REQUEST_FAILED, e.getMessage());
            log.info("\n========== REQUEST FINISHED(2) ==========");
            LoggingContext.clear();
            return unauthorized(exchange);
        }
    }

    private Mono<Void> processWebFilter(ServerWebExchange exchange, GatewayFilterChain chain) {
        String path = exchange.getRequest().getURI().getPath();

        if (isPublic(path)) {
            return chain.filter(exchange).doFinally(signalType -> {
                log.info("\n========== REQUEST FINISHED(3) ==========");
            });
        }

        String accessToken;
        HttpCookie accessCookie = exchange.getRequest().getCookies().getFirst("ACCESS_TOKEN");
        accessToken = accessCookie != null ? accessCookie.getValue() : null;

        String refreshToken;
        HttpCookie refreshCookie = exchange.getRequest().getCookies().getFirst("REFRESH_TOKEN");
        refreshToken = refreshCookie != null ? refreshCookie.getValue() : null;

        if (refreshToken == null || refreshToken.isEmpty()) {
            log.info("{} - Authorized web path [{}] \n REQUESTED with no REFRESH_TOKEN cookie, so Login required", LoggingEvents.REQUEST_FAILED, path);
            return redirectToLogin(exchange).doFinally(signalType -> {
                log.info("\n========== REQUEST FINISHED(4) ==========");
            });
        }

        if (accessToken == null || accessToken.isEmpty()) {
            log.info("{} - Authorized web path [{}] \n REQUESTED with no ACCESS_TOKEN cookie, so Refreshing required", LoggingEvents.REQUEST_FAILED, path);
            return redirectToRefresh(exchange, refreshToken).doFinally(signalType -> {
                log.info("\n========== REQUEST FINISHED(5) ==========");
            });
        }

        try {
            ClaimsData claimsData = claimsDataManager.Extractor(accessToken);
            ServerHttpRequest request = claimsDataManager.serverRequestBuilder(exchange, claimsData);
            return chain.filter(exchange.mutate().request(request).build());
        } catch (InvalidJwtToken e) {
            log.info("{} - Authorized web path [{}] \n REQUESTED with an Invalid JWT token, Start to use RefreshToken", LoggingEvents.REQUEST_FAILED, path);
            return redirectToRefresh(exchange, refreshToken).doFinally(signalType -> {
                log.info("\n========== REQUEST FINISHED(6) ==========");
            });
        }
    }

    private Mono<Void> redirectToRefresh(ServerWebExchange exchange, String refreshToken) {
        UserRefreshRequest refreshRequest = UserRefreshRequest.builder()
                .refreshToken(refreshToken)
                .returnUrl(exchange.getRequest().getURI().getRawPath())
                .build();
        String redirectPath = "/ir/aspireapps/linker/auth/web/v1/refresh";
        return webClientBuilder
                .build()
                .post()
                .uri(URI.create(redirectPath))
                .bodyValue(refreshRequest)
                .exchangeToMono(response -> {
                    if (response.statusCode() == HttpStatus.FORBIDDEN)
                        return redirectToLogin(exchange);

                    ServerHttpResponse gatewayResponse = exchange.getResponse();
                    List<String> cookies = response.headers().header(HttpHeaders.SET_COOKIE);
                    gatewayResponse.getHeaders().put(HttpHeaders.SET_COOKIE, cookies);
                    gatewayResponse.setStatusCode(HttpStatus.SEE_OTHER);
                    gatewayResponse.getHeaders().setLocation(URI.create(refreshRequest.returnUrl()));
                    return gatewayResponse.setComplete();
                });
    }

    private Mono<Void> redirectToLogin(ServerWebExchange exchange) { //}, boolean redirectToProfile) {
        ServerHttpResponse response = exchange.getResponse();
        response.setStatusCode(HttpStatus.SEE_OTHER);
        response.getHeaders().setLocation(
                // TODO: for implementation redirect to other pages that contains @RequestBody data I should use something like Redis to save it's data and send it's data into a path variable, then after redirecting I must load data from Redis and do a complete redirect with data
                URI.create("/ir/aspireapps/linker/auth/web/v1/login")
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

    private boolean isWebPath(String path) {
        return WEB_PATHS.stream().anyMatch(path::startsWith);
    }

    private Mono<Void> unauthorized(ServerWebExchange exchange) {
        exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
        return exchange.getResponse().setComplete();
    }
}
