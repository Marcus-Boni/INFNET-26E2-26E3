package com.techmarket.orderservice.service;

import com.techmarket.orderservice.domain.Order;
import com.techmarket.orderservice.domain.OrderItem;
import com.techmarket.orderservice.domain.OrderStatus;
import com.techmarket.orderservice.dto.CreateOrderRequest;
import com.techmarket.orderservice.dto.OrderItemDto;
import com.techmarket.orderservice.dto.OrderResponse;
import com.techmarket.orderservice.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class OrderService {

    private final OrderRepository orderRepository;

    public List<OrderResponse> getAllOrders(String currentUsername, boolean isAdmin) {
        log.info("Buscando pedidos. Solicitante: '{}' (Admin: {})", currentUsername, isAdmin);
        List<Order> orders = orderRepository.findAll();
        return orders.stream()
                .map(o -> mapToResponse(o, currentUsername))
                .collect(Collectors.toList());
    }

    public OrderResponse getOrderById(String id, String currentUsername, boolean isAdmin) {
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Pedido não encontrado com o ID: " + id));

        return mapToResponse(order, currentUsername);
    }

    public OrderResponse createOrder(CreateOrderRequest request, String currentUsername, String currentFullName) {
        log.info("Criando novo pedido para o usuário autenticado: '{}'", currentUsername);

        List<OrderItem> items = new ArrayList<>();

        // Cenário 1: Itens enviados em lista
        if (request.getItems() != null && !request.getItems().isEmpty()) {
            for (OrderItemDto dto : request.getItems()) {
                items.add(OrderItem.builder()
                        .productId(dto.getProductId())
                        .productName(dto.getProductName() != null ? dto.getProductName() : "Produto ID " + dto.getProductId())
                        .quantity(dto.getQuantity())
                        .unitPrice(dto.getUnitPrice() != null ? dto.getUnitPrice() : new BigDecimal("99.90"))
                        .build());
            }
        }
        // Cenário 2: Item único simplificado (compatibilidade TP2)
        else if (request.getProductId() != null) {
            String name = request.getProductName() != null ? request.getProductName() : "Produto Tech #" + request.getProductId();
            int qty = request.getQuantity() != null ? request.getQuantity() : 1;
            BigDecimal price = request.getUnitPrice() != null ? request.getUnitPrice() : new BigDecimal("149.90");

            items.add(OrderItem.builder()
                    .productId(request.getProductId())
                    .productName(name)
                    .quantity(qty)
                    .unitPrice(price)
                    .build());
        } else {
            // Item padrão caso nenhum seja fornecido
            items.add(OrderItem.builder()
                    .productId("100")
                    .productName("Kit Periféricos TechMarket Pro")
                    .quantity(1)
                    .unitPrice(new BigDecimal("350.00"))
                    .build());
        }

        BigDecimal total = items.stream()
                .map(OrderItem::getSubtotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        String customerName = (request.getCustomerName() != null && !request.getCustomerName().isBlank()) ?
                request.getCustomerName() :
                (currentFullName != null ? currentFullName : currentUsername);

        Order newOrder = Order.builder()
                .customerUsername(currentUsername)
                .customerFullName(customerName)
                .items(items)
                .totalAmount(total)
                .status(OrderStatus.CRIADO)
                .createdAt(LocalDateTime.now())
                .build();

        Order saved = orderRepository.save(newOrder);
        log.info("Pedido #{} criado com sucesso no valor total de R$ {}", saved.getId(), saved.getTotalAmount());

        return mapToResponse(saved, currentUsername);
    }

    private OrderResponse mapToResponse(Order order, String currentUsername) {
        return OrderResponse.builder()
                .id(order.getId())
                .customerUsername(order.getCustomerUsername())
                .customerFullName(order.getCustomerFullName())
                .items(order.getItems())
                .totalAmount(order.getTotalAmount())
                .status(order.getStatus())
                .createdAt(order.getCreatedAt())
                .authenticatedAs(currentUsername)
                .build();
    }
}
