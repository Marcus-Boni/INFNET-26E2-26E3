package com.techmarket.orderservice.repository;

import com.techmarket.orderservice.domain.Order;
import com.techmarket.orderservice.domain.OrderItem;
import com.techmarket.orderservice.domain.OrderStatus;
import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Repository
public class OrderRepository {

    private final Map<String, Order> orders = new ConcurrentHashMap<>();

    @PostConstruct
    public void init() {
        // Pedido de Exemplo 1: Vinculado ao administrador
        OrderItem item1 = OrderItem.builder()
                .productId("10")
                .productName("Notebook Dell XPS 15 OLED i9 32GB 1TB SSD")
                .quantity(1)
                .unitPrice(new BigDecimal("12999.00"))
                .build();

        Order order1 = Order.builder()
                .id("1")
                .customerUsername("admin@techmarket.com")
                .customerFullName("Marcus Boni (Administrador)")
                .items(new ArrayList<>(Collections.singletonList(item1)))
                .totalAmount(new BigDecimal("12999.00"))
                .status(OrderStatus.PAGO)
                .createdAt(LocalDateTime.now().minusDays(1))
                .build();
        orders.put(order1.getId(), order1);

        // Pedido de Exemplo 2: Vinculado ao cliente comum
        OrderItem item2 = OrderItem.builder()
                .productId("20")
                .productName("Teclado Mecânico Sem Fio Keychron K2 Pro QMK")
                .quantity(2)
                .unitPrice(new BigDecimal("650.00"))
                .build();

        Order order2 = Order.builder()
                .id("2")
                .customerUsername("cliente@techmarket.com")
                .customerFullName("Cliente TechMarket")
                .items(new ArrayList<>(Collections.singletonList(item2)))
                .totalAmount(new BigDecimal("1300.00"))
                .status(OrderStatus.CONFIRMADO)
                .createdAt(LocalDateTime.now().minusHours(4))
                .build();
        orders.put(order2.getId(), order2);
    }

    public Order save(Order order) {
        if (order.getId() == null) {
            order.setId(String.valueOf(orders.size() + 1));
        }
        orders.put(order.getId(), order);
        return order;
    }

    public List<Order> findAll() {
        return new ArrayList<>(orders.values());
    }

    public Optional<Order> findById(String id) {
        return Optional.ofNullable(orders.get(id));
    }

    public List<Order> findByCustomerUsername(String username) {
        if (username == null) {
            return Collections.emptyList();
        }
        return orders.values().stream()
                .filter(o -> username.equalsIgnoreCase(o.getCustomerUsername()))
                .collect(Collectors.toList());
    }
}
