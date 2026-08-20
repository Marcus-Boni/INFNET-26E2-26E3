package com.infnet.shipping.controller;

import com.infnet.shipping.domain.model.ShipmentStatus;
import com.infnet.shipping.dto.ShipmentCreateRequest;
import com.infnet.shipping.dto.ShipmentResponse;
import com.infnet.shipping.dto.ShipmentStatusUpdateRequest;
import com.infnet.shipping.service.ShipmentService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/shipping/shipments")
public class ShipmentController {

    private final ShipmentService shipmentService;

    @Autowired
    public ShipmentController(ShipmentService shipmentService) {
        this.shipmentService = shipmentService;
    }

    @PostMapping
    public ResponseEntity<ShipmentResponse> createShipment(@Valid @RequestBody ShipmentCreateRequest request) {
        ShipmentResponse created = shipmentService.createShipment(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @GetMapping("/{trackingNumber}")
    public ResponseEntity<ShipmentResponse> getByTrackingNumber(@PathVariable String trackingNumber) {
        ShipmentResponse response = shipmentService.getShipmentByTrackingNumber(trackingNumber);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/order/{orderId}")
    public ResponseEntity<ShipmentResponse> getByOrderId(@PathVariable Long orderId) {
        ShipmentResponse response = shipmentService.getShipmentByOrderId(orderId);
        return ResponseEntity.ok(response);
    }

    @PatchMapping("/{trackingNumber}/status")
    public ResponseEntity<ShipmentResponse> updateStatus(
            @PathVariable String trackingNumber,
            @Valid @RequestBody ShipmentStatusUpdateRequest request) {
        ShipmentResponse response = shipmentService.updateShipmentStatus(trackingNumber, request);
        return ResponseEntity.ok(response);
    }

    @GetMapping
    public ResponseEntity<List<ShipmentResponse>> getAllShipments(
            @RequestParam(required = false) ShipmentStatus status) {
        List<ShipmentResponse> list = (status != null)
                ? shipmentService.getShipmentsByStatus(status)
                : shipmentService.getAllShipments();
        return ResponseEntity.ok(list);
    }
}
