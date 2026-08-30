package com.techmarket.catalogservice.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Map;

@Document(collection = "products")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Product {

    @Id
    private String id;

    private String name;

    private String description;

    private String category;

    private BigDecimal price;

    private Integer stockQuantity;

    private Boolean active;

    /**
     * Especificações técnicas dinâmicas em formato chave-valor flexível.
     * Justificativa NoSQL: cada categoria de produto de tecnologia possui um conjunto
     * totalmente heterogêneo de atributos (ex: GPUs têm VRAM e clock; Monitores têm taxa de atualização e painel;
     * Smartphones têm câmeras e 5G).
     */
    private Map<String, Object> specifications;

    private List<String> tags;

    private Instant createdAt;

    private Instant updatedAt;
}
