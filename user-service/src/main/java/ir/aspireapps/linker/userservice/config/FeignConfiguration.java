package ir.aspireapps.linker.userservice.config;

import feign.Logger;
import feign.RequestInterceptor;
import ir.aspireapps.linker.common.utility.HeaderConstants;
import ir.aspireapps.linker.common.utility.LoggingConstants;
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
    public Logger.Level feignLoggerLevel() {
        return Logger.Level.FULL;
    }

    @Bean
    public RequestInterceptor requestInterceptor() {
        return requestTemplate -> {

            ServletRequestAttributes attributes =
                    (ServletRequestAttributes)
                            RequestContextHolder.getRequestAttributes();

            if (attributes == null) return;

            HttpServletRequest request = attributes.getRequest();

            String userId = request.getHeader(HeaderConstants.X_USER_ID);
            String userName = request.getHeader(HeaderConstants.X_USERNAME);
            String roles = request.getHeader(HeaderConstants.X_USER_ROLES);
            String status = request.getHeader(HeaderConstants.X_USER_STATE);
            String requestId = request.getHeader(LoggingConstants.REQUEST_ID_HEADER);

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
                requestTemplate.header(HeaderConstants.X_USER_ID, userId);
            }

            if (userName != null) {
                requestTemplate.header(HeaderConstants.X_USERNAME, userName);
            }

            if (roles != null && !roles.isBlank()) {
                requestTemplate.header(HeaderConstants.X_USER_ROLES, roles);
            }

            if (status != null && !status.isBlank()) {
                requestTemplate.header(HeaderConstants.X_USER_STATE, status);
            }

            if (requestId != null && !requestId.isBlank()) {
                log.info("FeignConfiguration - inspector detected requestId: {} and attached it to request", requestId);
                requestTemplate.header(LoggingConstants.REQUEST_ID_HEADER, requestId);
            } else {
                log.error("FeignConfiguration - inspector did not detected any requestId");
            }
        };
    }
}
