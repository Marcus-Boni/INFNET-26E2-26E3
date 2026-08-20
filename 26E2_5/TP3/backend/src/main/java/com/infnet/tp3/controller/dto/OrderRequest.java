package com.infnet.tp3.controller.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import lombok.*;

import java.math.BigDecimal;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderRequest {

    @NotBlank(message = "O e-mail do cliente é obrigatório.")
    @Email(message = "E-mail inválido.")
    private String customerEmail;

    @NotBlank(message = "A rua é obrigatória.")
    private String street;

    @NotBlank(message = "A cidade é obrigatória.")
    private String city;

    @NotBlank(message = "O estado é obrigatório.")
    private String state;

    @NotBlank(message = "O CEP é obrigatório.")
    private String zipCode;

    // Campos integrados com o microsserviço de frete
    private String carrier;
    private String serviceType;
    private BigDecimal shippingCost;
    private Integer estimatedDeliveryDays;

    @NotEmpty(message = "O pedido deve conter pelo menos um item.")
    @Valid
    private List<OrderItemRequest> items;
}
