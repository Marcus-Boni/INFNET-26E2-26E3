package com.techmarket.orderservice.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderRequest {

    @NotBlank(message = "O ID do cliente é obrigatório")
    private String customerId;

    @NotBlank(message = "O e-mail do cliente é obrigatório")
    @Email(message = "Formato de e-mail inválido")
    private String customerEmail;

    @NotEmpty(message = "O pedido deve conter pelo menos um item")
    @Valid
    private List<OrderItemRequest> items;
}
