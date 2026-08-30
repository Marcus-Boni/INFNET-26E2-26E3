package com.techmarket.orderservice.service;

import com.techmarket.orderservice.client.CatalogClient;
import com.techmarket.orderservice.dto.ProductDto;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
@RequiredArgsConstructor
@Slf4j
public class CatalogIntegrationService {

    private final CatalogClient catalogClient;

    // Cache local em memória para demonstrar estratégia de fallback inteligente
    private final Map<String, ProductDto> localProductCache = new ConcurrentHashMap<>();

    /**
     * Consulta informações de produto no catalog-service com proteção de Circuit Breaker e Retry.
     * Caso o serviço remoto falhe, caia em timeout ou o circuito esteja aberto, o método de fallback é executado.
     */
    @CircuitBreaker(name = "catalogService", fallbackMethod = "fetchProductFallback")
    @Retry(name = "catalogService")
    public ProductDto fetchProduct(String productId) {
        log.info("[Resilience4j] Chamando catalog-service para obter produto ID={}", productId);
        ProductDto product = catalogClient.getProductById(productId);
        if (product != null) {
            localProductCache.put(productId, product);
        }
        return product;
    }

    /**
     * Método de Fallback executado quando o catalog-service estiver indisponível,
     * lento (timeout) ou quando o Circuit Breaker estiver com estado OPEN.
     */
    public ProductDto fetchProductFallback(String productId, Throwable throwable) {
        log.warn("[Resilience4j - FALLBACK ATIVADO] Falha ao comunicar com catalog-service para o produto ID={}. Causa: {}",
                productId, throwable.getMessage());

        // Tenta recuperar do cache local se disponível
        if (localProductCache.containsKey(productId)) {
            ProductDto cached = localProductCache.get(productId);
            log.info("[Resilience4j - FALLBACK] Recuperando produto ID={} do cache local resiliente", productId);
            return cached;
        }

        // Caso não esteja em cache, retorna um objeto padrão seguro para não travar a aplicação
        return ProductDto.builder()
                .id(productId)
                .name("Produto " + productId + " (Catálogo Temporariamente Indisponível)")
                .description("Fallback acionado pelo Circuit Breaker devido a instabilidade no catalog-service: " + throwable.getClass().getSimpleName())
                .category("Indisponível")
                .price(new BigDecimal("99.90")) // Preço estimado para contingência
                .stockQuantity(1)
                .active(false)
                .specifications(Map.of("resilienceFallback", true, "error", throwable.getMessage() != null ? throwable.getMessage() : "Unknown"))
                .build();
    }
}
