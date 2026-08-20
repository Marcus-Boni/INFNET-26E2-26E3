package com.infnet.tp3.controller.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProductRequest {

    @NotBlank(message = "O nome do produto é obrigatório.")
    private String name;

    private String description;

    @NotNull(message = "O preço é obrigatório.")
    @DecimalMin(value = "0.01", message = "O preço deve ser maior que zero.")
    private BigDecimal price;

    @NotNull(message = "O estoque é obrigatório.")
    @Min(value = 0, message = "O estoque não pode ser negativo.")
    private Integer stock;
}
