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
public class OrderCancelledEvent implements Serializable {
    private String eventId;
    private String correlationId;
    @Builder.Default
    private String eventType = "ORDER_CANCELLED";
    private LocalDateTime timestamp;

    private Long orderId;
    private String trackingNumber;
    private String reason;
}

