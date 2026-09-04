package com.portfolio.gateway.filter;

import io.micrometer.tracing.Span;
import io.micrometer.tracing.Tracer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.UUID;

@Component
public class LoggingGlobalFilter implements GlobalFilter, Ordered {

    private static final Logger log =
            LoggerFactory.getLogger(LoggingGlobalFilter.class);

    private static final String CORRELATION_ID = "X-Correlation-ID";

    private final Tracer tracer;

    public LoggingGlobalFilter(Tracer tracer) {
        this.tracer = tracer;
    }

    @Override
    public Mono<Void> filter(
            ServerWebExchange exchange,
            GatewayFilterChain chain) {

        long start = System.currentTimeMillis();

        String path = exchange.getRequest()
                .getURI()
                .getPath();

        // 1. Get correlation ID from incoming request
        String correlationId = exchange.getRequest()
                .getHeaders()
                .getFirst(CORRELATION_ID);

        // 2. If client didn't send one, create one
        if (correlationId == null || correlationId.isBlank()) {
            correlationId = UUID.randomUUID().toString();
        }

        final String finalCorrelationId = correlationId;

        // 3. Add correlation ID to response as well
        exchange.getResponse()
                .getHeaders()
                .set(CORRELATION_ID, finalCorrelationId);

        // 4. Put it into MDC for normal logging
        MDC.put(CORRELATION_ID, finalCorrelationId);

        // DEBUG: prove correlation ID exists
        log.info(
                "DEBUG CORRELATION: correlationId={} | requestHeader={}",
                finalCorrelationId,
                exchange.getRequest()
                        .getHeaders()
                        .getFirst(CORRELATION_ID)
        );

        log.info(
                "Incoming request: {} {} | correlationId={} | traceId={} | spanId={}",
                exchange.getRequest().getMethod(),
                path,
                finalCorrelationId,
                getTraceId(),
                getSpanId()
        );

        return chain.filter(exchange)
                .contextWrite(context ->
                        context.put(CORRELATION_ID, finalCorrelationId)
                )
                .doFinally(signal -> {

                    try {
                        // Restore MDC because this is a reactive callback
                        MDC.put(CORRELATION_ID, finalCorrelationId);

                        log.info(
                                "Completed {} in {} ms with status {} | correlationId={} | traceId={} | spanId={}",
                                path,
                                System.currentTimeMillis() - start,
                                exchange.getResponse().getStatusCode(),
                                finalCorrelationId,
                                getTraceId(),
                                getSpanId()
                        );

                    } finally {
                        MDC.remove(CORRELATION_ID);
                    }
                });
    }

    private String getTraceId() {
        Span span = tracer.currentSpan();

        return span != null
                ? span.context().traceId()
                : "N/A";
    }

    private String getSpanId() {
        Span span = tracer.currentSpan();

        return span != null
                ? span.context().spanId()
                : "N/A";
    }

    @Override
    public int getOrder() {
        return -1;
    }
}