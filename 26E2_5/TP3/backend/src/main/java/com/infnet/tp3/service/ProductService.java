package com.infnet.tp3.service;

import com.infnet.tp3.controller.dto.ProductRequest;
import com.infnet.tp3.domain.model.AuditLog;
import com.infnet.tp3.domain.model.Product;
import com.infnet.tp3.domain.repository.ProductRepository;
import com.infnet.tp3.infrastructure.exception.ProductNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class ProductService {

    private final ProductRepository productRepository;
    private final AuditLogService auditLogService;

    @Autowired
    public ProductService(ProductRepository productRepository, AuditLogService auditLogService) {
        this.productRepository = productRepository;
        this.auditLogService = auditLogService;
    }

    @Transactional(readOnly = true)
    public List<Product> findAll(String search, BigDecimal minPrice, BigDecimal maxPrice, Boolean lowStock) {
        if (search != null && !search.trim().isEmpty()) {
            return productRepository.searchByKeyword(search.trim());
        }
        if (minPrice != null && maxPrice != null) {
            return productRepository.findByPriceBetween(minPrice, maxPrice);
        }
        if (Boolean.TRUE.equals(lowStock)) {
            return productRepository.findByStockLessThan(5);
        }
        return productRepository.findAll();
    }

    @Transactional(readOnly = true)
    public Optional<Product> findById(Long id) {
        return productRepository.findById(id);
    }

    public Product create(ProductRequest request) {
        Product product = Product.builder()
                .name(request.getName())
                .description(request.getDescription())
                .price(request.getPrice())
                .stock(request.getStock())
                .build();

        Product saved = productRepository.save(product);

        auditLogService.logChange(
                "Product",
                saved.getId(),
                "CREATE",
                "Produto cadastrado: " + saved.getName(),
                null,
                "Preço: R$ " + saved.getPrice() + ", Estoque: " + saved.getStock()
        );

        return saved;
    }

    public Product update(Long id, ProductRequest request) {
        Product existing = productRepository.findById(id)
                .orElseThrow(() -> new ProductNotFoundException(id));

        String oldValues = "Nome: " + existing.getName() + ", Preço: R$ " + existing.getPrice() + ", Estoque: " + existing.getStock();

        existing.setName(request.getName());
        existing.setDescription(request.getDescription());
        existing.setPrice(request.getPrice());
        existing.setStock(request.getStock());

        Product updated = productRepository.save(existing);

        String newValues = "Nome: " + updated.getName() + ", Preço: R$ " + updated.getPrice() + ", Estoque: " + updated.getStock();

        auditLogService.logChange(
                "Product",
                updated.getId(),
                "UPDATE",
                "Produto atualizado no catálogo",
                oldValues,
                newValues
        );

        return updated;
    }

    public Product adjustStock(Long id, int delta) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ProductNotFoundException(id));

        int oldStock = product.getStock();
        int newStock = oldStock + delta;
        if (newStock < 0) {
            throw new IllegalArgumentException("O ajuste resultaria em estoque negativo. Estoque atual: " + oldStock);
        }

        product.setStock(newStock);
        Product saved = productRepository.save(product);

        auditLogService.logChange(
                "Product",
                saved.getId(),
                "STOCK_CHANGE",
                "Ajuste manual de estoque (" + (delta >= 0 ? "+" + delta : delta) + " un.)",
                String.valueOf(oldStock),
                String.valueOf(newStock)
        );

        return saved;
    }

    public void delete(Long id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ProductNotFoundException(id));

        auditLogService.logChange(
                "Product",
                id,
                "DELETE",
                "Produto removido do catálogo: " + product.getName(),
                "Preço: R$ " + product.getPrice() + ", Estoque: " + product.getStock(),
                null
        );

        productRepository.delete(product);
    }

    @Transactional(readOnly = true)
    public List<AuditLog> getProductHistory(Long id) {
        return auditLogService.getHistoryForEntity("Product", id);
    }
}
