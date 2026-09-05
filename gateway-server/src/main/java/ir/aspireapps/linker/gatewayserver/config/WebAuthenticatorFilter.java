package ir.aspireapps.linker.gatewayserver.config;

import io.jsonwebtoken.ExpiredJwtException;
import ir.aspireapps.linker.common.error.InvalidJwtToken;
import ir.aspireapps.linker.common.utility.HeaderConstants;
import ir.aspireapps.linker.common.utility.LoggingConstants;
import ir.aspireapps.linker.common.utility.LoggingContext;
import ir.aspireapps.linker.gatewayserver.service.JwtService;
import ir.aspireapps.linker.gatewayserver.utility.ClaimsDataManager;
import jakarta.annotation.Nonnull;
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
    private final static List<String> RESOURCE_PATHS = List.of(
            "/ir/aspireapps/linker/css/"
    );
    private final static List<String> GATEWAY_WEB_CONTROLLERS_PATH = List.of(
            "/ir/aspireapps/linker/home");
    private final JwtService jwtService;
    private final ClaimsDataManager  claimsDataManager;

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, @Nonnull WebFilterChain chain) {
        String requestId = exchange.getRequest().getHeaders().getFirst(LoggingConstants.REQUEST_ID_HEADER);
        if (requestId == null || requestId.isEmpty()) requestId = UUID.randomUUID().toString();
        LoggingContext.putRequestId(requestId);
        exchange.getResponse().getHeaders().add(LoggingConstants.REQUEST_ID_HEADER, requestId);
        exchange.getRequest().getHeaders().add(LoggingConstants.REQUEST_ID_HEADER, requestId);

        String path = exchange.getRequest().getURI().getPath();

        if (isResource(path)) {
            log.info("This is a request for Resources, return resource");
            return chain.filter(exchange);
        }

        // if path is a controller at gateway, it's a public page no need any check up, just send it to controller
        if (!isGatewayWebControllersPath(path)) return chain.filter(exchange);

        // by default request is not authenticated, until authentication passes successfully
        exchange.getAttributes().put("AUTHENTICATED", false);

        HttpCookie cookie = exchange.getRequest().getCookies().getFirst("ACCESS_TOKEN");
        if (cookie != null && !cookie.getValue().isEmpty()) {
            log.info("Access Token has been loaded {}", cookie.getValue());
            try {
                claimsDataManager.serverRequestBuilder(
                        exchange,
                        claimsDataManager.extract(cookie.getValue())
                );
                log.info("Web Auth Header X-USERNAME: {}", exchange.getRequest().getHeaders().get(HeaderConstants.X_USERNAME));
                log.info("Web Auth Header X-USER_ID: {}", exchange.getRequest().getHeaders().get(HeaderConstants.X_USER_ID));
                log.info("Web Auth Header X-USER_STATE: {}", exchange.getRequest().getHeaders().get(HeaderConstants.X_USER_STATUS));
                log.info("Web Auth Header X-USER-ROLES: {}", exchange.getRequest().getHeaders().get(HeaderConstants.X_USER_ROLES));
                exchange.getAttributes().put("AUTHENTICATED", true);
            } catch (ExpiredJwtException e) {
                log.warn("Expired JWT Token received");
                return chain.filter(exchange);
            } catch (InvalidJwtToken e) {
                log.warn("Invalid JWT Token received");
                return chain.filter(exchange);
            }
        }
        return chain.filter(exchange);
    }

    private boolean isGatewayWebControllersPath(String path) {
        return GATEWAY_WEB_CONTROLLERS_PATH.stream().anyMatch(path::equals);
    }

    private boolean isResource(String path) {
        return RESOURCE_PATHS.stream().anyMatch(path::startsWith);
    }
}
