package com.infnet.tp5.service;

import com.infnet.tp5.client.ShippingClient;
import com.infnet.tp5.client.dto.ShipmentDetailsDto;
import com.infnet.tp5.controller.dto.OrderRequest;
import com.infnet.tp5.domain.model.*;
import com.infnet.tp5.domain.repository.OrderRepository;
import com.infnet.tp5.domain.repository.ProductRepository;
import com.infnet.tp5.event.OrderCancelledEvent;
import com.infnet.tp5.event.OrderCreatedEvent;
import com.infnet.tp5.event.OrderDispatchedEvent;
import com.infnet.tp5.event.OrderItemEventDto;
import com.infnet.tp5.infrastructure.rabbitmq.OrderEventPublisher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@Transactional
public class OrderService {

    private static final Logger log = LoggerFactory.getLogger(OrderService.class);

    private final OrderRepository orderRepository;
    private final ProductRepository productRepository;
    private final AuditLogService auditLogService;
    private final OrderEventPublisher orderEventPublisher;
    private final ShippingClient shippingClient;

    @Autowired
    public OrderService(
            OrderRepository orderRepository,
            ProductRepository productRepository,
            AuditLogService auditLogService,
            OrderEventPublisher orderEventPublisher,
            ShippingClient shippingClient
    ) {
        this.orderRepository = orderRepository;
        this.productRepository = productRepository;
        this.auditLogService = auditLogService;
        this.orderEventPublisher = orderEventPublisher;
        this.shippingClient = shippingClient;
    }

    @Transactional(readOnly = true)
    public List<Order> findAll() {
        return orderRepository.findAllWithItems();
    }

    @Transactional(readOnly = true)
    public Optional<Order> findById(Long id) {
        return orderRepository.findWithItemsById(id);
    }

    @Transactional(readOnly = true)
    public List<Order> findByCustomerEmail(String email) {
        return orderRepository.findByCustomerEmailOrderByCreatedAtDesc(email);
    }

    @Transactional(readOnly = true)
    public List<Order> findByStatus(OrderStatus status) {
        return orderRepository.findByStatus(status);
    }

    public Order createOrder(OrderRequest request) {
        Address address = new Address(
                request.getStreet(),
                request.getCity(),
                request.getState(),
                request.getZipCode()
        );

        Order order = Order.builder()
                .customerEmail(request.getCustomerEmail())
                .shippingAddress(address)
                .shippingCarrier(request.getCarrier() != null ? request.getCarrier() : "LogBrasil Padrão")
                .shippingCost(request.getShippingCost() != null ? request.getShippingCost() : BigDecimal.ZERO)
                .estimatedDeliveryDays(request.getEstimatedDeliveryDays() != null ? request.getEstimatedDeliveryDays() : 3)
                .trackingNumber("AGUARDANDO_LOGISTICA")
                .build();
        order.initialize();

        if (request.getItems() == null || request.getItems().isEmpty()) {
            throw new IllegalArgumentException("O pedido deve conter pelo menos um item.");
        }

        for (var itemReq : request.getItems()) {
            Product product = productRepository.findById(itemReq.getProductId())
                    .orElseThrow(() -> new IllegalArgumentException("Produto não encontrado com id: " + itemReq.getProductId()));

            int oldStock = product.getStock();
            order.addItem(product, itemReq.getQuantity());
            productRepository.save(product);

            auditLogService.logChange(
                    "Product",
                    product.getId(),
                    "STOCK_CHANGE",
                    "Estoque reduzido devido ao pedido para '" + request.getCustomerEmail() + "'",
                    String.valueOf(oldStock),
                    String.valueOf(product.getStock())
            );
        }

        Order savedOrder = orderRepository.save(order);

        // EDA: Publicação de evento assíncrono para o RabbitMQ (desacoplamento total de microsserviços)
        List<OrderItemEventDto> itemDtos = savedOrder.getItems().stream()
                .map(i -> OrderItemEventDto.builder()
                        .productId(i.getProductId())
                        .productName(i.getProductName())
                        .quantity(i.getQuantity())
                        .unitPrice(i.getUnitPrice())
                        .build())
                .collect(Collectors.toList());

        String correlationId = "corr-order-" + savedOrder.getId() + "-" + UUID.randomUUID().toString().substring(0, 8);

        OrderCreatedEvent event = OrderCreatedEvent.builder()
                .eventId(UUID.randomUUID().toString())
                .correlationId(correlationId)
                .eventType("ORDER_CREATED")
                .timestamp(LocalDateTime.now())
                .orderId(savedOrder.getId())
                .customerEmail(savedOrder.getCustomerEmail())
                .carrier(savedOrder.getShippingCarrier())
                .serviceType(request.getServiceType() != null ? request.getServiceType() : "STANDARD")
                .shippingCost(savedOrder.getShippingCost())
                .estimatedDeliveryDays(savedOrder.getEstimatedDeliveryDays())
                .itemsTotal(savedOrder.getItemsTotal())
                .totalPrice(savedOrder.getTotalPrice())
                .street(address.getStreet())
                .city(address.getCity())
                .state(address.getState())
                .zipCode(address.getZipCode())
                .items(itemDtos)
                .build();

        orderEventPublisher.publishOrderCreated(event);

        auditLogService.logChange(
                "Order",
                savedOrder.getId(),
                "CREATE",
                "Pedido registrado com " + savedOrder.getItems().size() + " itens por " + savedOrder.getCustomerEmail() + ". Evento ORDER_CREATED enviado via RabbitMQ.",
                null,
                "Status: PENDING, Total: R$ " + savedOrder.getTotalPrice() + " (Itens: R$ " + savedOrder.getItemsTotal() + " + Frete: R$ " + savedOrder.getShippingCost() + "), CorrelationId: " + correlationId
        );

        return savedOrder;
    }

    public Order shipOrder(Long id) {
        Order order = orderRepository.findWithItemsById(id)
                .orElseThrow(() -> new IllegalArgumentException("Pedido não encontrado com id: " + id));

        OrderStatus oldStatus = order.getStatus();
        order.ship();
        Order updated = orderRepository.save(order);

        // Publica evento de despacho no RabbitMQ
        OrderDispatchedEvent dispatchedEvent = OrderDispatchedEvent.builder()
                .eventId(UUID.randomUUID().toString())
                .correlationId("corr-ship-" + id + "-" + UUID.randomUUID().toString().substring(0, 8))
                .eventType("ORDER_DISPATCHED")
                .timestamp(LocalDateTime.now())
                .orderId(id)
                .trackingNumber(order.getTrackingNumber())
                .status("DISPATCHED")
                .message("Pedido despachado da central de distribuição Nexus Store.")
                .location("Expedição - CD Nexus Store (SP)")
                .build();

        orderEventPublisher.publishOrderDispatched(dispatchedEvent);

        auditLogService.logChange(
                "Order",
                id,
                "STATUS_CHANGE",
                "Pedido enviado para transporte. Evento ORDER_DISPATCHED publicado no RabbitMQ.",
                oldStatus.name(),
                updated.getStatus().name()
        );

        return updated;
    }

    public Order cancelOrder(Long id) {
        Order order = orderRepository.findWithItemsById(id)
                .orElseThrow(() -> new IllegalArgumentException("Pedido não encontrado com id: " + id));

        OrderStatus oldStatus = order.getStatus();
        order.cancel();

        for (var item : order.getItems()) {
            Product product = productRepository.findById(item.getProductId())
                    .orElseThrow(() -> new IllegalStateException("Produto não encontrado ao restaurar estoque: " + item.getProductId()));
            int oldStock = product.getStock();
            product.increaseStock(item.getQuantity());
            productRepository.save(product);

            auditLogService.logChange(
                    "Product",
                    product.getId(),
                    "STOCK_CHANGE",
                    "Estoque restaurado devido ao cancelamento do Pedido #" + id,
                    String.valueOf(oldStock),
                    String.valueOf(product.getStock())
            );
        }

        Order updated = orderRepository.save(order);

        // Publica evento de cancelamento no RabbitMQ (Compensação Distribuída / Saga Choreography)
        OrderCancelledEvent cancelledEvent = OrderCancelledEvent.builder()
                .eventId(UUID.randomUUID().toString())
                .correlationId("corr-cancel-" + id + "-" + UUID.randomUUID().toString().substring(0, 8))
                .eventType("ORDER_CANCELLED")
                .timestamp(LocalDateTime.now())
                .orderId(id)
                .trackingNumber(order.getTrackingNumber())
                .reason("Cancelamento solicitado pelo cliente ou operador")
                .build();

        orderEventPublisher.publishOrderCancelled(cancelledEvent);

        auditLogService.logChange(
                "Order",
                id,
                "STATUS_CHANGE",
                "Pedido cancelado pelo cliente. Evento ORDER_CANCELLED publicado no RabbitMQ.",
                oldStatus.name(),
                updated.getStatus().name()
        );

        return updated;
    }

    @Transactional(readOnly = true)
    public ShipmentDetailsDto getOrderTracking(Long orderId) {
        Order order = orderRepository.findWithItemsById(orderId)
                .orElseThrow(() -> new IllegalArgumentException("Pedido não encontrado com id: " + orderId));

        if (order.getTrackingNumber() != null && !order.getTrackingNumber().startsWith("AGUARDANDO")) {
            return shippingClient.getShipmentByTrackingNumber(order.getTrackingNumber());
        } else {
            return shippingClient.getShipmentByOrderId(orderId);
        }
    }

    @Transactional(readOnly = true)
    public List<AuditLog> getOrderHistory(Long id) {
        return auditLogService.getHistoryForEntity("Order", id);
    }
}

