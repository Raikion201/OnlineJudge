package com.apigateway.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.Ordered;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Component;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

@Component
public class UserHeaderFilter implements GlobalFilter, Ordered {

    private static final Logger log = LoggerFactory.getLogger(UserHeaderFilter.class);
    private static final String USER_HEADER = "X-User-Id";

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        return exchange.getPrincipal()
                .filter(principal -> principal instanceof JwtAuthenticationToken)
                .cast(JwtAuthenticationToken.class)
                .flatMap(jwtAuth -> {
                    String keycloakId = jwtAuth.getToken().getSubject();
                    String headerValue = keycloakId != null ? keycloakId
                            : jwtAuth.getToken().getClaimAsString("preferred_username");

                    if (headerValue != null) {
                        log.debug("Injecting {} header with value {}", USER_HEADER, headerValue);
                        ServerHttpRequest mutatedRequest = exchange.getRequest().mutate()
                                .headers(headers -> headers.set(USER_HEADER, headerValue))
                                .build();

                        return chain.filter(exchange.mutate().request(mutatedRequest).build());
                    }

                    log.debug("No user identifier found in JWT, proceeding without header injection");
                    return chain.filter(exchange);
                })
                .switchIfEmpty(chain.filter(exchange));
    }

    @Override
    public int getOrder() {
        return Ordered.LOWEST_PRECEDENCE;
    }
}

