package com.submissionservice.submission.config;

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
 * Spring Security Configuration cho Submission Service
 * 
 * Permission Strategy:
 * - POST /submissions: Authenticated users (submit code)
 * - GET /submissions/user/me: User chỉ xem submissions của mình
 * - GET /submissions/{id}: User chỉ xem submission của mình hoặc Admin xem tất cả
 * - Internal endpoints: Chỉ cho Judge Service (sẽ dùng service-to-service auth)
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
                        
                        // Internal endpoints - Judge Service gọi để update status
                        // TODO: Sau này sẽ bảo vệ bằng service-to-service auth
                        .requestMatchers(HttpMethod.PUT, "/api/v1/internal/submissions/*/status").permitAll()
                        
                        // POST submissions - submit code
                        .requestMatchers(HttpMethod.POST, "/api/v1/submissions").authenticated()
                        
                        // GET submissions - view submissions
                        .requestMatchers(HttpMethod.GET, "/api/v1/submissions/user/me").authenticated()
                        .requestMatchers(HttpMethod.GET, "/api/v1/submissions/*").authenticated()
                        
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

