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
//    }
//        log.info(("=================================================================="));
//        log.info("New request comes throw Gateway filter ");
//        log.info("exchange: {}", exchange.toString());
//        String path = exchange.getRequest().getURI().getPath();
//        if (isPublic(path)) {
//            log.info("Public path called at: {}", path);
//            return chain.filter(exchange);
//        }
//        log.info("Authenticated path called at: {}", path);
//        log.info("exchange path: {}", path);
//        boolean webConnection = path.contains("/web/");
//        log.info("This is a /web/ request so we will handle it using web request patterns");
//        String accessToken;
//        String refreshToken = null;
//        if (webConnection) {
//            HttpCookie cookie = exchange.getRequest().getCookies().getFirst("ACCESS_TOKEN");
//            if (cookie == null) {
//                log.info("ACCESS_TOKEN cookie is null goto login page");
//                return redirectToLogin(exchange);
//            }
//            accessToken = cookie.getValue();
//
//            HttpCookie refreshCookie = exchange.getRequest().getCookies().getFirst("REFRESH_TOKEN");
//            if (refreshCookie == null) {
//                log.info("REFRESH_TOKEN cookie is null goto login page");
//                return redirectToLogin(exchange);
//            }
//            refreshToken = refreshCookie.getValue();
//            if(refreshToken.isEmpty()) {
//                log.info("REFRESH_TOKEN is null goto login page");
//                return redirectToLogin(exchange);
//            }
//        } else {
//            String authHeader = exchange.getRequest().getHeaders().getFirst(HttpHeaders.AUTHORIZATION);
//            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
//                return unauthorized(exchange);
//            }
//            accessToken = authHeader.substring("Bearer ".length());
//        }
//        log.info("access token: {}", accessToken);
//        log.info("refresh token: {}", refreshToken);
//        if(webConnection){
//            return processWebFilter(exchange, accessToken, refreshToken, path, chain);
//        }
//
//        Claims claims = null;
//        try {
//            claims = jwtService.validateToken(accessToken);
//        } catch (ExpiredJwtException e) {
//            if (webConnection) {
//                    return redirectToRefresh(exchange, refreshToken);
//            } //else {
//                // TODO: throw an token expired exception
//            //}
//        }
//        if(claims == null) return redirectToLogin(exchange);
//
//        String username = claims.getSubject();
//        String userId = claims.get("userId").toString();
//        List<?> roles = claims.get("roles", List.class);
//        String userSubscriptionStatus = claims.get("status").toString();
//        List<String> rolesNames = roles.stream().map(Object::toString).toList();
//
//        log.info("Is refresh token expired: {}", claims.getExpiration().before(Date.from(Instant.now())));
//        log.info("refresh token expiration: {}", claims.getExpiration());
//        if (!webConnection) {
//            if (username == null) {
//                log.info("username is null so redirecting to unauthorized");
//                return unauthorized(exchange);
//            }
//            if (isAdmin(path) && !rolesNames.contains("ROLE_ADMIN")) {
//                log.info("user is not ADMIN so redirect to forbidden");
//                return forbidden(exchange);
//            }
//        } else {
//            // TODO: handles an inconsistency in token information and request
//        }
//
//        ServerHttpRequest request = exchange
//                .getRequest()
//                .mutate()
//                .header("X-USER-ID", userId)
//                .header("X-USERNAME", username)
//                .header("X-USER-ROLES", String.join(",", rolesNames))
//                .header("X-USER-STATUS", userSubscriptionStatus)
//                .build();
//        log.info("Create a request and send to it's target service");
//        log.info("request: {}", request);
//        Mono<Void> result = chain.filter(exchange.mutate().request(request).build());
//        log.info("Mono<Void> result of chain.filer: {}", result.toString());
//        return result;
//    }
//
//    private Mono<Void> processWebFilter(ServerWebExchange exchange, String accessToken, String refreshToken, String path, GatewayFilterChain chain) {
//        Claims claims = null;
//        try {
//            claims = jwtService.validateToken(accessToken);
//        } catch (ExpiredJwtException e) {
//                return redirectToRefresh(exchange, refreshToken);
//        }
//        if(claims == null) return redirectToRefresh(exchange, refreshToken);
//
//        String username = claims.getSubject();
//        String userId = claims.get("userId").toString();
//        List<?> roles = claims.get("roles", List.class);
//        String userSubscriptionStatus = claims.get("status").toString();
//        List<String> rolesNames = roles.stream().map(Object::toString).toList();
//        log.info("Is web refresh token expired: {}", claims.getExpiration().before(Date.from(Instant.now())));
//        log.info("web refresh token expiration: {}", claims.getExpiration());
//        if (username == null) {
//            log.info("username is null so redirecting to web login");
//            return redirectToRefresh(exchange, refreshToken);
//        }
//        if (isAdmin(path) && !rolesNames.contains("ROLE_ADMIN")) {
//            log.info("web user is not ADMIN so redirect to forbidden");
//            return forbidden(exchange); // redirect to unauthorized web page
//        }
//
//        ServerHttpRequest request = exchange
//                .getRequest()
//                .mutate()
//                .header("X-USER-ID", userId)
//                .header("X-USERNAME", username)
//                .header("X-USER-ROLES", String.join(",", rolesNames))
//                .header("X-USER-STATUS", userSubscriptionStatus)
//                .build();
//        log.info("Create a web request and send to it's target service");
//        log.info("web request: {}", request);
//        Mono<Void> result = chain.filter(exchange.mutate().request(request).build());
//        log.info("Mono<Void> web result of chain.filer: {}", result.toString());
//        return result;
//    }
//
//    private Mono<Void> redirectToRefresh(ServerWebExchange exchange,
//                                         String refreshToken) {
//        UserRefreshRequest request = UserRefreshRequest.builder()
//                .refreshToken(refreshToken)
//                .returnUrl(exchange.getRequest().getURI().getRawPath())
//                .build();
//        log.info("request created at redirectToRefresh: {}", request.toString());
//        String redirectPath = "http://user-service/ir/aspireapps/linker/auth/web/v1/refresh";
//        log.info("Create a request to: {}", redirectPath);
//        return webClientBuilder
//                .build()
//                .post()
//                .uri(redirectPath)
//                .bodyValue(request)
//                .exchangeToMono(response -> {
//                    log.info("repose status code: {}", response.statusCode());
//
//                    log.info("Refresh response status: {}", response.statusCode());
//                    log.info("Refresh Set-Cookie: {}",
//                            response.headers().header(HttpHeaders.SET_COOKIE));
//                    log.info("Refresh Location: {}",
//                            response.headers().header(HttpHeaders.LOCATION));
//
//                    log.info("exchange cookies are:");
//                    log.info(exchange.getResponse().getCookies().toString());
//
//                    ServerHttpResponse gatewayResponse =
//                            exchange.getResponse();
//                    List<String> cookies = response.headers()
//                            .header(HttpHeaders.SET_COOKIE);
//                    log.info("Set Cookies are: {}", cookies.stream().toList());
//                    gatewayResponse.getHeaders()
//                            .put(HttpHeaders.SET_COOKIE, cookies);
//                    gatewayResponse.setStatusCode(HttpStatus.SEE_OTHER);
//                    gatewayResponse.getHeaders().setLocation(URI.create(request.returnUrl()));
//                    log.info("return URL is: {}", URI.create(request.returnUrl()));
//                    return gatewayResponse.setComplete();
//                });
//    }
//
//    private Mono<Void> redirectToLogin(ServerWebExchange exchange) {
//        ServerHttpResponse response = exchange.getResponse();
//        response.setStatusCode(HttpStatus.SEE_OTHER);
//        response.getHeaders().setLocation(
//                URI.create("/ir/aspireapps/linker/auth/web/v1/login")
//        );
//        return response.setComplete();
//    }
//

