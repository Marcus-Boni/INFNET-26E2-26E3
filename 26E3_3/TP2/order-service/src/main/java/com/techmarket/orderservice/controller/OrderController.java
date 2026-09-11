package com.techmarket.orderservice.controller;

import com.techmarket.orderservice.client.ProductClient;
import com.techmarket.orderservice.dto.OrderRequest;
import com.techmarket.orderservice.dto.OrderResponse;
import com.techmarket.orderservice.service.OrderService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;
    private final ProductClient productClient;

    @PostMapping
    public ResponseEntity<OrderResponse> createOrder(@Valid @RequestBody OrderRequest request) {
        OrderResponse response = orderService.createOrder(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .header("X-Served-By", orderService.getHostIdentity())
                .body(response);
    }

    @GetMapping
    public ResponseEntity<List<OrderResponse>> getAllOrders() {
        return ResponseEntity.ok()
                .header("X-Served-By", orderService.getHostIdentity())
                .body(orderService.getAllOrders());
    }

    @GetMapping("/{id}")
    public ResponseEntity<OrderResponse> getOrderById(@PathVariable String id) {
        return ResponseEntity.ok()
                .header("X-Served-By", orderService.getHostIdentity())
                .body(orderService.getOrderById(id));
    }

    @GetMapping("/info")
    public ResponseEntity<Map<String, String>> getServiceInfo() {
        return ResponseEntity.ok(Map.of(
                "service", "order-service",
                "servedBy", orderService.getHostIdentity(),
                "productServiceTargetUrl", productClient.getProductServiceUrl(),
                "status", "UP"
        ));
    }
}
