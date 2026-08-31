package ir.aspireapps.linker.gatewayserver.config;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import ir.aspireapps.linker.common.utility.LoggingConstants;
import ir.aspireapps.linker.common.utility.LoggingContext;
import ir.aspireapps.linker.common.utility.LoggingEvents;
import ir.aspireapps.linker.gatewayserver.service.JwtService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpCookie;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class WebAuthenticatorFilter implements WebFilter {
    private final static List<String> LOCAL_PATHS = List.of(
            "/ir/aspireapps/linker/home",
            "/ir/aspireapps/linker/css/**");
    private final JwtService jwtService;

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
        String requestId = exchange.getRequest().getHeaders().getFirst(LoggingConstants.REQUEST_ID_HEADER);
        if (requestId == null || requestId.isEmpty()) requestId = UUID.randomUUID().toString();
        LoggingContext.putRequestId(requestId);
        exchange.getResponse().getHeaders().add(LoggingConstants.REQUEST_ID_HEADER, requestId);
        exchange.getRequest().getHeaders().add(LoggingConstants.REQUEST_ID_HEADER, requestId);

        String path = exchange.getRequest().getURI().getPath();
        log.info("{} - Starting web request to path {}", LoggingEvents.REQUEST_STARTED, path);
        if (!isLocal(path)) {
            log.info("Not a local request so sent request to controller");
            return chain.filter(exchange)
                    .doFinally(signal -> {
                        log.info("{} - End web request to path {}", LoggingEvents.REQUEST_COMPLETED, path);
                    });
        }

        exchange.getAttributes().put("AUTHENTICATED", false);

        HttpCookie cookie = exchange.getRequest().getCookies().getFirst("ACCESS_TOKEN");
        if (cookie != null) {
            Claims claims;
            try {
                claims = jwtService.validateToken(cookie.getValue());
                exchange.getAttributes().put("AUTHENTICATED", true);
            } catch (ExpiredJwtException e) {
                log.warn("Expired JWT Token received");
                return chain.filter(exchange)
                        .doFinally(signal -> {
                            log.info("{} End of request without Auth info", LoggingEvents.REQUEST_COMPLETED);
                            LoggingContext.clear();
                        });
            }
        }
        return chain.filter(exchange)
                .doFinally(signal -> {
                    log.info("{} End of request with Auth info", LoggingEvents.REQUEST_COMPLETED);
                    LoggingContext.clear();
                });
    }

    private boolean isLocal(String path) {
        return LOCAL_PATHS.stream().anyMatch(path::equalsIgnoreCase);
    }
}
