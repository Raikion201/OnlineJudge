package com.problemservice.config;

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
 * Spring Security Configuration cho Problem Service
 * 
 * Permission Strategy:
 * - GET endpoints: Public (authenticated) - mọi user đều xem được
 * - POST/PUT/DELETE: Admin hoặc Creator - chỉ admin hoặc người tạo mới sửa/xóa được
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
                        
                        // GET endpoints - mọi authenticated user đều xem được problems
                        .requestMatchers(HttpMethod.GET, "/api/v1/problems/**").authenticated()
                        .requestMatchers(HttpMethod.GET, "/api/v1/tags/**").authenticated()
                        
                        // Problem endpoints - POST/PUT/DELETE
                        .requestMatchers(HttpMethod.POST, "/api/v1/problems/**").hasAnyRole("User", "Admin")
                        .requestMatchers(HttpMethod.PUT, "/api/v1/problems/**").hasAnyRole("User", "Admin")
                        .requestMatchers(HttpMethod.DELETE, "/api/v1/problems/**").hasAnyRole("User", "Admin")
                        
                        // Tag endpoints - chỉ Admin
                        .requestMatchers(HttpMethod.POST, "/api/v1/tags/**").hasRole("Admin")
                        .requestMatchers(HttpMethod.PUT, "/api/v1/tags/**").hasRole("Admin")
                        .requestMatchers(HttpMethod.DELETE, "/api/v1/tags/**").hasRole("Admin")
                        
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

