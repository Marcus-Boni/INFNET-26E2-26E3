package com.infnet.tp5.service;

import com.infnet.tp5.client.ShippingClient;
import com.infnet.tp5.controller.dto.OrderItemRequest;
import com.infnet.tp5.controller.dto.OrderRequest;
import com.infnet.tp5.domain.model.*;
import com.infnet.tp5.domain.repository.AuditLogRepository;
import com.infnet.tp5.domain.repository.EventLogRepository;
import com.infnet.tp5.domain.repository.OrderRepository;
import com.infnet.tp5.domain.repository.ProductRepository;
import com.infnet.tp5.event.OrderCreatedEvent;
import com.infnet.tp5.event.OrderDispatchedEvent;
import com.infnet.tp5.event.OrderCancelledEvent;
import com.infnet.tp5.event.ShipmentCreatedEvent;
import com.infnet.tp5.infrastructure.config.RabbitMqConfig;
import com.infnet.tp5.infrastructure.rabbitmq.ShipmentEventListener;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.amqp.core.MessagePostProcessor;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@SpringBootTest
class OrderServiceDistributedTest {

    @Autowired
    private OrderService orderService;

    @Autowired
    private ShipmentEventListener shipmentEventListener;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private AuditLogRepository auditLogRepository;

    @Autowired
    private EventLogRepository eventLogRepository;

    @MockBean
    private RabbitTemplate rabbitTemplate;

    @MockBean
    private ShippingClient shippingClient;

    private Product product;

    @BeforeEach
    void setUp() {
        orderRepository.deleteAll();
        auditLogRepository.deleteAll();
        eventLogRepository.deleteAll();
        productRepository.deleteAll();

        product = productRepository.save(Product.builder()
                .name("Monitor Gamer 144Hz")
                .description("Monitor Curvo")
                .price(new BigDecimal("1500.00"))
                .stock(10)
                .build());
    }

    @Test
    @DisplayName("Deve criar pedido, deduzir estoque e publicar OrderCreatedEvent via RabbitMQ (EDA)")
    void shouldCreateOrderAndPublishOrderCreatedEvent() {
        OrderRequest request = OrderRequest.builder()
                .customerEmail("comprador@infnet.com")
                .street("Av. Presidente Vargas, 100")
                .city("Rio de Janeiro")
                .state("RJ")
                .zipCode("20071-001")
                .carrier("Nexus Express Air")
                .serviceType("EXPRESS")
                .shippingCost(new BigDecimal("29.90"))
                .estimatedDeliveryDays(2)
                .items(List.of(OrderItemRequest.builder()
                        .productId(product.getId())
                        .quantity(2)
                        .build()))
                .build();

        Order order = orderService.createOrder(request);

        assertThat(order).isNotNull();
        assertThat(order.getId()).isNotNull();
        // Na arquitetura orientada a eventos, o pedido é salvo rapidamente com status aguardando sincronização
        assertThat(order.getTrackingNumber()).isEqualTo("AGUARDANDO_LOGISTICA");
        assertThat(order.getShippingCarrier()).isEqualTo("Nexus Express Air");
        assertThat(order.getShippingCost()).isEqualByComparingTo(new BigDecimal("29.90"));
        assertThat(order.getItemsTotal()).isEqualByComparingTo(new BigDecimal("3000.00"));
        assertThat(order.getTotalPrice()).isEqualByComparingTo(new BigDecimal("3029.90"));

        // Verifica que o estoque do produto foi decrementado
        Product updatedProduct = productRepository.findById(product.getId()).orElseThrow();
        assertThat(updatedProduct.getStock()).isEqualTo(8);

        // Verifica que a publicação assíncrona do evento no RabbitMQ foi disparada
        verify(rabbitTemplate, times(1)).convertAndSend(
                eq(RabbitMqConfig.EXCHANGE_ORDER),
                eq(RabbitMqConfig.ROUTING_KEY_ORDER_CREATED),
                any(OrderCreatedEvent.class),
                any(MessagePostProcessor.class)
        );

        // Verifica persistência de log de auditoria
        List<AuditLog> orderLogs = auditLogRepository.findByEntityNameOrderByTimestampDesc("Order");
        assertThat(orderLogs).isNotEmpty();
    }

    @Test
    @DisplayName("Deve atualizar código de rastreamento ao consumir ShipmentCreatedEvent assincronamente do RabbitMQ")
    void shouldHandleShipmentCreatedEventFromRabbitMq() {
        OrderRequest request = OrderRequest.builder()
                .customerEmail("rastreio@infnet.com")
                .street("Rua São José, 90")
                .city("Rio de Janeiro")
                .state("RJ")
                .zipCode("20010-020")
                .carrier("Nexus Express")
                .shippingCost(new BigDecimal("20.00"))
                .estimatedDeliveryDays(3)
                .items(List.of(OrderItemRequest.builder()
                        .productId(product.getId())
                        .quantity(1)
                        .build()))
                .build();

        Order createdOrder = orderService.createOrder(request);
        assertThat(createdOrder.getTrackingNumber()).isEqualTo("AGUARDANDO_LOGISTICA");

        // Simula o recebimento do evento emitido pelo microsserviço de frete via RabbitMQ
        ShipmentCreatedEvent shipmentEvent = ShipmentCreatedEvent.builder()
                .eventId(UUID.randomUUID().toString())
                .correlationId("corr-test-123")
                .eventType("SHIPMENT_CREATED")
                .timestamp(LocalDateTime.now())
                .shipmentId(999L)
                .orderId(createdOrder.getId())
                .trackingNumber("NX-778899-BR")
                .carrier("Nexus Express")
                .status("CREATED")
                .estimatedDeliveryDays(3)
                .freightCost(new BigDecimal("20.00"))
                .customerEmail("rastreio@infnet.com")
                .build();

        shipmentEventListener.handleShipmentCreated(shipmentEvent);

        // Verifica se o pedido foi atualizado com o código de rastreio definitivo
        Order updatedOrder = orderRepository.findById(createdOrder.getId()).orElseThrow();
        assertThat(updatedOrder.getTrackingNumber()).isEqualTo("NX-778899-BR");

        // Verifica se foi gerado o log de auditoria correspondente
        List<AuditLog> logs = auditLogRepository.findByEntityNameOrderByTimestampDesc("Order");
        boolean hasShippingAudit = logs.stream()
                .anyMatch(l -> "SHIPPING_PROVISIONED_VIA_RABBITMQ".equals(l.getAction()));
        assertThat(hasShippingAudit).isTrue();
    }

    @Test
    @DisplayName("Deve despachar pedido e publicar OrderDispatchedEvent no RabbitMQ")
    void shouldShipOrderAndPublishOrderDispatchedEvent() {
        OrderRequest request = OrderRequest.builder()
                .customerEmail("despacho@infnet.com")
                .street("Rua do Ouvidor, 10")
                .city("Rio de Janeiro")
                .state("RJ")
                .zipCode("20040-030")
                .carrier("LogBrasil Rodoviário")
                .shippingCost(new BigDecimal("15.00"))
                .estimatedDeliveryDays(3)
                .items(List.of(OrderItemRequest.builder()
                        .productId(product.getId())
                        .quantity(1)
                        .build()))
                .build();

        Order created = orderService.createOrder(request);
        created.setTrackingNumber("NX-112233-BR");
        orderRepository.save(created);

        Order shipped = orderService.shipOrder(created.getId());

        assertThat(shipped.getStatus()).isEqualTo(OrderStatus.SHIPPED);

        verify(rabbitTemplate, times(1)).convertAndSend(
                eq(RabbitMqConfig.EXCHANGE_ORDER),
                eq(RabbitMqConfig.ROUTING_KEY_ORDER_DISPATCHED),
                any(OrderDispatchedEvent.class),
                any(MessagePostProcessor.class)
        );
    }

    @Test
    @DisplayName("Deve cancelar pedido, estornar estoque e publicar OrderCancelledEvent no RabbitMQ (Compensação)")
    void shouldCancelOrderAndPublishOrderCancelledEvent() {
        OrderRequest request = OrderRequest.builder()
                .customerEmail("cancelamento@infnet.com")
                .street("Praça XV, 20")
                .city("Rio de Janeiro")
                .state("RJ")
                .zipCode("20010-010")
                .items(List.of(OrderItemRequest.builder()
                        .productId(product.getId())
                        .quantity(3)
                        .build()))
                .build();

        Order created = orderService.createOrder(request);
        assertThat(productRepository.findById(product.getId()).orElseThrow().getStock()).isEqualTo(7);

        Order cancelled = orderService.cancelOrder(created.getId());

        assertThat(cancelled.getStatus()).isEqualTo(OrderStatus.CANCELLED);
        // Estoque restaurado
        assertThat(productRepository.findById(product.getId()).orElseThrow().getStock()).isEqualTo(10);

        verify(rabbitTemplate, times(1)).convertAndSend(
                eq(RabbitMqConfig.EXCHANGE_ORDER),
                eq(RabbitMqConfig.ROUTING_KEY_ORDER_CANCELLED),
                any(OrderCancelledEvent.class),
                any(MessagePostProcessor.class)
        );
    }
}

