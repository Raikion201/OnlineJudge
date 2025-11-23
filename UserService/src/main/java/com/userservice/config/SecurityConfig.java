package com.userservice.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.core.convert.converter.Converter;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.oauth2.jwt.Jwt;

/**
 * Spring Security Configuration cho User Service
 * Bảo vệ các endpoints với JWT từ Keycloak
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                
                .authorizeHttpRequests(auth -> auth
                        // Public endpoints
                        .requestMatchers(
                                "/actuator/**",
                                "/error"
                        ).permitAll()
                        
                        // GET endpoints - cho phép mọi người xem (authenticated)
                        .requestMatchers(HttpMethod.GET, "/api/v1/users/**").authenticated()
                        
                        // User profile endpoints - chỉ chính user hoặc admin
                        .requestMatchers("/api/v1/users/me").authenticated()
                        
                        // POST endpoints - create user
                        .requestMatchers(HttpMethod.POST, "/api/v1/users").hasAnyRole("Admin", "User")
                        
                        // PUT endpoints - update user
                        .requestMatchers(HttpMethod.PUT, "/api/v1/users/**").hasAnyRole("Admin", "User")
                        
                        // DELETE endpoints - delete user
                        .requestMatchers(HttpMethod.DELETE, "/api/v1/users/**").hasAnyRole("Admin", "User")
                        
                        // Tất cả requests khác cần authenticated
                        .anyRequest().authenticated()
                )
                
                .oauth2ResourceServer(oauth2 -> oauth2
                        .jwt(jwt -> jwt
                                .jwtAuthenticationConverter(jwtAuthenticationConverter())
                        )
                )
                
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                );

        return http.build();
    }

    @Bean
    public Converter<Jwt, AbstractAuthenticationToken> jwtAuthenticationConverter() {
        return new KeycloakJwtAuthenticationConverter();
    }
}

