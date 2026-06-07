package com.infnet.tp1.domain.model;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "orders")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Order {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String customerEmail;

    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    @JoinColumn(name = "order_id")
    @Builder.Default
    private List<OrderItem> items = new ArrayList<>();

    @Embedded
    private Address shippingAddress;

    @Enumerated(EnumType.STRING)
    private OrderStatus status;

    private BigDecimal totalPrice;

    private LocalDateTime createdAt;

    public void initialize() {
        this.status = OrderStatus.PENDING;
        this.createdAt = LocalDateTime.now();
        this.totalPrice = BigDecimal.ZERO;
        if (this.items == null) {
            this.items = new ArrayList<>();
        }
    }

    public void addItem(Product product, int quantity) {
        if (this.status != null && this.status != OrderStatus.PENDING) {
            throw new IllegalStateException("Não é possível adicionar itens a um pedido que não está PENDENTE.");
        }
        product.decreaseStock(quantity);
        OrderItem item = OrderItem.builder()
                .productId(product.getId())
                .productName(product.getName())
                .quantity(quantity)
                .unitPrice(product.getPrice())
                .build();
        this.items.add(item);
        recalculateTotalPrice();
    }

    public void recalculateTotalPrice() {
        this.totalPrice = this.items.stream()
                .map(OrderItem::getTotalPrice)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    public void ship() {
        if (this.status != OrderStatus.PENDING) {
            throw new IllegalStateException("Apenas pedidos PENDENTES podem ser enviados. Status atual: " + this.status);
        }
        this.status = OrderStatus.SHIPPED;
    }

    public void cancel() {
        if (this.status != OrderStatus.PENDING) {
            throw new IllegalStateException("Apenas pedidos PENDENTES podem ser cancelados. Status atual: " + this.status);
        }
        this.status = OrderStatus.CANCELLED;
    }
}
