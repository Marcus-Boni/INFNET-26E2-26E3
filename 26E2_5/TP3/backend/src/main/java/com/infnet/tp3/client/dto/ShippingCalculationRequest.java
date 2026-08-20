package com.infnet.tp3.client.dto;

import lombok.*;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ShippingCalculationRequest {
    private String zipCode;
    private Integer totalItems;
    private BigDecimal orderTotal;
}
