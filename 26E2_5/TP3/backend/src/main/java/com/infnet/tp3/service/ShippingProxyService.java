package com.infnet.tp3.service;

import com.infnet.tp3.client.ShippingClient;
import com.infnet.tp3.client.dto.ShippingCalculationRequest;
import com.infnet.tp3.client.dto.ShippingCalculationResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class ShippingProxyService {

    private final ShippingClient shippingClient;

    @Autowired
    public ShippingProxyService(ShippingClient shippingClient) {
        this.shippingClient = shippingClient;
    }

    public ShippingCalculationResponse calculateShipping(ShippingCalculationRequest request) {
        return shippingClient.calculateRates(request);
    }
}
