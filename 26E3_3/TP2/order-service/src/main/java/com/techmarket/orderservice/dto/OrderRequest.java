package com.techmarket.orderservice.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderRequest {

    @NotBlank(message = "O nome do cliente é obrigatório")
    private String customerName;

    @NotBlank(message = "O identificador do produto (productId) é obrigatório")
    private String productId;

    @NotNull(message = "A quantidade é obrigatória")
    @Min(value = 1, message = "A quantidade deve ser de no mínimo 1 item")
    private Integer quantity;
}
