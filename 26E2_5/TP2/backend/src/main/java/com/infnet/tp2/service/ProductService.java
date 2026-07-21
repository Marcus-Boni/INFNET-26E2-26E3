package com.infnet.tp2.service;

import com.infnet.tp2.domain.model.AuditLog;
import com.infnet.tp2.domain.model.Product;
import com.infnet.tp2.domain.repository.ProductRepository;
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
    public List<Product> findAll() {
        return productRepository.findAll();
    }

    @Transactional(readOnly = true)
    public Optional<Product> findById(Long id) {
        return productRepository.findById(id);
    }

    @Transactional(readOnly = true)
    public List<Product> searchByName(String name) {
        return productRepository.findByNameContainingIgnoreCase(name);
    }

    @Transactional(readOnly = true)
    public List<Product> findByPriceRange(BigDecimal min, BigDecimal max) {
        return productRepository.findByPriceBetween(min, max);
    }

    @Transactional(readOnly = true)
    public List<Product> findLowStock(Integer threshold) {
        return productRepository.findByStockLessThan(threshold);
    }

    public Product save(Product product) {
        boolean isNew = (product.getId() == null);
        Product oldProduct = isNew ? null : productRepository.findById(product.getId()).orElse(null);

        Product saved = productRepository.save(product);

        if (isNew) {
            auditLogService.logChange(
                    "Product",
                    saved.getId(),
                    "CREATE",
                    "Produto '" + saved.getName() + "' criado com preço R$ " + saved.getPrice() + " e estoque " + saved.getStock(),
                    null,
                    "Name: " + saved.getName() + ", Price: " + saved.getPrice() + ", Stock: " + saved.getStock()
            );
        } else if (oldProduct != null) {
            if (oldProduct.getStock() != null && !oldProduct.getStock().equals(saved.getStock())) {
                auditLogService.logChange(
                        "Product",
                        saved.getId(),
                        "STOCK_CHANGE",
                        "Estoque do produto '" + saved.getName() + "' alterado",
                        String.valueOf(oldProduct.getStock()),
                        String.valueOf(saved.getStock())
                );
            }
            if (oldProduct.getPrice() != null && oldProduct.getPrice().compareTo(saved.getPrice()) != 0) {
                auditLogService.logChange(
                        "Product",
                        saved.getId(),
                        "PRICE_CHANGE",
                        "Preço do produto '" + saved.getName() + "' alterado",
                        "R$ " + oldProduct.getPrice(),
                        "R$ " + saved.getPrice()
                );
            }
        }

        return saved;
    }

    public Product updateStock(Long id, int quantityDelta) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Produto não encontrado com id: " + id));
        int oldStock = product.getStock();
        if (quantityDelta > 0) {
            product.increaseStock(quantityDelta);
        } else if (quantityDelta < 0) {
            product.decreaseStock(Math.abs(quantityDelta));
        }
        Product updated = productRepository.save(product);
        auditLogService.logChange(
                "Product",
                id,
                "STOCK_CHANGE",
                "Ajuste manual de estoque no produto '" + updated.getName() + "'",
                String.valueOf(oldStock),
                String.valueOf(updated.getStock())
        );
        return updated;
    }

    public void deleteById(Long id) {
        Product product = productRepository.findById(id).orElse(null);
        if (product != null) {
            productRepository.deleteById(id);
            auditLogService.logChange(
                    "Product",
                    id,
                    "DELETE",
                    "Produto '" + product.getName() + "' removido do catálogo",
                    "Name: " + product.getName() + ", Stock: " + product.getStock(),
                    null
            );
        }
    }

    @Transactional(readOnly = true)
    public List<AuditLog> getProductHistory(Long id) {
        return auditLogService.getHistoryForEntity("Product", id);
    }
}
