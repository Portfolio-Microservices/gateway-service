package com.portfolio.gateway.config;

import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class GatewayConfig {

    @Bean
    public RouteLocator routes(RouteLocatorBuilder builder) {
        return builder.routes()
                .route("auth-service", r -> r.path("/api/v1/auth/**")
                        .uri("http://localhost:8081"))
                .route("project-service", r -> r.path("/api/v1/projects/**")
                        .uri("http://localhost:8082"))
                .route("content-service-blogs", r -> r.path("/api/v1/blogs/**")
                        .uri("http://localhost:8083"))
                .route("content-service-skills", r -> r.path("/api/v1/skills/**")
                        .uri("http://localhost:8083"))
                .route("content-service-contact", r -> r.path("/api/v1/contact/**")
                        .uri("http://localhost:8083"))
                .build();
    }
}
