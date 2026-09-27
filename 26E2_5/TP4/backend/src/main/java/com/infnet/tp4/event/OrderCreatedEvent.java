package com.infnet.tp4.event;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderCreatedEvent implements Serializable {
    private String eventId;
    private String correlationId;
    @Builder.Default
    private String eventType = "ORDER_CREATED";
    private LocalDateTime timestamp;

    private Long orderId;
    private String customerEmail;
    private String carrier;
    private String serviceType;
    private BigDecimal shippingCost;
    private Integer estimatedDeliveryDays;
    private BigDecimal itemsTotal;
    private BigDecimal totalPrice;

    private String street;
    private String city;
    private String state;
    private String zipCode;

    private List<OrderItemEventDto> items;
}
