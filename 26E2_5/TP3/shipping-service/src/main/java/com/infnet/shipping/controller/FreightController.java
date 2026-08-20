package com.infnet.shipping.controller;

import com.infnet.shipping.dto.FreightQuoteRequest;
import com.infnet.shipping.dto.FreightQuoteResponse;
import com.infnet.shipping.service.FreightCalculationService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/shipping")
public class FreightController {

    private final FreightCalculationService freightCalculationService;

    @Autowired
    public FreightController(FreightCalculationService freightCalculationService) {
        this.freightCalculationService = freightCalculationService;
    }

    @PostMapping("/calculate")
    public ResponseEntity<FreightQuoteResponse> calculateRates(@Valid @RequestBody FreightQuoteRequest request) {
        FreightQuoteResponse response = freightCalculationService.calculateRates(request);
        return ResponseEntity.ok(response);
    }
}
