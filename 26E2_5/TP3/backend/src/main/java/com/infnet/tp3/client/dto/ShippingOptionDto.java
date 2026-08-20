package com.infnet.tp3.client.dto;

import lombok.*;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ShippingOptionDto {
    private String serviceType;
    private String carrierName;
    private String description;
    private BigDecimal price;
    private Integer estimatedDays;
}
