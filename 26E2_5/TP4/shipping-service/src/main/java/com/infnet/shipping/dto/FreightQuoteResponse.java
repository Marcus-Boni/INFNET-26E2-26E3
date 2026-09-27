package com.infnet.shipping.dto;

import lombok.*;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FreightQuoteResponse {
    private String destinationZipCode;
    private String destinationRegion;
    private List<FreightOptionDto> options;
}
