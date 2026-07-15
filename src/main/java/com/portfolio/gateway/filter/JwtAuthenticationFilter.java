package com.portfolio.gateway.filter;

import java.nio.charset.StandardCharsets;
import java.util.List;

import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;

import com.portfolio.gateway.security.JwtTokenProvider;

import reactor.core.publisher.Mono;

@Component
public class JwtAuthenticationFilter implements GlobalFilter, Ordered {

    private final JwtTokenProvider jwtTokenProvider;

    public JwtAuthenticationFilter(JwtTokenProvider jwtTokenProvider) {
        this.jwtTokenProvider = jwtTokenProvider;
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        String path = exchange.getRequest().getURI().getPath();

        if (isPublicPath(path)) {
            return chain.filter(exchange);
        }

        String authHeader = exchange.getRequest().getHeaders().getFirst(HttpHeaders.AUTHORIZATION);
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return unauthorized(exchange, "Missing or invalid Authorization header");
        }

        String token = authHeader.substring(7);
        if (!jwtTokenProvider.validateAccessToken(token)) {
            return unauthorized(exchange, "Invalid or expired token");
        }

        String username = jwtTokenProvider.getUsername(token);
        String role = jwtTokenProvider.getRole(token);
        Long userId = jwtTokenProvider.getUserId(token);

        ServerHttpRequest mutatedRequest = exchange.getRequest().mutate()
                .header("X-User-Id", String.valueOf(userId))
                .header("X-Username", username)
                .header("X-Role", role)
                .build();

        return chain.filter(exchange.mutate().request(mutatedRequest).build());
    }

    private static final List<String> PUBLIC_PATHS = List.of(
            "/api/v1/auth",
            "/api/v1/projects",
            "/api/v1/blogs",
            "/api/v1/skills",
            "/api/v1/contact",
            "/api/v1/admin",
            "/actuator"
    );

    private boolean isPublicPath(String path) {
        return PUBLIC_PATHS.stream()
                .anyMatch(publicPath ->
                        path.equals(publicPath) || path.startsWith(publicPath + "/"));
    }

    private Mono<Void> unauthorized(ServerWebExchange exchange, String message) {
        ServerHttpResponse response = exchange.getResponse();
        response.setStatusCode(HttpStatus.UNAUTHORIZED);
        response.getHeaders().setContentType(org.springframework.http.MediaType.APPLICATION_JSON);

        String body = "{\"error\":\"Unauthorized\",\"message\":\"" + message + "\"}";
        return response.writeWith(Mono.just(response.bufferFactory().wrap(body.getBytes(StandardCharsets.UTF_8))));
    }

    @Override
    public int getOrder() {
        return -1;
    }
    
}
