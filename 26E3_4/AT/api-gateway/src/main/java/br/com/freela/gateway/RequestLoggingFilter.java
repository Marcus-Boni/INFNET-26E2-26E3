package br.com.freela.gateway;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.UUID;

@Component
public class RequestLoggingFilter implements GlobalFilter, Ordered {

    private static final Logger log = LoggerFactory.getLogger(RequestLoggingFilter.class);

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        String incomingCorrelationId = exchange.getRequest().getHeaders().getFirst("X-Correlation-Id");
        String correlationId = (incomingCorrelationId != null && !incomingCorrelationId.isBlank())
                ? incomingCorrelationId
                : UUID.randomUUID().toString();

        long inicio = System.currentTimeMillis();

        var mutatedRequest = exchange.getRequest().mutate()
                .header("X-Correlation-Id", correlationId)
                .build();

        exchange.getResponse().getHeaders().set("X-Correlation-Id", correlationId);

        MDC.put("correlationId", correlationId);
        log.info("gateway.request.inicio correlationId={} method={} path={}",
                correlationId, mutatedRequest.getMethod(), mutatedRequest.getURI().getPath());
        MDC.clear();

        return chain.filter(exchange.mutate().request(mutatedRequest).build())
                .doFinally(signal -> {
                    MDC.put("correlationId", correlationId);
                    log.info("gateway.request.fim correlationId={} method={} path={} status={} durationMs={} signal={}",
                            correlationId, mutatedRequest.getMethod(), mutatedRequest.getURI().getPath(),
                            exchange.getResponse().getStatusCode(), System.currentTimeMillis() - inicio, signal);
                    MDC.clear();
                });
    }

    @Override
    public int getOrder() {
        return -100;
    }
}
