package com.infnet.tp3.repository;

import com.infnet.tp3.domain.model.Product;
import com.infnet.tp3.domain.repository.ProductRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
class ProductRepositoryTest {

    @Autowired
    private ProductRepository productRepository;

    @BeforeEach
    void setUp() {
        productRepository.deleteAll();

        productRepository.save(Product.builder()
                .name("Teclado Mecânico RGB")
                .description("Teclado gamer switch blue")
                .price(new BigDecimal("250.00"))
                .stock(10)
                .build());

        productRepository.save(Product.builder()
                .name("Mouse Sem Fio")
                .description("Mouse ergonômico silencioso")
                .price(new BigDecimal("120.00"))
                .stock(2)
                .build());

        productRepository.save(Product.builder()
                .name("Monitor 4K")
                .description("Monitor profissional IPS")
                .price(new BigDecimal("1800.00"))
                .stock(0)
                .build());
    }

    @Test
    @DisplayName("Deve buscar produtos contendo substring no nome ignorando case")
    void shouldFindByNameContainingIgnoreCase() {
        List<Product> results = productRepository.findByNameContainingIgnoreCase("teclado");
        assertThat(results).hasSize(1);
        assertThat(results.get(0).getName()).isEqualTo("Teclado Mecânico RGB");
    }

    @Test
    @DisplayName("Deve buscar produtos com estoque abaixo do threshold")
    void shouldFindByStockLessThan() {
        List<Product> lowStock = productRepository.findByStockLessThan(5);
        assertThat(lowStock).hasSize(2); // Mouse (2) e Monitor (0)
    }

    @Test
    @DisplayName("Deve buscar produtos por faixa de preço")
    void shouldFindByPriceBetween() {
        List<Product> results = productRepository.findByPriceBetween(new BigDecimal("100.00"), new BigDecimal("300.00"));
        assertThat(results).hasSize(2);
    }
}
