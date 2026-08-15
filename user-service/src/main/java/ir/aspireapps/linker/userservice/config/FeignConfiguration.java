package ir.aspireapps.linker.userservice.config;

import feign.RequestInterceptor;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.Arrays;
import java.util.stream.Collectors;

@Slf4j
@Configuration
public class FeignConfiguration {
    @Bean
    public RequestInterceptor requestInterceptor() {
        return requestTemplate -> {

            ServletRequestAttributes attributes =
                    (ServletRequestAttributes)
                            RequestContextHolder.getRequestAttributes();

            log.info("FeignConfiguration requestInterceptor");

            if (attributes == null) {
                log.warn("No ServletRequestAttributes found");
                return;
            }

            HttpServletRequest request = attributes.getRequest();

            String userId = request.getHeader("X-USER-ID");
            String userName = request.getHeader("X-USERNAME");
            String roles = request.getHeader("X-USER-ROLES");

            if (roles != null && !roles.isBlank()) {

                roles = Arrays.stream(roles.split(","))
                        .map(String::trim)
                        .filter(role -> !role.isBlank())
                        .map(role ->
                                role.startsWith("ROLE_")
                                        ? role
                                        : "ROLE_" + role
                        )
                        .collect(Collectors.joining(","));
            }

            if (userId != null) {
                requestTemplate.header("X-USER-ID", userId);
            }

            if (userName != null) {
                requestTemplate.header("X-USERNAME", userName);
            }

            if (roles != null && !roles.isBlank()) {
                requestTemplate.header("X-USER-ROLES", roles);
            }

            log.info("User Id: {}", userId);
            log.info("User Name: {}", userName);
            log.info("Roles: {}", roles);
        };
    }
}
