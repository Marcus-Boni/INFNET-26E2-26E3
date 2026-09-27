package com.infnet.shipping.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ShipmentCreateRequest {

    @NotNull(message = "O ID do pedido é obrigatório.")
    private Long orderId;

    @NotBlank(message = "O e-mail do cliente é obrigatório.")
    private String customerEmail;

    @NotBlank(message = "A transportadora é obrigatória.")
    private String carrier;

    private String serviceType;

    private BigDecimal freightCost;

    private Integer estimatedDeliveryDays;

    @NotBlank(message = "O logradouro é obrigatório.")
    private String street;

    @NotBlank(message = "A cidade é obrigatória.")
    private String city;

    @NotBlank(message = "O estado é obrigatório.")
    private String state;

    @NotBlank(message = "O CEP é obrigatório.")
    private String zipCode;
}
