package com.example.gateway.config;

import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class GatewayConfig {

    @Bean
    public RouteLocator customRouteLocator(RouteLocatorBuilder builder) {
        return builder.routes()
                .route("account-service", r -> r
                        .path("/api/accounts/**")
                        .uri("lb://account-service"))
                .route("customer-service", r -> r
                        .path("/api/customers/**")
                        .uri("lb://customer-service"))
                .route("process-service", r -> r
                        .path("/api/processes/**")
                        .uri("lb://process-service"))
                .build();
    }
}

