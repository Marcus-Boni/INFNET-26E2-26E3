package com.infnet.tp5.event;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ShipmentCreatedEvent implements Serializable {
    private String eventId;
    private String correlationId;
    @Builder.Default
    private String eventType = "SHIPMENT_CREATED";
    private LocalDateTime timestamp;

    private Long shipmentId;
    private Long orderId;
    private String trackingNumber;
    private String carrier;
    private String status;
    private Integer estimatedDeliveryDays;
    private BigDecimal freightCost;
    private String customerEmail;
}

