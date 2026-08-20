package com.infnet.tp3.client.dto;

import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ShipmentDetailsDto {
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
    private String status;
    private String statusDisplay;
    private LocalDateTime createdAt;
    private LocalDateTime lastModifiedAt;
    @Builder.Default
    private List<TrackingEventDto> events = new ArrayList<>();
}
