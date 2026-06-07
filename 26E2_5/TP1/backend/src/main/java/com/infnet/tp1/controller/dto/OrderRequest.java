package com.infnet.tp1.controller.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import lombok.*;

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

    @NotEmpty(message = "O pedido deve conter pelo menos um item.")
    @Valid
    private List<OrderItemRequest> items;
}
