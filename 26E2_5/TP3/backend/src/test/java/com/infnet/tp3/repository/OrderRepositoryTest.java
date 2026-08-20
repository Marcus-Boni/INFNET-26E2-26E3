package com.infnet.tp3.repository;

import com.infnet.tp3.domain.model.*;
import com.infnet.tp3.domain.repository.OrderRepository;
import com.infnet.tp3.domain.repository.ProductRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
class OrderRepositoryTest {

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private ProductRepository productRepository;

    @Test
    @DisplayName("Deve salvar pedido com itens e carregar coleções via findAllWithItems")
    void shouldSaveAndFindAllWithItems() {
        Product product = productRepository.save(Product.builder()
                .name("Notebook Gamer")
                .price(new BigDecimal("4500.00"))
                .stock(10)
                .build());

        Order order = Order.builder()
                .customerEmail("pedidos@infnet.com")
                .shippingAddress(new Address("Av. Rio Branco, 1", "Rio de Janeiro", "RJ", "20000-000"))
                .shippingCarrier("Nexus Express Air")
                .shippingCost(new BigDecimal("35.00"))
                .trackingNumber("NX-778899-BR")
                .build();
        order.initialize();
        order.addItem(product, 1);

        Order saved = orderRepository.save(order);

        List<Order> orders = orderRepository.findAllWithItems();
        assertThat(orders).isNotEmpty();
        assertThat(orders.get(0).getItems()).hasSize(1);
        assertThat(orders.get(0).getTrackingNumber()).isEqualTo("NX-778899-BR");
    }

    @Test
    @DisplayName("Deve buscar pedido por código de rastreamento")
    void shouldFindByTrackingNumber() {
        Order order = Order.builder()
                .customerEmail("teste.tracking@infnet.com")
                .shippingAddress(new Address("Rua Teste, 10", "São Paulo", "SP", "01000-000"))
                .shippingCarrier("LogBrasil Rodoviário")
                .shippingCost(new BigDecimal("18.00"))
                .trackingNumber("NX-112233-BR")
                .build();
        order.initialize();

        orderRepository.save(order);

        Optional<Order> found = orderRepository.findByTrackingNumber("NX-112233-BR");
        assertThat(found).isPresent();
        assertThat(found.get().getCustomerEmail()).isEqualTo("teste.tracking@infnet.com");
    }
}
