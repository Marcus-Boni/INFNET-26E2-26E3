package com.infnet.tp5.event;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ShipmentStatusUpdatedEvent implements Serializable {
    private String eventId;
    private String correlationId;
    @Builder.Default
    private String eventType = "SHIPMENT_STATUS_UPDATED";
    private LocalDateTime timestamp;

    private Long shipmentId;
    private Long orderId;
    private String trackingNumber;
    private String previousStatus;
    private String newStatus;
    private String message;
    private String location;
}

