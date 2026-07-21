package com.infnet.tp2.repository;

import com.infnet.tp2.domain.model.*;
import com.infnet.tp2.domain.repository.OrderRepository;
import org.junit.jupiter.api.BeforeEach;
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

    private Order sampleOrder;

    @BeforeEach
    void setUp() {
        orderRepository.deleteAll();

        Address address = new Address("Rua das Flores, 123", "São Paulo", "SP", "01234-567");

        OrderItem item1 = OrderItem.builder()
                .productId(1L)
                .productName("Teclado")
                .quantity(2)
                .unitPrice(new BigDecimal("100.00"))
                .build();

        sampleOrder = Order.builder()
                .customerEmail("cliente@teste.com")
                .shippingAddress(address)
                .status(OrderStatus.PENDING)
                .totalPrice(new BigDecimal("200.00"))
                .build();
        sampleOrder.initialize();
        sampleOrder.getItems().add(item1);

        sampleOrder = orderRepository.save(sampleOrder);
    }

    @Test
    @DisplayName("Deve buscar pedido por ID trazendo itens via @EntityGraph")
    void shouldFindWithItemsById() {
        Optional<Order> found = orderRepository.findWithItemsById(sampleOrder.getId());
        assertThat(found).isPresent();
        assertThat(found.get().getItems()).hasSize(1);
        assertThat(found.get().getItems().get(0).getProductName()).isEqualTo("Teclado");
    }

    @Test
    @DisplayName("Deve buscar pedidos por e-mail do cliente")
    void shouldFindByCustomerEmail() {
        List<Order> orders = orderRepository.findByCustomerEmailOrderByCreatedAtDesc("cliente@teste.com");
        assertThat(orders).hasSize(1);
        assertThat(orders.get(0).getCustomerEmail()).isEqualTo("cliente@teste.com");
    }

    @Test
    @DisplayName("Deve buscar pedidos por status")
    void shouldFindByStatus() {
        List<Order> pendingOrders = orderRepository.findByStatus(OrderStatus.PENDING);
        assertThat(pendingOrders).hasSize(1);

        List<Order> shippedOrders = orderRepository.findByStatus(OrderStatus.SHIPPED);
        assertThat(shippedOrders).isEmpty();
    }
}
