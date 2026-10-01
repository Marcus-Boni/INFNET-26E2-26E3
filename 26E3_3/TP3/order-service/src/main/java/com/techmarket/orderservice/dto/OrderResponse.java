package com.techmarket.orderservice.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.techmarket.orderservice.domain.OrderItem;
import com.techmarket.orderservice.domain.OrderStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderResponse {

    private String id;
    private String customerUsername;
    private String customerFullName;
    private List<OrderItem> items;
    private BigDecimal totalAmount;
    private OrderStatus status;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime createdAt;

    private String authenticatedAs;
}
