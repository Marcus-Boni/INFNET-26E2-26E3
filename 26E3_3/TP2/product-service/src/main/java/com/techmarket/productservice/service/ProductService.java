package com.techmarket.productservice.service;

import com.techmarket.productservice.domain.Product;
import com.techmarket.productservice.dto.ProductRequest;
import com.techmarket.productservice.dto.ProductResponse;
import com.techmarket.productservice.repository.ProductRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.List;

@Service
public class ProductService {

    private final ProductRepository productRepository;
    private final String hostname;

    public ProductService(ProductRepository productRepository) {
        this.productRepository = productRepository;
        this.hostname = resolveHostname();
    }

    public ProductResponse createProduct(ProductRequest request) {
        Product product = Product.builder()
                .name(request.getName())
                .description(request.getDescription())
                .price(request.getPrice())
                .stockQuantity(request.getStockQuantity())
                .build();

        Product saved = productRepository.save(product);
        return mapToResponse(saved);
    }

    public List<ProductResponse> getAllProducts() {
        return productRepository.findAll()
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    public ProductResponse getProductById(String id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Produto não encontrado com id: " + id));
        return mapToResponse(product);
    }

    public String getHostIdentity() {
        return this.hostname;
    }

    private ProductResponse mapToResponse(Product product) {
        return ProductResponse.builder()
                .id(product.getId())
                .name(product.getName())
                .description(product.getDescription())
                .price(product.getPrice())
                .stockQuantity(product.getStockQuantity())
                .servedBy(this.hostname)
                .build();
    }

    private String resolveHostname() {
        String envHost = System.getenv("HOSTNAME");
        if (envHost != null && !envHost.isBlank()) {
            return envHost;
        }
        try {
            return InetAddress.getLocalHost().getHostName();
        } catch (UnknownHostException e) {
            return "unknown-host";
        }
    }
}
