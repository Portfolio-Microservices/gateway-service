package com.portfolio.gateway.filter;

import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.UUID;

@Component
public class CorrelationIdFilter implements GlobalFilter, Ordered {

	private static final String CORRELATION_ID = "X-Correlation-ID";

	@Override
	public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {

		String correlationId = exchange.getRequest().getHeaders().getFirst(CORRELATION_ID);

		if (correlationId == null || correlationId.isBlank()) {
			correlationId = UUID.randomUUID().toString();
		}

		ServerHttpRequest request = exchange.getRequest().mutate().header(CORRELATION_ID, correlationId).build();

		return chain.filter(exchange.mutate().request(request).build());
	}

	@Override
	public int getOrder() {
		return -100;
	}
}