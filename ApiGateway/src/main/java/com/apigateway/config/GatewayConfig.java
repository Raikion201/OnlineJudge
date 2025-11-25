package com.apigateway.config;

import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class GatewayConfig {

    @Bean
    public RouteLocator customRouteLocator(RouteLocatorBuilder builder) {
        return builder.routes()
                // User Service routes
                .route("user-service", r -> r
                        .path("/api/v1/users/**")
                        .filters(f -> f.tokenRelay())
                        .uri("lb://user-service"))
                
                // Problem Service routes
                .route("problem-service", r -> r
                        .path("/api/v1/problems/**", "/api/v1/tags/**")
                        .filters(f -> f.tokenRelay())
                        .uri("lb://problem-service"))
                
                // Submission Service routes
                .route("submission-service", r -> r
                        .path("/api/v1/submissions/**")
                        .filters(f -> f.tokenRelay())
                        .uri("lb://submission-service"))
                
                // Judge Service routes
                .route("judge-service", r -> r
                        .path("/api/v1/judge/**")
                        .filters(f -> f.tokenRelay())
                        .uri("lb://judge-service"))
                
                // Code Executor Service routes
                .route("code-executor-service", r -> r
                        .path("/api/v1/executor/**")
                        .filters(f -> f.tokenRelay())
                        .uri("lb://code-executor-service"))
                
                .build();
    }
}

