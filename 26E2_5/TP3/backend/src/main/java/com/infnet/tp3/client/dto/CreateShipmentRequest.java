package com.infnet.tp3.client.dto;

import lombok.*;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreateShipmentRequest {
    private Long orderId;
    private String customerEmail;
    private String carrier;
    private String serviceType;
    private BigDecimal freightCost;
    private Integer estimatedDeliveryDays;
    private String street;
    private String city;
    private String state;
    private String zipCode;
}
