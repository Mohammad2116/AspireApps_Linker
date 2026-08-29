package ir.aspireapps.linker.gatewayserver.config;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import ir.aspireapps.linker.gatewayserver.service.JwtService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpCookie;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;

import java.util.List;

@Component
@RequiredArgsConstructor
public class WebAuthenticatorFilter implements WebFilter {
    private final static List<String> LOCAL_PATHS = List.of(
            "/ir/aspireapps/linker/home");
    private final JwtService jwtService;

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
        String path = exchange.getRequest().getURI().getPath();
        if (!isLocal(path)) return chain.filter(exchange);
        exchange.getAttributes().put("AUTHENTICATED", false);

        HttpCookie cookie = exchange.getRequest().getCookies().getFirst("ACCESS_TOKEN");
        if (cookie != null) {
            Claims claims;
            try {
                claims = jwtService.validateToken(cookie.getValue());
                exchange.getAttributes().put("AUTHENTICATED", true);
            } catch (ExpiredJwtException e) {
                return chain.filter(exchange);
            }
        }
        return chain.filter(exchange);
    }

    private boolean isLocal(String path) {
        return LOCAL_PATHS.stream().anyMatch(path::equalsIgnoreCase);
    }
}
