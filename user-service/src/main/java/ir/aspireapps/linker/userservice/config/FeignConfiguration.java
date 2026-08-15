package ir.aspireapps.linker.userservice.config;

import feign.RequestInterceptor;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

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
            if (attributes != null) {
                HttpServletRequest request = attributes.getRequest();
                String userId = request.getHeader("X-USER-ID");
                String userName = request.getHeader("X-USERNAME");
                String role = request.getHeader("X-USER-ROLES");
                requestTemplate.header("X-USER-ID", userId);
                requestTemplate.header("X-USERNAME", userName);
                requestTemplate.header("X-USER-ROLES", role);
                log.info("User Id {}", userId);
                log.info("User Name {}", userName);
                log.info("Role {}", role);
            }

        };
    }
}
