package com.infnet.shipping.dto;

import com.infnet.shipping.domain.model.ShipmentStatus;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ShipmentResponse {
    private Long id;
    private Long orderId;
    private String customerEmail;
    private String trackingNumber;
    private String carrier;
    private String serviceType;
    private BigDecimal freightCost;
    private Integer estimatedDeliveryDays;
    private String destinationStreet;
    private String destinationCity;
    private String destinationState;
    private String destinationZipCode;
    private ShipmentStatus status;
    private String statusDisplay;
    private LocalDateTime createdAt;
    private LocalDateTime lastModifiedAt;
    private List<TrackingEventDto> events;
}
