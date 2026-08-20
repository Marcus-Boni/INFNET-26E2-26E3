package com.infnet.tp3.client.dto;

import lombok.*;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ShippingCalculationResponse {
    private String destinationZipCode;
    private String destinationRegion;
    private List<ShippingOptionDto> options;
}
