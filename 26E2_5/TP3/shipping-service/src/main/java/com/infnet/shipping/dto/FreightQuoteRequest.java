package com.infnet.shipping.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.*;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FreightQuoteRequest {

    @NotBlank(message = "O CEP de destino é obrigatório.")
    private String zipCode;

    private Integer totalItems;

    private BigDecimal orderTotal;
}
