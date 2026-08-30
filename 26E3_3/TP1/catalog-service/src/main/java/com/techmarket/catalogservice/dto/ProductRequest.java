package com.techmarket.catalogservice.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductRequest {

    @NotBlank(message = "O nome do produto é obrigatório")
    private String name;

    private String description;

    @NotBlank(message = "A categoria é obrigatória")
    private String category;

    @NotNull(message = "O preço é obrigatório")
    @DecimalMin(value = "0.01", message = "O preço deve ser maior que zero")
    private BigDecimal price;

    @NotNull(message = "A quantidade em estoque é obrigatória")
    @Min(value = 0, message = "O estoque não pode ser negativo")
    private Integer stockQuantity;

    private Map<String, Object> specifications;

    private List<String> tags;
}
