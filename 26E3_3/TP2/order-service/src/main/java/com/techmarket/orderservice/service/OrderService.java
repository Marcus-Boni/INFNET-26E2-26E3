package com.techmarket.orderservice.service;

import com.techmarket.orderservice.client.ProductClient;
import com.techmarket.orderservice.domain.Order;
import com.techmarket.orderservice.domain.OrderStatus;
import com.techmarket.orderservice.dto.OrderRequest;
import com.techmarket.orderservice.dto.OrderResponse;
import com.techmarket.orderservice.dto.ProductDto;
import com.techmarket.orderservice.repository.OrderRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class OrderService {

    private final OrderRepository orderRepository;
    private final ProductClient productClient;
    private final String hostname;

    public OrderService(OrderRepository orderRepository, ProductClient productClient) {
        this.orderRepository = orderRepository;
        this.productClient = productClient;
        this.hostname = resolveHostname();
    }

    public OrderResponse createOrder(OrderRequest request) {
        // Etapa 7: Consulta o product-service para verificar se o produto existe
        ProductDto product = productClient.getProductById(request.getProductId())
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.BAD_REQUEST,
                        "Não foi possível criar o pedido: Produto com ID '" + request.getProductId() + "' não existe no product-service."
                ));

        BigDecimal unitPrice = product.getPrice();
        BigDecimal totalPrice = unitPrice.multiply(BigDecimal.valueOf(request.getQuantity()));

        Order order = Order.builder()
                .customerName(request.getCustomerName())
                .productId(product.getId())
                .productName(product.getName())
                .quantity(request.getQuantity())
                .unitPrice(unitPrice)
                .totalPrice(totalPrice)
                .status(OrderStatus.CONFIRMED)
                .createdAt(LocalDateTime.now())
                .validatedByPod(product.getServedBy() != null ? product.getServedBy() : "standalone")
                .build();

        Order saved = orderRepository.save(order);
        return mapToResponse(saved);
    }

    public List<OrderResponse> getAllOrders() {
        return orderRepository.findAll()
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    public OrderResponse getOrderById(String id) {
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Pedido não encontrado com o ID: " + id
                ));
        return mapToResponse(order);
    }

    public String getHostIdentity() {
        return this.hostname;
    }

    private OrderResponse mapToResponse(Order order) {
        return OrderResponse.builder()
                .id(order.getId())
                .customerName(order.getCustomerName())
                .productId(order.getProductId())
                .productName(order.getProductName())
                .quantity(order.getQuantity())
                .unitPrice(order.getUnitPrice())
                .totalPrice(order.getTotalPrice())
                .status(order.getStatus())
                .createdAt(order.getCreatedAt())
                .validatedByPod(order.getValidatedByPod())
                .handledByInstance(this.hostname)
                .build();
    }

    private String resolveHostname() {
        String envHost = System.getenv("HOSTNAME");
        if (envHost != null && !envHost.isBlank()) {
            return envHost;
        }
        try {
            return InetAddress.getLocalHost().getHostName();
        } catch (UnknownHostException e) {
            return "unknown-order-host";
        }
    }
}
