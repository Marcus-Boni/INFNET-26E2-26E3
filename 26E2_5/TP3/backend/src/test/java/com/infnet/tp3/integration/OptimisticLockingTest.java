package com.infnet.tp3.integration;

import com.infnet.tp3.domain.model.Product;
import com.infnet.tp3.domain.repository.ProductRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.orm.ObjectOptimisticLockingFailureException;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
class OptimisticLockingTest {

    @Autowired
    private ProductRepository productRepository;

    @Test
    @DisplayName("Deve lançar ObjectOptimisticLockingFailureException quando duas transações tentam atualizar a mesma versão de um produto")
    void shouldThrowOptimisticLockingExceptionOnConcurrentUpdate() {
        Product product = productRepository.save(Product.builder()
                .name("Produto Concorrente")
                .description("Teste de lock otimista")
                .price(new BigDecimal("100.00"))
                .stock(10)
                .build());

        // Simula a primeira thread obtendo a entidade (versão 0)
        Product thread1Product = productRepository.findById(product.getId()).orElseThrow();

        // Simula a segunda thread obtendo a mesma versão
        Product thread2Product = productRepository.findById(product.getId()).orElseThrow();

        // Thread 1 atualiza e salva com sucesso (incrementa versão para 1)
        thread1Product.setStock(8);
        productRepository.saveAndFlush(thread1Product);

        // Thread 2 tenta salvar a instância antiga com a versão defasada
        thread2Product.setStock(5);
        assertThatThrownBy(() -> productRepository.saveAndFlush(thread2Product))
                .isInstanceOf(ObjectOptimisticLockingFailureException.class);
    }
}
