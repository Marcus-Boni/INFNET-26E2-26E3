package com.infnet.tp2.domain.model;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

class OrderTest {

    private Product keyboard;
    private Product mouse;
    private Order order;

    @BeforeEach
    void setUp() {
        keyboard = Product.builder()
                .id(1L)
                .name("Teclado Mecânico")
                .description("Teclado mecânico gamer RGB")
                .price(new BigDecimal("350.00"))
                .stock(10)
                .build();

        mouse = Product.builder()
                .id(2L)
                .name("Mouse Ergonômico")
                .description("Mouse sem fio vertical")
                .price(new BigDecimal("150.00"))
                .stock(5)
                .build();

        order = Order.builder()
                .customerEmail("cliente@exemplo.com")
                .shippingAddress(new Address("Rua Teste, 123", "Cidade", "ST", "12345-678"))
                .build();
        order.initialize();
    }

    @Test
    void shouldInitializeOrderAsPendingWithZeroTotal() {
        assertEquals(OrderStatus.PENDING, order.getStatus());
        assertEquals(BigDecimal.ZERO, order.getTotalPrice());
        assertTrue(order.getItems().isEmpty());
    }

    @Test
    void shouldAddItemAndCalculateTotalPriceAndDecreaseStock() {
        order.addItem(keyboard, 2);

        assertEquals(1, order.getItems().size());
        assertEquals(8, keyboard.getStock()); // Invariant: stock decreases
        assertEquals(new BigDecimal("700.00"), order.getTotalPrice());

        OrderItem item = order.getItems().get(0);
        assertEquals(1L, item.getProductId());
        assertEquals("Teclado Mecânico", item.getProductName());
        assertEquals(2, item.getQuantity());
        assertEquals(new BigDecimal("350.00"), item.getUnitPrice());
    }

    @Test
    void shouldCalculateTotalForMultipleItems() {
        order.addItem(keyboard, 1); // R$ 350
        order.addItem(mouse, 2);    // R$ 300 (2 * 150)

        assertEquals(2, order.getItems().size());
        assertEquals(9, keyboard.getStock());
        assertEquals(3, mouse.getStock());
        assertEquals(new BigDecimal("650.00"), order.getTotalPrice());
    }

    @Test
    void shouldThrowExceptionWhenAddingItemWithInsufficientStock() {
        assertThrows(IllegalStateException.class, () -> order.addItem(mouse, 6));
        assertEquals(5, mouse.getStock()); // Stock unchanged
        assertTrue(order.getItems().isEmpty());
    }

    @Test
    void shouldTransitionToShippedOnlyFromPending() {
        order.addItem(keyboard, 1);
        order.ship();

        assertEquals(OrderStatus.SHIPPED, order.getStatus());

        // Cannot ship again
        assertThrows(IllegalStateException.class, () -> order.ship());
        // Cannot cancel shipped order
        assertThrows(IllegalStateException.class, () -> order.cancel());
    }

    @Test
    void shouldTransitionToCancelledOnlyFromPending() {
        order.addItem(keyboard, 1);
        order.cancel();

        assertEquals(OrderStatus.CANCELLED, order.getStatus());

        // Cannot cancel again
        assertThrows(IllegalStateException.class, () -> order.cancel());
        // Cannot ship cancelled order
        assertThrows(IllegalStateException.class, () -> order.ship());
    }
}
