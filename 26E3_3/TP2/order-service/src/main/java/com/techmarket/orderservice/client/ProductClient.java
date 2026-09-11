package com.techmarket.orderservice.client;

import com.techmarket.orderservice.dto.ProductDto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClient;
import org.springframework.web.server.ResponseStatusException;

import java.util.Optional;

@Component
public class ProductClient {

    private static final Logger log = LoggerFactory.getLogger(ProductClient.class);

    private final RestClient restClient;
    private final String productServiceUrl;

    public ProductClient(
            RestClient.Builder restClientBuilder,
            @Value("${product-service.url:http://localhost:8081/products}") String productServiceUrl) {
        this.productServiceUrl = productServiceUrl;
        this.restClient = restClientBuilder.build();
        log.info("ProductClient inicializado com endpoint: {}", productServiceUrl);
    }

    public Optional<ProductDto> getProductById(String productId) {
        String targetUrl = productServiceUrl + "/" + productId;
        log.info("Consultando microsserviço remoto via HTTP GET em: {}", targetUrl);

        try {
            var response = restClient.get()
                    .uri(targetUrl)
                    .retrieve()
                    .onStatus(HttpStatusCode::is4xxClientError, (request, resp) -> {
                        log.warn("Produto com id '{}' retornou código de cliente: {}", productId, resp.getStatusCode());
                    })
                    .toEntity(ProductDto.class);

            if (response.getStatusCode().is2xxSuccessful() 
                    && response.getBody() != null 
                    && response.getBody().getId() != null 
                    && response.getBody().getPrice() != null) {
                return Optional.of(response.getBody());
            }
            return Optional.empty();
        } catch (HttpClientErrorException.NotFound ex) {
            log.warn("Produto com id '{}' não encontrado (404)", productId);
            return Optional.empty();
        } catch (Exception ex) {
            log.error("Falha ao comunicar com o product-service na URL '{}': {}", targetUrl, ex.getMessage());
            throw new ResponseStatusException(
                    HttpStatus.SERVICE_UNAVAILABLE,
                    "Falha de comunicação com o product-service em " + targetUrl + ": " + ex.getMessage(),
                    ex
            );
        }
    }

    public String getProductServiceUrl() {
        return this.productServiceUrl;
    }
}
