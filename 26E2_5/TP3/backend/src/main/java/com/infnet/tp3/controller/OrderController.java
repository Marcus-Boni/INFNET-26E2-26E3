package com.infnet.tp3.controller;

import com.infnet.tp3.client.dto.ShipmentDetailsDto;
import com.infnet.tp3.controller.dto.OrderRequest;
import com.infnet.tp3.domain.model.AuditLog;
import com.infnet.tp3.domain.model.Order;
import com.infnet.tp3.domain.model.OrderStatus;
import com.infnet.tp3.service.OrderService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/orders")
public class OrderController {

    private final OrderService orderService;

    @Autowired
    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    @GetMapping
    public ResponseEntity<List<Order>> getAllOrders(@RequestParam(required = false) OrderStatus status) {
        if (status != null) {
            return ResponseEntity.ok(orderService.findByStatus(status));
        }
        return ResponseEntity.ok(orderService.findAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Order> getOrderById(@PathVariable Long id) {
        return orderService.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<Order> createOrder(@Valid @RequestBody OrderRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(orderService.createOrder(request));
    }

    @PatchMapping("/{id}/ship")
    public ResponseEntity<Order> shipOrder(@PathVariable Long id) {
        return ResponseEntity.ok(orderService.shipOrder(id));
    }

    @PatchMapping("/{id}/cancel")
    public ResponseEntity<Order> cancelOrder(@PathVariable Long id) {
        return ResponseEntity.ok(orderService.cancelOrder(id));
    }

    @GetMapping("/{id}/tracking")
    public ResponseEntity<ShipmentDetailsDto> getOrderTracking(@PathVariable Long id) {
        return ResponseEntity.ok(orderService.getOrderTracking(id));
    }

    @GetMapping("/{id}/history")
    public ResponseEntity<List<AuditLog>> getOrderHistory(@PathVariable Long id) {
        return ResponseEntity.ok(orderService.getOrderHistory(id));
    }
}
