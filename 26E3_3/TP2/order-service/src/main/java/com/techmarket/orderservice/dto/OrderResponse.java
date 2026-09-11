package com.techmarket.orderservice.dto;

import com.techmarket.orderservice.domain.OrderStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderResponse {
    private String id;
    private String customerName;
    private String productId;
    private String productName;
    private Integer quantity;
    private BigDecimal unitPrice;
    private BigDecimal totalPrice;
    private OrderStatus status;
    private LocalDateTime createdAt;
    private String validatedByPod;
    private String handledByInstance;
}
