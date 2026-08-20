package com.infnet.tp3.domain.model;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class OrderTest {

    private Order order;
    private Product p1;
    private Product p2;

    @BeforeEach
    void setUp() {
        order = Order.builder()
                .customerEmail("cliente@exemplo.com")
                .shippingAddress(new Address("Rua das Flores, 123", "Rio de Janeiro", "RJ", "20000-000"))
                .build();
        order.initialize();

        p1 = Product.builder()
                .id(1L)
                .name("Teclado Mecânico")
                .price(new BigDecimal("100.00"))
                .stock(10)
                .build();

        p2 = Product.builder()
                .id(2L)
                .name("Mouse Gamer")
                .price(new BigDecimal("50.00"))
                .stock(5)
                .build();
    }

    @Test
    @DisplayName("Deve inicializar pedido com status PENDING e valor zero")
    void shouldInitializeCorrectly() {
        assertThat(order.getStatus()).isEqualTo(OrderStatus.PENDING);
        assertThat(order.getTotalPrice()).isEqualByComparingTo(BigDecimal.ZERO);
        assertThat(order.getItems()).isEmpty();
    }

    @Test
    @DisplayName("Deve adicionar itens, reduzir estoque e calcular valor total somando itens e frete")
    void shouldAddItemsAndCalculateTotalWithFreight() {
        order.addItem(p1, 2); // 2x 100.00 = 200.00
        order.addItem(p2, 1); // 1x 50.00 = 50.00 (Subtotal = 250.00)

        order.setShipping("Nexus Express Air", new BigDecimal("25.00"), 2, "NX-998877-BR");

        assertThat(p1.getStock()).isEqualTo(8);
        assertThat(p2.getStock()).isEqualTo(4);
        assertThat(order.getItems()).hasSize(2);
        assertThat(order.getItemsTotal()).isEqualByComparingTo(new BigDecimal("250.00"));
        assertThat(order.getShippingCost()).isEqualByComparingTo(new BigDecimal("25.00"));
        assertThat(order.getTotalPrice()).isEqualByComparingTo(new BigDecimal("275.00"));
        assertThat(order.getTrackingNumber()).isEqualTo("NX-998877-BR");
    }

    @Test
    @DisplayName("Deve transitar status para SHIPPED quando pendente")
    void shouldShipOrder() {
        order.ship();
        assertThat(order.getStatus()).isEqualTo(OrderStatus.SHIPPED);
    }

    @Test
    @DisplayName("Deve transitar status para CANCELLED quando pendente")
    void shouldCancelOrder() {
        order.cancel();
        assertThat(order.getStatus()).isEqualTo(OrderStatus.CANCELLED);
    }

    @Test
    @DisplayName("Deve impedir transição de status inválida se não estiver PENDING")
    void shouldPreventInvalidStatusTransitions() {
        order.ship();
        assertThatThrownBy(() -> order.cancel())
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Apenas pedidos PENDENTES");
    }
}
