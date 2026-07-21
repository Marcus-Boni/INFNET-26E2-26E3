package com.infnet.tp2.integration;

import com.infnet.tp2.domain.model.Product;
import com.infnet.tp2.domain.repository.ProductRepository;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
class OptimisticLockingTest {

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private EntityManager entityManager;

    @Test
    @DisplayName("Deve lançar ObjectOptimisticLockingFailureException em caso de modificação concorrente de entidade")
    void shouldThrowOptimisticLockExceptionOnConcurrentUpdate() {
        Product p = Product.builder()
                .name("Smartphone Z")
                .description("Top de linha")
                .price(new BigDecimal("3000.00"))
                .stock(20)
                .build();

        Product savedProduct = productRepository.save(p);

        // Fetch two separate instances representing concurrent requests
        Product instance1 = productRepository.findById(savedProduct.getId()).orElseThrow();
        Product instance2 = productRepository.findById(savedProduct.getId()).orElseThrow();

        // First user updates stock
        instance1.setStock(15);
        productRepository.saveAndFlush(instance1);

        // Second user tries to update price using stale entity instance (version mismatch)
        instance2.setPrice(new BigDecimal("2800.00"));

        assertThatThrownBy(() -> productRepository.saveAndFlush(instance2))
                .isInstanceOf(ObjectOptimisticLockingFailureException.class);
    }
}
