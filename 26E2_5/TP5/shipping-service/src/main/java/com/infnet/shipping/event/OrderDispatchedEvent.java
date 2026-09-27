package com.infnet.shipping.event;

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
public class OrderDispatchedEvent implements Serializable {
    private String eventId;
    private String correlationId;
    @Builder.Default
    private String eventType = "ORDER_DISPATCHED";
    private LocalDateTime timestamp;

    private Long orderId;
    private String trackingNumber;
    private String status;
    private String message;
    private String location;
}
