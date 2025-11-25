package com.apigateway.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.convert.converter.Converter;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtTimestampValidator;
import org.springframework.security.oauth2.jwt.NimbusReactiveJwtDecoder;
import org.springframework.security.oauth2.jwt.ReactiveJwtDecoder;
import org.springframework.security.oauth2.core.DelegatingOAuth2TokenValidator;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.security.web.server.SecurityWebFilterChain;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Configuration
@EnableWebFluxSecurity
public class SecurityConfig {

    private static final Logger log = LoggerFactory.getLogger(SecurityConfig.class);

    @Value("${spring.security.oauth2.resourceserver.jwt.issuer-uri:http://keycloak:8080/realms/online-judge}")
    private String issuerUri;

    @Bean
    public SecurityWebFilterChain securityWebFilterChain(ServerHttpSecurity http) {
        log.info("Configuring Security Web Filter Chain");
        log.info("JWT Issuer URI: {}", issuerUri);
        
        http
            .cors(cors -> cors.disable()) // Will use global CORS config
            .csrf(csrf -> csrf.disable())
            .authorizeExchange(exchanges -> exchanges
                // Allow CORS preflight requests
                .pathMatchers(org.springframework.http.HttpMethod.OPTIONS, "/**").permitAll()
                // Public endpoints
                .pathMatchers("/api/v1/users/register", "/api/v1/users/login").permitAll()
                .pathMatchers("/public/**").permitAll()
                .pathMatchers("/actuator/**").permitAll()
                
                // Public GET for problems (anyone can view)
                .pathMatchers(org.springframework.http.HttpMethod.GET, "/api/v1/problems", "/api/v1/problems/**").permitAll()
                .pathMatchers(org.springframework.http.HttpMethod.GET, "/api/v1/tags", "/api/v1/tags/**").permitAll()
                
                // Protected endpoints với roles (POST, PUT, DELETE require auth)
                .pathMatchers("/api/v1/problems/**").hasAnyRole("User", "Admin")
                .pathMatchers("/api/v1/tags/**").hasAnyRole("User", "Admin")
                .pathMatchers("/api/v1/submissions/**").hasAnyRole("User", "Admin")
                .pathMatchers("/api/v1/judge/**").hasAnyRole("User", "Admin")
                .pathMatchers("/api/v1/executor/**").hasAnyRole("User", "Admin")
                .pathMatchers("/api/v1/users/**").hasAnyRole("User", "Admin")
                
                // Tất cả requests khác cần authenticated
                .anyExchange().authenticated()
            )
            .oauth2ResourceServer(oauth2 -> oauth2
                .jwt(jwt -> jwt.jwtAuthenticationConverter(jwtAuthenticationConverter()))
            );
        
        log.info("Security Web Filter Chain configured successfully");
        return http.build();
    }

    @Bean
    public ReactiveJwtDecoder jwtDecoder() {
        log.info("Creating ReactiveJwtDecoder with JWK Set URI from Keycloak");
        String jwkSetUri = issuerUri + "/protocol/openid-connect/certs";
        log.info("JWK Set URI: {}", jwkSetUri);
        
        try {
            NimbusReactiveJwtDecoder jwtDecoder = NimbusReactiveJwtDecoder.withJwkSetUri(jwkSetUri).build();
            
            // Remove issuer validation to support both internal (keycloak:8080) and external (keycloak:8321) tokens
            jwtDecoder.setJwtValidator(new DelegatingOAuth2TokenValidator<>(
                new JwtTimestampValidator()
                // Removed JwtIssuerValidator to allow flexible issuer
            ));
            
            log.info("ReactiveJwtDecoder created successfully without issuer validation");
            return jwtDecoder;
        } catch (Exception e) {
            log.error("Failed to create ReactiveJwtDecoder: {}", e.getMessage(), e);
            throw e;
        }
    }

    @Bean
    public Converter<Jwt, Mono<JwtAuthenticationToken>> jwtAuthenticationConverter() {
        return jwt -> {
            log.debug("Converting JWT to Authentication Token");
            log.debug("JWT Subject: {}", jwt.getSubject());
            log.debug("JWT Issuer: {}", jwt.getIssuer());
            log.debug("JWT Claims: {}", jwt.getClaims());
            
            // Extract roles from Keycloak token
            Collection<GrantedAuthority> authorities = extractAuthorities(jwt);
            log.info("Extracted authorities for user {}: {}", 
                jwt.getClaim("preferred_username"), 
                authorities.stream()
                    .map(GrantedAuthority::getAuthority)
                    .collect(Collectors.joining(", ")));
            
            return Mono.just(new JwtAuthenticationToken(jwt, authorities));
        };
    }
    
    private Collection<GrantedAuthority> extractAuthorities(Jwt jwt) {
        log.debug("Extracting authorities from JWT");
        Map<String, Object> realmAccess = jwt.getClaim("realm_access");
        Collection<GrantedAuthority> authorities = new ArrayList<>();
        
        if (realmAccess != null && realmAccess.containsKey("roles")) {
            @SuppressWarnings("unchecked")
            List<String> roles = (List<String>) realmAccess.get("roles");
            log.debug("Found roles in realm_access: {}", roles);
            
            authorities = roles.stream()
                .filter(role -> !role.startsWith("default-") && 
                               !role.equals("offline_access") && 
                               !role.equals("uma_authorization"))
                .map(role -> {
                    String authority = "ROLE_" + role;
                    log.debug("Mapping role '{}' to authority '{}'", role, authority);
                    return new SimpleGrantedAuthority(authority);
                })
                .collect(Collectors.toList());
        } else {
            log.warn("No realm_access or roles found in JWT token");
        }
        
        log.debug("Total authorities extracted: {}", authorities.size());
        return authorities;
    }
}

