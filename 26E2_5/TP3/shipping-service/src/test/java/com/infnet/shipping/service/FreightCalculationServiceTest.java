package com.infnet.shipping.service;

import com.infnet.shipping.dto.FreightQuoteRequest;
import com.infnet.shipping.dto.FreightQuoteResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class FreightCalculationServiceTest {

    private FreightCalculationService freightCalculationService;

    @BeforeEach
    void setUp() {
        freightCalculationService = new FreightCalculationService();
    }

    @Test
    @DisplayName("Deve calcular opções de frete para CEP de São Paulo (Dígito 0)")
    void shouldCalculateRatesForSaoPaulo() {
        FreightQuoteRequest request = FreightQuoteRequest.builder()
                .zipCode("01310-100")
                .totalItems(1)
                .orderTotal(new BigDecimal("150.00"))
                .build();

        FreightQuoteResponse response = freightCalculationService.calculateRates(request);

        assertThat(response).isNotNull();
        assertThat(response.getDestinationRegion()).contains("São Paulo");
        assertThat(response.getOptions()).hasSize(3);

        var express = response.getOptions().stream().filter(o -> "EXPRESS".equals(o.getServiceType())).findFirst().orElseThrow();
        var standard = response.getOptions().stream().filter(o -> "STANDARD".equals(o.getServiceType())).findFirst().orElseThrow();
        var eco = response.getOptions().stream().filter(o -> "ECONOMICAL".equals(o.getServiceType())).findFirst().orElseThrow();

        assertThat(express.getPrice()).isGreaterThan(standard.getPrice());
        assertThat(express.getEstimatedDays()).isLessThanOrEqualTo(standard.getEstimatedDays());
        assertThat(eco.getPrice()).isLessThan(standard.getPrice());
    }

    @Test
    @DisplayName("Deve conceder frete grátis na opção Econômica para compras acima de R$ 350")
    void shouldGiveFreeShippingOnEcoForHighValueOrders() {
        FreightQuoteRequest request = FreightQuoteRequest.builder()
                .zipCode("20040-003")
                .totalItems(2)
                .orderTotal(new BigDecimal("500.00"))
                .build();

        FreightQuoteResponse response = freightCalculationService.calculateRates(request);

        var eco = response.getOptions().stream().filter(o -> "ECONOMICAL".equals(o.getServiceType())).findFirst().orElseThrow();
        assertThat(eco.getPrice()).isEqualByComparingTo(BigDecimal.ZERO);
    }

    @Test
    @DisplayName("Deve lançar exceção para CEP inválido ou curto")
    void shouldThrowExceptionForInvalidCep() {
        FreightQuoteRequest request = FreightQuoteRequest.builder()
                .zipCode("123")
                .build();

        assertThatThrownBy(() -> freightCalculationService.calculateRates(request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("CEP inválido");
    }
}
