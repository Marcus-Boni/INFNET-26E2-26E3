package com.infnet.tp3.domain.model;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "orders")
@EntityListeners(AuditingEntityListener.class)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Order {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String customerEmail;

    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id")
    @Builder.Default
    private List<OrderItem> items = new ArrayList<>();

    @Embedded
    private Address shippingAddress;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private OrderStatus status;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal itemsTotal;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal shippingCost;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal totalPrice;

    // Distributed Integration Fields (Microsserviço de Logística)
    @Column(length = 50)
    private String trackingNumber;

    @Column(length = 100)
    private String shippingCarrier;

    private Integer estimatedDeliveryDays;

    @Version
    private Long version;

    @CreatedDate
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    private LocalDateTime lastModifiedAt;

    public void initialize() {
        this.status = OrderStatus.PENDING;
        if (this.itemsTotal == null) {
            this.itemsTotal = BigDecimal.ZERO;
        }
        if (this.shippingCost == null) {
            this.shippingCost = BigDecimal.ZERO;
        }
        if (this.totalPrice == null) {
            this.totalPrice = BigDecimal.ZERO;
        }
        if (this.createdAt == null) {
            this.createdAt = LocalDateTime.now();
        }
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
        recalculateTotals();
    }

    public void setShipping(String carrier, BigDecimal cost, Integer days, String trackingCode) {
        this.shippingCarrier = carrier;
        this.shippingCost = (cost != null) ? cost : BigDecimal.ZERO;
        this.estimatedDeliveryDays = days;
        this.trackingNumber = trackingCode;
        recalculateTotals();
    }

    public void recalculateTotals() {
        this.itemsTotal = this.items.stream()
                .map(OrderItem::getTotalPrice)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal freight = (this.shippingCost != null) ? this.shippingCost : BigDecimal.ZERO;
        this.totalPrice = this.itemsTotal.add(freight);
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
