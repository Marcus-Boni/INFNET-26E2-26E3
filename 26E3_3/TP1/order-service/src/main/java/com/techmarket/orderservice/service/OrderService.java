package com.techmarket.orderservice.service;

import com.techmarket.orderservice.domain.Order;
import com.techmarket.orderservice.domain.OrderItem;
import com.techmarket.orderservice.domain.OrderStatus;
import com.techmarket.orderservice.dto.*;
import com.techmarket.orderservice.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class OrderService {

    private final OrderRepository orderRepository;
    private final CatalogIntegrationService catalogIntegrationService;

    @Transactional
    public OrderResponse createOrder(OrderRequest request) {
        log.info("Iniciando criação de pedido para o cliente={}", request.getCustomerEmail());

        Order order = Order.builder()
                .customerId(request.getCustomerId())
                .customerEmail(request.getCustomerEmail())
                .status(OrderStatus.PENDING)
                .items(new ArrayList<>())
                .createdAt(Instant.now())
                .updatedAt(Instant.now())
                .build();

        BigDecimal total = BigDecimal.ZERO;
        boolean degradedCatalog = false;

        for (OrderItemRequest itemReq : request.getItems()) {
            ProductDto product = catalogIntegrationService.fetchProduct(itemReq.getProductId());

            if (product.getSpecifications() != null && Boolean.TRUE.equals(product.getSpecifications().get("resilienceFallback"))) {
                degradedCatalog = true;
            }

            BigDecimal unitPrice = product.getPrice() != null ? product.getPrice() : BigDecimal.ZERO;
            BigDecimal subtotal = unitPrice.multiply(BigDecimal.valueOf(itemReq.getQuantity()));
            total = total.add(subtotal);

            OrderItem item = OrderItem.builder()
                    .productId(itemReq.getProductId())
                    .productName(product.getName())
                    .unitPrice(unitPrice)
                    .quantity(itemReq.getQuantity())
                    .subtotal(subtotal)
                    .build();

            order.addItem(item);
        }

        order.setTotalAmount(total);
        Order savedOrder = orderRepository.save(order);
        log.info("Pedido criado com sucesso! ID={}, Total={}", savedOrder.getId(), savedOrder.getTotalAmount());

        OrderResponse response = mapToResponse(savedOrder);
        response.setCatalogServiceStatus(degradedCatalog ? "DEGRADED (Circuit Breaker / Fallback)" : "HEALTHY (Online)");
        return response;
    }

    public List<OrderResponse> getAllOrders(String customerId) {
        List<Order> orders;
        if (customerId != null && !customerId.isBlank()) {
            orders = orderRepository.findByCustomerId(customerId);
        } else {
            orders = orderRepository.findAll();
        }
        return orders.stream().map(this::mapToResponse).collect(Collectors.toList());
    }

    public OrderResponse getOrderById(Long id) {
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Pedido não encontrado com ID: " + id));
        return mapToResponse(order);
    }

    @Transactional
    public OrderResponse updateOrderStatus(Long id, OrderStatus status) {
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Pedido não encontrado com ID: " + id));
        order.setStatus(status);
        order.setUpdatedAt(Instant.now());
        Order updated = orderRepository.save(order);
        log.info("Status do pedido ID={} atualizado para {}", id, status);
        return mapToResponse(updated);
    }

    private OrderResponse mapToResponse(Order order) {
        List<OrderItemResponse> itemResponses = order.getItems().stream()
                .map(item -> OrderItemResponse.builder()
                        .id(item.getId())
                        .productId(item.getProductId())
                        .productName(item.getProductName())
                        .unitPrice(item.getUnitPrice())
                        .quantity(item.getQuantity())
                        .subtotal(item.getSubtotal())
                        .build())
                .collect(Collectors.toList());

        return OrderResponse.builder()
                .id(order.getId())
                .customerId(order.getCustomerId())
                .customerEmail(order.getCustomerEmail())
                .status(order.getStatus())
                .totalAmount(order.getTotalAmount())
                .items(itemResponses)
                .catalogServiceStatus("HEALTHY (Online)")
                .createdAt(order.getCreatedAt())
                .updatedAt(order.getUpdatedAt())
                .build();
    }
}
