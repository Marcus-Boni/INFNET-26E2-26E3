package com.techmarket.orderservice.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateOrderRequest {

    // Opcional: caso não seja fornecido, o microsserviço utilizará o usuário e nome extraídos do Token JWT
    private String customerName;

    // Formato de item único simplificado (compatível com TP2)
    private String productId;
    private String productName;
    private Integer quantity;
    private BigDecimal unitPrice;

    // Formato de lista de múltiplos itens
    private List<OrderItemDto> items;
}
