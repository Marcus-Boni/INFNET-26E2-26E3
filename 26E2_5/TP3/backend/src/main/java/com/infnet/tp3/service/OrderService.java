package com.infnet.tp3.service;

import com.infnet.tp3.client.ShippingClient;
import com.infnet.tp3.client.dto.CreateShipmentRequest;
import com.infnet.tp3.client.dto.ShipmentDetailsDto;
import com.infnet.tp3.client.dto.UpdateShipmentStatusRequest;
import com.infnet.tp3.controller.dto.OrderRequest;
import com.infnet.tp3.domain.model.*;
import com.infnet.tp3.domain.repository.OrderRepository;
import com.infnet.tp3.domain.repository.ProductRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class OrderService {

    private static final Logger log = LoggerFactory.getLogger(OrderService.class);

    private final OrderRepository orderRepository;
    private final ProductRepository productRepository;
    private final AuditLogService auditLogService;
    private final ShippingClient shippingClient;

    @Autowired
    public OrderService(
            OrderRepository orderRepository,
            ProductRepository productRepository,
            AuditLogService auditLogService,
            ShippingClient shippingClient
    ) {
        this.orderRepository = orderRepository;
        this.productRepository = productRepository;
        this.auditLogService = auditLogService;
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

        // Integração Distribuída: Registro no Microsserviço de Frete via Spring Cloud OpenFeign
        try {
            CreateShipmentRequest shipmentReq = CreateShipmentRequest.builder()
                    .orderId(savedOrder.getId())
                    .customerEmail(savedOrder.getCustomerEmail())
                    .carrier(savedOrder.getShippingCarrier())
                    .serviceType(request.getServiceType() != null ? request.getServiceType() : "STANDARD")
                    .freightCost(savedOrder.getShippingCost())
                    .estimatedDeliveryDays(savedOrder.getEstimatedDeliveryDays())
                    .street(address.getStreet())
                    .city(address.getCity())
                    .state(address.getState())
                    .zipCode(address.getZipCode())
                    .build();

            ShipmentDetailsDto shipment = shippingClient.createShipment(shipmentReq);
            if (shipment != null && shipment.getTrackingNumber() != null) {
                savedOrder.setTrackingNumber(shipment.getTrackingNumber());
                savedOrder = orderRepository.save(savedOrder);

                auditLogService.logChange(
                        "Order",
                        savedOrder.getId(),
                        "SHIPPING_REGISTERED",
                        "Envio registrado no microsserviço de logística. Código: " + shipment.getTrackingNumber(),
                        null,
                        "Transportadora: " + savedOrder.getShippingCarrier() + ", Frete: R$ " + savedOrder.getShippingCost()
                );
            }
        } catch (Exception ex) {
            log.error("Falha ao comunicar com microsserviço de frete ao criar pedido #{}", savedOrder.getId(), ex);
            // Fallback: não interrompe a transação do pedido
            savedOrder.setTrackingNumber("NX-PENDING-" + savedOrder.getId() + "-BR");
            savedOrder = orderRepository.save(savedOrder);
        }

        auditLogService.logChange(
                "Order",
                savedOrder.getId(),
                "CREATE",
                "Pedido registrado com " + savedOrder.getItems().size() + " itens por " + savedOrder.getCustomerEmail(),
                null,
                "Status: PENDING, Total: R$ " + savedOrder.getTotalPrice() + " (Itens: R$ " + savedOrder.getItemsTotal() + " + Frete: R$ " + savedOrder.getShippingCost() + ")"
        );

        return savedOrder;
    }

    public Order shipOrder(Long id) {
        Order order = orderRepository.findWithItemsById(id)
                .orElseThrow(() -> new IllegalArgumentException("Pedido não encontrado com id: " + id));

        OrderStatus oldStatus = order.getStatus();
        order.ship();
        Order updated = orderRepository.save(order);

        // Notifica o microsserviço de frete sobre o despacho
        if (order.getTrackingNumber() != null && !order.getTrackingNumber().startsWith("NX-PENDING")) {
            try {
                UpdateShipmentStatusRequest updateReq = UpdateShipmentStatusRequest.builder()
                        .status("DISPATCHED")
                        .message("Pedido despachado da central de distribuição Nexus Store.")
                        .location("Expedição - CD Nexus Store (SP)")
                        .build();
                shippingClient.updateShipmentStatus(order.getTrackingNumber(), updateReq);
            } catch (Exception ex) {
                log.warn("Não foi possível sincronizar despacho com microsserviço de frete: {}", ex.getMessage());
            }
        }

        auditLogService.logChange(
                "Order",
                id,
                "STATUS_CHANGE",
                "Pedido enviado para transporte",
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

        // Notifica o microsserviço de frete do cancelamento
        if (order.getTrackingNumber() != null && !order.getTrackingNumber().startsWith("NX-PENDING")) {
            try {
                UpdateShipmentStatusRequest updateReq = UpdateShipmentStatusRequest.builder()
                        .status("CANCELLED")
                        .message("Envio cancelado devido ao cancelamento do pedido.")
                        .location("CD Nexus Store")
                        .build();
                shippingClient.updateShipmentStatus(order.getTrackingNumber(), updateReq);
            } catch (Exception ex) {
                log.warn("Não foi possível sincronizar cancelamento com microsserviço de frete: {}", ex.getMessage());
            }
        }

        auditLogService.logChange(
                "Order",
                id,
                "STATUS_CHANGE",
                "Pedido cancelado pelo cliente",
                oldStatus.name(),
                updated.getStatus().name()
        );

        return updated;
    }

    @Transactional(readOnly = true)
    public ShipmentDetailsDto getOrderTracking(Long orderId) {
        Order order = orderRepository.findWithItemsById(orderId)
                .orElseThrow(() -> new IllegalArgumentException("Pedido não encontrado com id: " + orderId));

        if (order.getTrackingNumber() != null) {
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
