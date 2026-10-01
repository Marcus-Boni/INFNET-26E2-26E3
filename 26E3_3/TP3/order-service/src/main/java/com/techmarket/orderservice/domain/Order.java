package com.techmarket.orderservice.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Order {

    private String id;
    private String customerUsername;
    private String customerFullName;

    @Builder.Default
    private List<OrderItem> items = new ArrayList<>();

    private BigDecimal totalAmount;

    @Builder.Default
    private OrderStatus status = OrderStatus.CRIADO;

    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();
}
