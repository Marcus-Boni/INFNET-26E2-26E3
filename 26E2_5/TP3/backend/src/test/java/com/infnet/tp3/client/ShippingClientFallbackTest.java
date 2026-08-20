package com.infnet.tp3.client;

import com.infnet.tp3.client.dto.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

class ShippingClientFallbackTest {

    private ShippingClientFallback fallback;

    @BeforeEach
    void setUp() {
        fallback = new ShippingClientFallback();
    }

    @Test
    @DisplayName("Fallback deve fornecer cotação de contingência quando o microsserviço de frete estiver indisponível")
    void shouldReturnContingencyQuoteOnFallback() {
        ShippingCalculationRequest request = ShippingCalculationRequest.builder()
                .zipCode("20040-003")
                .totalItems(2)
                .orderTotal(new BigDecimal("150.00"))
                .build();

        ShippingCalculationResponse response = fallback.calculateRates(request);

        assertThat(response).isNotNull();
        assertThat(response.getDestinationRegion()).contains("Offline Mode");
        assertThat(response.getOptions()).hasSize(3);
    }

    @Test
    @DisplayName("Fallback deve gerar tracking offline contingencial para não interromper criação de pedidos")
    void shouldReturnOfflineTrackingOnShipmentCreationFallback() {
        CreateShipmentRequest request = CreateShipmentRequest.builder()
                .orderId(1234L)
                .customerEmail("cliente@offline.com")
                .carrier("Transportadora Contingência")
                .freightCost(new BigDecimal("20.00"))
                .street("Rua Principal, 10")
                .city("Rio de Janeiro")
                .state("RJ")
                .zipCode("20040-003")
                .build();

        ShipmentDetailsDto response = fallback.createShipment(request);

        assertThat(response).isNotNull();
        assertThat(response.getTrackingNumber()).isEqualTo("NX-OFFLINE-1234-BR");
        assertThat(response.getEvents()).isNotEmpty();
        assertThat(response.getEvents().get(0).getMessage()).contains("contingência");
    }
}
