package com.infnet.tp4.controller;

import com.infnet.tp4.client.dto.ShippingCalculationRequest;
import com.infnet.tp4.client.dto.ShippingCalculationResponse;
import com.infnet.tp4.service.ShippingProxyService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/shipping")
public class ShippingProxyController {

    private final ShippingProxyService shippingProxyService;

    @Autowired
    public ShippingProxyController(ShippingProxyService shippingProxyService) {
        this.shippingProxyService = shippingProxyService;
    }

    @PostMapping("/calculate")
    public ResponseEntity<ShippingCalculationResponse> calculateShipping(@RequestBody ShippingCalculationRequest request) {
        ShippingCalculationResponse response = shippingProxyService.calculateShipping(request);
        return ResponseEntity.ok(response);
    }
}

