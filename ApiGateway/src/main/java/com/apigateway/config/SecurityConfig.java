package com.apigateway.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.core.convert.converter.Converter;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.oauth2.jwt.Jwt;

/**
 * Spring Security Configuration cho API Gateway
 * Cấu hình OAuth2 Resource Server để verify JWT tokens từ Keycloak
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    /**
     * Configure HTTP Security
     * - Tất cả requests phải được authenticated
     * - Sử dụng JWT từ OAuth2 Resource Server (Keycloak)
     * - Stateless sessions (không dùng cookies)
     */
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                // Disable CSRF vì dùng JWT (stateless)
                .csrf(csrf -> csrf.disable())
                
                // Configure authorization
                .authorizeHttpRequests(auth -> auth
                        // Public endpoints (không cần authentication)
                        .requestMatchers(
                                "/actuator/**",          // Health checks
                                "/error",                // Error page
                                "/api/v1/test/public"    // Public test endpoint
                        ).permitAll()
                        
                        // Admin endpoints - cần role ADMIN
                        .requestMatchers(
                                "/api/v1/admin/**",
                                "/api/v1/test/admin"
                        ).hasRole("Admin")
                        
                        // User endpoints - cần role USER hoặc ADMIN
                        .requestMatchers(
                                "/api/v1/users/**",
                                "/api/v1/test/user"
                        ).hasAnyRole("User", "Admin")
                        
                        // Tất cả requests khác đều cần authenticated
                        .anyRequest().authenticated()
                )
                
                // Configure OAuth2 Resource Server với JWT
                .oauth2ResourceServer(oauth2 -> oauth2
                        .jwt(jwt -> jwt
                                // Custom JWT converter để extract roles từ Keycloak
                                .jwtAuthenticationConverter(jwtAuthenticationConverter())
                        )
                )
                
                // Stateless session - không dùng session cookies
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                );

        return http.build();
    }

    /**
     * JWT Authentication Converter
     * Sử dụng custom converter để extract roles từ Keycloak format
     */
    @Bean
    public Converter<Jwt, AbstractAuthenticationToken> jwtAuthenticationConverter() {
        return new KeycloakJwtAuthenticationConverter();
    }
}

