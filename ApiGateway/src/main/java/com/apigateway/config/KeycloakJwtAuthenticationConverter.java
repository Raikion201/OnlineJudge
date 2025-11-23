package com.apigateway.config;

import org.springframework.core.convert.converter.Converter;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Custom JWT Authentication Converter cho Keycloak
 * Extract roles từ "realm_access.roles" trong JWT token
 */
public class KeycloakJwtAuthenticationConverter implements Converter<Jwt, AbstractAuthenticationToken> {

    /**
     * Convert JWT token sang AbstractAuthenticationToken
     * Extract roles từ Keycloak format: realm_access.roles[]
     */
    @Override
    public AbstractAuthenticationToken convert(Jwt jwt) {
        // Extract authorities (roles) từ JWT
        Collection<GrantedAuthority> authorities = extractAuthorities(jwt);
        
        // Tạo JwtAuthenticationToken với authorities
        return new JwtAuthenticationToken(jwt, authorities);
    }

    /**
     * Extract authorities từ JWT token
     * Keycloak đặt roles trong nested object: { "realm_access": { "roles": ["User", "Admin"] } }
     */
    private Collection<GrantedAuthority> extractAuthorities(Jwt jwt) {
        // Get realm_access claim
        Map<String, Object> realmAccess = jwt.getClaim("realm_access");
        
        if (realmAccess == null) {
            return Collections.emptyList();
        }
        
        // Get roles array từ realm_access
        Object rolesObj = realmAccess.get("roles");
        
        if (!(rolesObj instanceof List)) {
            return Collections.emptyList();
        }
        
        @SuppressWarnings("unchecked")
        List<String> roles = (List<String>) rolesObj;
        
        // Convert roles thành GrantedAuthority với prefix "ROLE_"
        return roles.stream()
                .map(role -> new SimpleGrantedAuthority("ROLE_" + role))
                .collect(Collectors.toList());
    }
}

