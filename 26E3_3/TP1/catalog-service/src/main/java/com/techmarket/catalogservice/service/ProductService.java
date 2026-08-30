package com.techmarket.catalogservice.service;

import com.techmarket.catalogservice.domain.Product;
import com.techmarket.catalogservice.dto.ProductRequest;
import com.techmarket.catalogservice.dto.ProductResponse;
import com.techmarket.catalogservice.repository.ProductRepository;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class ProductService {

    private final ProductRepository productRepository;

    public ProductResponse createProduct(ProductRequest request) {
        Product product = Product.builder()
                .name(request.getName())
                .description(request.getDescription())
                .category(request.getCategory())
                .price(request.getPrice())
                .stockQuantity(request.getStockQuantity())
                .specifications(request.getSpecifications() != null ? request.getSpecifications() : Map.of())
                .tags(request.getTags() != null ? request.getTags() : List.of())
                .active(true)
                .createdAt(Instant.now())
                .updatedAt(Instant.now())
                .build();

        Product saved = productRepository.save(product);
        log.info("Produto cadastrado com sucesso: ID={}, Nome={}", saved.getId(), saved.getName());
        return mapToResponse(saved);
    }

    public List<ProductResponse> getAllProducts(String category, String tag) {
        List<Product> products;
        if (category != null && !category.isBlank()) {
            products = productRepository.findByCategoryIgnoreCase(category);
        } else if (tag != null && !tag.isBlank()) {
            products = productRepository.findByTagsContainingIgnoreCase(tag);
        } else {
            products = productRepository.findAll();
        }
        return products.stream().map(this::mapToResponse).collect(Collectors.toList());
    }

    public ProductResponse getProductById(String id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Produto não encontrado com id: " + id));
        return mapToResponse(product);
    }

    public void deleteProduct(String id) {
        if (!productRepository.existsById(id)) {
            throw new RuntimeException("Produto não encontrado com id: " + id);
        }
        productRepository.deleteById(id);
        log.info("Produto deletado com sucesso: ID={}", id);
    }

    @PostConstruct
    public void seedInitialData() {
        if (productRepository.count() == 0) {
            log.info("Inicializando catálogo com produtos de demonstração no MongoDB...");

            Product p1 = Product.builder()
                    .name("Notebook Gamer Ultra RTX 4080")
                    .description("Notebook de alta performance para inteligência artificial e jogos pesados")
                    .category("Computadores")
                    .price(new BigDecimal("12499.90"))
                    .stockQuantity(15)
                    .active(true)
                    .specifications(Map.of(
                            "gpu", "NVIDIA GeForce RTX 4080 12GB GDDR6X",
                            "cpu", "Intel Core i9-14900HX 24-Cores",
                            "ram", "32GB DDR5 5600MHz",
                            "storage", "1TB NVMe PCIe 4.0 SSD",
                            "display", "16 polegadas QHD+ 240Hz G-Sync"
                    ))
                    .tags(List.of("gamer", "notebook", "rtx", "ia", "alto-desempenho"))
                    .createdAt(Instant.now())
                    .updatedAt(Instant.now())
                    .build();

            Product p2 = Product.builder()
                    .name("Monitor Ultrawide 34 IPS 144Hz")
                    .description("Monitor curvo para produtividade e multitarefas profissionais")
                    .category("Monitores")
                    .price(new BigDecimal("3299.00"))
                    .stockQuantity(25)
                    .active(true)
                    .specifications(Map.of(
                            "resolution", "3440x1440 WQHD",
                            "refreshRate", "144Hz",
                            "panelType", "Nano IPS",
                            "curvature", "1900R",
                            "hdr", "HDR400"
                    ))
                    .tags(List.of("monitor", "ultrawide", "produtividade", "ips"))
                    .createdAt(Instant.now())
                    .updatedAt(Instant.now())
                    .build();

            Product p3 = Product.builder()
                    .name("Smartphone Flagship Pro Max 512GB")
                    .description("Smartphone topo de linha com processador de 3nm e câmera periscópica 5x")
                    .category("Smartphones")
                    .price(new BigDecimal("7999.00"))
                    .stockQuantity(40)
                    .active(true)
                    .specifications(Map.of(
                            "chipset", "Snapdragon 8 Gen 3 (3nm)",
                            "storage", "512GB UFS 4.0",
                            "cameras", "Principal 200MP + Ultrawide 50MP + Teleobjetiva 50MP",
                            "battery", "5000mAh com recarga 120W",
                            "network", "5G Dual SIM + eSIM"
                    ))
                    .tags(List.of("smartphone", "5g", "flagship", "camera"))
                    .createdAt(Instant.now())
                    .updatedAt(Instant.now())
                    .build();

            productRepository.saveAll(List.of(p1, p2, p3));
            log.info("Carga inicial de produtos MongoDB concluída!");
        }
    }

    private ProductResponse mapToResponse(Product product) {
        return ProductResponse.builder()
                .id(product.getId())
                .name(product.getName())
                .description(product.getDescription())
                .category(product.getCategory())
                .price(product.getPrice())
                .stockQuantity(product.getStockQuantity())
                .active(product.getActive())
                .specifications(product.getSpecifications())
                .tags(product.getTags())
                .createdAt(product.getCreatedAt())
                .updatedAt(product.getUpdatedAt())
                .build();
    }
}
