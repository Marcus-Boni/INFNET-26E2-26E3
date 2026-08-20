package com.infnet.tp3.client;

import com.infnet.tp3.client.dto.*;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

@FeignClient(
        name = "shipping-service",
        url = "${shipping.service.url:http://localhost:8082}",
        fallback = ShippingClientFallback.class
)
public interface ShippingClient {

    @PostMapping("/api/v1/shipping/calculate")
    ShippingCalculationResponse calculateRates(@RequestBody ShippingCalculationRequest request);

    @PostMapping("/api/v1/shipping/shipments")
    ShipmentDetailsDto createShipment(@RequestBody CreateShipmentRequest request);

    @GetMapping("/api/v1/shipping/shipments/{trackingNumber}")
    ShipmentDetailsDto getShipmentByTrackingNumber(@PathVariable("trackingNumber") String trackingNumber);

    @GetMapping("/api/v1/shipping/shipments/order/{orderId}")
    ShipmentDetailsDto getShipmentByOrderId(@PathVariable("orderId") Long orderId);

    @PatchMapping("/api/v1/shipping/shipments/{trackingNumber}/status")
    ShipmentDetailsDto updateShipmentStatus(
            @PathVariable("trackingNumber") String trackingNumber,
            @RequestBody UpdateShipmentStatusRequest request
    );
}
