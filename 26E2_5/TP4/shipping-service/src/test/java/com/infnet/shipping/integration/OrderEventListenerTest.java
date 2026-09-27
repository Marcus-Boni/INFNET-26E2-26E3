package com.infnet.shipping.integration;

import com.infnet.shipping.domain.model.Shipment;
import com.infnet.shipping.domain.model.ShipmentStatus;
import com.infnet.shipping.domain.repository.EventLogRepository;
import com.infnet.shipping.domain.repository.ShipmentRepository;
import com.infnet.shipping.domain.repository.TrackingEventRepository;
import com.infnet.shipping.event.*;
import com.infnet.shipping.infrastructure.config.RabbitMqConfig;
import com.infnet.shipping.infrastructure.rabbitmq.OrderEventListener;
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
class OrderEventListenerTest {

    @Autowired
    private OrderEventListener orderEventListener;

    @Autowired
    private ShipmentRepository shipmentRepository;

    @Autowired
    private TrackingEventRepository trackingEventRepository;

    @Autowired
    private EventLogRepository eventLogRepository;

    @MockBean
    private RabbitTemplate rabbitTemplate;

    @BeforeEach
    void setUp() {
        trackingEventRepository.deleteAll();
        shipmentRepository.deleteAll();
        eventLogRepository.deleteAll();
    }

    @Test
    @DisplayName("Deve consumir OrderCreatedEvent, emitir etiqueta com rastreio e publicar ShipmentCreatedEvent no RabbitMQ")
    void shouldConsumeOrderCreatedEventAndPublishShipmentCreatedEvent() {
        OrderCreatedEvent event = OrderCreatedEvent.builder()
                .eventId(UUID.randomUUID().toString())
                .correlationId("corr-eda-999")
                .eventType("ORDER_CREATED")
                .timestamp(LocalDateTime.now())
                .orderId(999L)
                .customerEmail("pedidos@infnet.com")
                .carrier("Nexus Express Air")
                .serviceType("EXPRESS")
                .shippingCost(new BigDecimal("35.00"))
                .estimatedDeliveryDays(2)
                .itemsTotal(new BigDecimal("200.00"))
                .totalPrice(new BigDecimal("235.00"))
                .street("Av. Atlântica, 1500")
                .city("Rio de Janeiro")
                .state("RJ")
                .zipCode("22021-000")
                .items(List.of(
                        OrderItemEventDto.builder()
                                .productId(10L)
                                .productName("Headset Pro")
                                .quantity(1)
                                .unitPrice(new BigDecimal("200.00"))
                                .build()
                ))
                .build();

        orderEventListener.handleOrderCreated(event);

        // Verifica que o envio foi gerado no banco de dados isolado shippingdb
        Shipment shipment = shipmentRepository.findByOrderId(999L).orElseThrow();
        assertThat(shipment.getTrackingNumber()).startsWith("NX-").endsWith("-BR");
        assertThat(shipment.getCustomerEmail()).isEqualTo("pedidos@infnet.com");
        assertThat(shipment.getStatus()).isEqualTo(ShipmentStatus.CREATED);

        // Verifica que o evento de resposta ShipmentCreatedEvent foi publicado no RabbitMQ
        verify(rabbitTemplate, times(1)).convertAndSend(
                eq(RabbitMqConfig.EXCHANGE_SHIPPING),
                eq(RabbitMqConfig.ROUTING_KEY_SHIPPING_CREATED),
                any(ShipmentCreatedEvent.class),
                any(MessagePostProcessor.class)
        );

        // Verifica se o log de eventos registrou a recepção
        assertThat(eventLogRepository.findAll()).isNotEmpty();
    }

    @Test
    @DisplayName("Deve consumir OrderDispatchedEvent e atualizar status do envio para DISPATCHED")
    void shouldConsumeOrderDispatchedEventAndUpdateStatus() {
        OrderCreatedEvent createdEvent = OrderCreatedEvent.builder()
                .eventId(UUID.randomUUID().toString())
                .correlationId("corr-disp-1")
                .eventType("ORDER_CREATED")
                .timestamp(LocalDateTime.now())
                .orderId(1001L)
                .customerEmail("disp@infnet.com")
                .carrier("LogBrasil")
                .shippingCost(new BigDecimal("15.00"))
                .street("Rua 1")
                .city("Rio de Janeiro")
                .state("RJ")
                .zipCode("20000-000")
                .build();

        orderEventListener.handleOrderCreated(createdEvent);
        Shipment shipment = shipmentRepository.findByOrderId(1001L).orElseThrow();

        OrderDispatchedEvent dispatchedEvent = OrderDispatchedEvent.builder()
                .eventId(UUID.randomUUID().toString())
                .correlationId("corr-disp-1")
                .eventType("ORDER_DISPATCHED")
                .timestamp(LocalDateTime.now())
                .orderId(1001L)
                .trackingNumber(shipment.getTrackingNumber())
                .status("DISPATCHED")
                .message("Pacote coletado pela transportadora")
                .location("CD Nexus Store")
                .build();

        orderEventListener.handleOrderDispatched(dispatchedEvent);

        Shipment updated = shipmentRepository.findByOrderId(1001L).orElseThrow();
        assertThat(updated.getStatus()).isEqualTo(ShipmentStatus.DISPATCHED);
    }

    @Test
    @DisplayName("Deve consumir OrderCancelledEvent e atualizar status do envio para CANCELLED")
    void shouldConsumeOrderCancelledEventAndUpdateStatus() {
        OrderCreatedEvent createdEvent = OrderCreatedEvent.builder()
                .eventId(UUID.randomUUID().toString())
                .correlationId("corr-cancel-1")
                .eventType("ORDER_CREATED")
                .timestamp(LocalDateTime.now())
                .orderId(1002L)
                .customerEmail("cancel@infnet.com")
                .carrier("LogBrasil")
                .shippingCost(new BigDecimal("15.00"))
                .street("Rua 2")
                .city("Rio de Janeiro")
                .state("RJ")
                .zipCode("20000-000")
                .build();

        orderEventListener.handleOrderCreated(createdEvent);
        Shipment shipment = shipmentRepository.findByOrderId(1002L).orElseThrow();

        OrderCancelledEvent cancelledEvent = OrderCancelledEvent.builder()
                .eventId(UUID.randomUUID().toString())
                .correlationId("corr-cancel-1")
                .eventType("ORDER_CANCELLED")
                .timestamp(LocalDateTime.now())
                .orderId(1002L)
                .trackingNumber(shipment.getTrackingNumber())
                .reason("Cliente desistiu da compra")
                .build();

        orderEventListener.handleOrderCancelled(cancelledEvent);

        Shipment updated = shipmentRepository.findByOrderId(1002L).orElseThrow();
        assertThat(updated.getStatus()).isEqualTo(ShipmentStatus.CANCELLED);
    }
}
