package com.infnet.tp4.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.infnet.tp4.event.*;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class RabbitMqEventSerializationTest {

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @DisplayName("Deve serializar e deserializar OrderCreatedEvent em JSON compatível com RabbitMQ")
    void shouldSerializeAndDeserializeOrderCreatedEvent() throws Exception {
        OrderCreatedEvent event = OrderCreatedEvent.builder()
                .eventId(UUID.randomUUID().toString())
                .correlationId("corr-test-456")
                .eventType("ORDER_CREATED")
                .timestamp(LocalDateTime.now())
                .orderId(123L)
                .customerEmail("teste@loja.com")
                .carrier("Nexus Express")
                .serviceType("EXPRESS")
                .shippingCost(new BigDecimal("25.00"))
                .estimatedDeliveryDays(2)
                .itemsTotal(new BigDecimal("100.00"))
                .totalPrice(new BigDecimal("125.00"))
                .street("Av. Paulista, 1000")
                .city("São Paulo")
                .state("SP")
                .zipCode("01310-100")
                .items(List.of(
                        OrderItemEventDto.builder()
                                .productId(1L)
                                .productName("Item A")
                                .quantity(2)
                                .unitPrice(new BigDecimal("50.00"))
                                .build()
                ))
                .build();

        String json = objectMapper.writeValueAsString(event);
        assertThat(json).contains("ORDER_CREATED");
        assertThat(json).contains("corr-test-456");

        OrderCreatedEvent deserialized = objectMapper.readValue(json, OrderCreatedEvent.class);
        assertThat(deserialized.getOrderId()).isEqualTo(123L);
        assertThat(deserialized.getCustomerEmail()).isEqualTo("teste@loja.com");
        assertThat(deserialized.getItems()).hasSize(1);
    }

    @Test
    @DisplayName("Deve serializar e deserializar ShipmentCreatedEvent")
    void shouldSerializeAndDeserializeShipmentCreatedEvent() throws Exception {
        ShipmentCreatedEvent event = ShipmentCreatedEvent.builder()
                .eventId(UUID.randomUUID().toString())
                .correlationId("corr-ship-1")
                .eventType("SHIPMENT_CREATED")
                .timestamp(LocalDateTime.now())
                .shipmentId(55L)
                .orderId(123L)
                .trackingNumber("NX-123456-BR")
                .carrier("Nexus Express")
                .status("CREATED")
                .estimatedDeliveryDays(3)
                .freightCost(new BigDecimal("20.00"))
                .customerEmail("teste@loja.com")
                .build();

        String json = objectMapper.writeValueAsString(event);
        ShipmentCreatedEvent deserialized = objectMapper.readValue(json, ShipmentCreatedEvent.class);

        assertThat(deserialized.getTrackingNumber()).isEqualTo("NX-123456-BR");
        assertThat(deserialized.getOrderId()).isEqualTo(123L);
    }
}
