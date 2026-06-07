package com.infnet.tp1.domain.model;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;

@Entity
@Table(name = "products")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Product {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;
    private String description;
    private BigDecimal price;
    private Integer stock;

    public void decreaseStock(int quantity) {
        if (quantity <= 0) {
            throw new IllegalArgumentException("A quantidade a ser retirada deve ser positiva.");
        }
        if (this.stock < quantity) {
            throw new IllegalStateException("Estoque insuficiente para o produto: " + this.name + 
                ". Solicitado: " + quantity + ", Disponível: " + this.stock);
        }
        this.stock -= quantity;
    }

    public void increaseStock(int quantity) {
        if (quantity <= 0) {
            throw new IllegalArgumentException("A quantidade a ser adicionada deve ser positiva.");
        }
        this.stock += quantity;
    }
}
