package ir.aspireapps.linker.linksservice.config;

import ir.aspireapps.linker.common.utility.HeaderConstants;
import ir.aspireapps.linker.common.utility.LoggingConstants;
import ir.aspireapps.linker.common.utility.LoggingContext;
import ir.aspireapps.linker.common.utility.LoggingEvents;
import jakarta.annotation.Nonnull;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Component
public class GatewayHeaderAuthFilter extends OncePerRequestFilter {
    @Override
    protected void doFilterInternal(@Nonnull HttpServletRequest request,
                                    @Nonnull HttpServletResponse response,
                                    @Nonnull FilterChain filterChain) throws ServletException, IOException {
        String requestId = request.getHeader(LoggingConstants.REQUEST_ID_HEADER);
        if (requestId == null || requestId.isEmpty()) requestId = UUID.randomUUID().toString();
        LoggingContext.putRequestId(requestId);
        response.setHeader(LoggingConstants.REQUEST_ID_HEADER, requestId);

        log.info("{} - Request received at links-service", LoggingEvents.REQUEST_STARTED);
        String username = request.getHeader(HeaderConstants.X_USERNAME);
        String roles = request.getHeader(HeaderConstants.X_USER_ROLES);
        String status = request.getHeader(HeaderConstants.X_USER_STATE);

        if ((username != null) && (!username.isEmpty())
                && (SecurityContextHolder.getContext().getAuthentication() == null)) {
            List<GrantedAuthority> authorities = Collections.emptyList();
            if (roles != null && !roles.isEmpty()) {
                authorities = Arrays.stream(roles.split(","))
                        .map(String::trim)
                        .map(r -> r.startsWith("ROLE_") ? r : "ROLE_" + r)
                        .map(SimpleGrantedAuthority::new)
                        .collect(Collectors.toList());
            }
            UsernamePasswordAuthenticationToken authentication =
                    new UsernamePasswordAuthenticationToken(username, null, authorities);
            authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
            SecurityContextHolder.getContext().setAuthentication(authentication);
            log.info("Request Authenticated with: username[{}], role[{}],  status[{}]", username, roles, status);
        } else {
            log.info("Request with no auth information received");
        }

        try {
            filterChain.doFilter(request, response);
        } finally {
            log.info("{} - Request completed at links-Service", LoggingEvents.REQUEST_COMPLETED);
        }
    }
}
