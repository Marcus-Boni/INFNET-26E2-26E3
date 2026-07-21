package com.infnet.tp2.repository;

import com.infnet.tp2.domain.model.Product;
import com.infnet.tp2.domain.repository.ProductRepository;
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
                .description("Switch Blue")
                .price(new BigDecimal("250.00"))
                .stock(10)
                .build());

        productRepository.save(Product.builder()
                .name("Mouse Sem Fio")
                .description("3200 DPI")
                .price(new BigDecimal("100.00"))
                .stock(2)
                .build());

        productRepository.save(Product.builder()
                .name("Monitor 4K")
                .description("32 polegadas")
                .price(new BigDecimal("1500.00"))
                .stock(15)
                .build());
    }

    @Test
    @DisplayName("Deve buscar produtos pelo nome ignorando case")
    void shouldFindByNameContainingIgnoreCase() {
        List<Product> products = productRepository.findByNameContainingIgnoreCase("teclado");
        assertThat(products).hasSize(1);
        assertThat(products.get(0).getName()).isEqualTo("Teclado Mecânico RGB");
    }

    @Test
    @DisplayName("Deve buscar produtos com estoque abaixo do limite")
    void shouldFindByStockLessThan() {
        List<Product> lowStockProducts = productRepository.findByStockLessThan(5);
        assertThat(lowStockProducts).hasSize(1);
        assertThat(lowStockProducts.get(0).getName()).isEqualTo("Mouse Sem Fio");
    }

    @Test
    @DisplayName("Deve buscar produtos por faixa de preço")
    void shouldFindByPriceBetween() {
        List<Product> products = productRepository.findByPriceBetween(new BigDecimal("50.00"), new BigDecimal("300.00"));
        assertThat(products).hasSize(2);
    }
}
