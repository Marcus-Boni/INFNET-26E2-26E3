package com.infnet.shipping.dto;

import lombok.*;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FreightOptionDto {
    private String serviceType;
    private String carrierName;
    private String description;
    private BigDecimal price;
    private Integer estimatedDays;
}
