package ir.aspireapps.linker.userservice.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfiguration {
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        return http
                .csrf(AbstractHttpConfigurer::disable)
                .sessionManagement(
                        session -> {
                            session.sessionCreationPolicy(
                                    SessionCreationPolicy.STATELESS
                            );
                        }
                )
                .authorizeHttpRequests(
                        authorize ->
                                authorize.requestMatchers(
                                                "/ir/aspireapps/api/v1/linker/auth/register",
                                                "/ir/aspireapps/api/v1/linker/auth/login",
                                                "/ir/aspireapps/api/v1/linker/auth/refresh"
                                        )
                                        .permitAll()
                                        .anyRequest().authenticated()
                )
                .addFilterAfter(new GatewayHeaderAuthFilter(), UsernamePasswordAuthenticationFilter.class)
                .httpBasic(AbstractHttpConfigurer::disable)
                .formLogin(AbstractHttpConfigurer::disable)
                .build();
    }
}
