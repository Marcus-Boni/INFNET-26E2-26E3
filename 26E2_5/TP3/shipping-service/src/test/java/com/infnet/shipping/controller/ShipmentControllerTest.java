package com.infnet.shipping.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.infnet.shipping.dto.FreightQuoteRequest;
import com.infnet.shipping.dto.ShipmentCreateRequest;
import com.infnet.shipping.dto.ShipmentStatusUpdateRequest;
import com.infnet.shipping.domain.model.ShipmentStatus;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class ShipmentControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @DisplayName("POST /api/v1/shipping/calculate deve retornar cotação de frete com 3 opções")
    void shouldCalculateRates() throws Exception {
        FreightQuoteRequest request = FreightQuoteRequest.builder()
                .zipCode("20040-003")
                .totalItems(2)
                .orderTotal(new BigDecimal("180.00"))
                .build();

        mockMvc.perform(post("/api/v1/shipping/calculate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(org.springframework.test.web.servlet.result.MockMvcResultHandlers.print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.destinationZipCode").value("20040-003"))
                .andExpect(jsonPath("$.options.length()").value(3))
                .andExpect(jsonPath("$.options[0].carrierName").exists());
    }

    @Test
    @DisplayName("POST /api/v1/shipping/shipments deve criar um envio e gerar código de rastreio")
    void shouldCreateShipment() throws Exception {
        ShipmentCreateRequest request = ShipmentCreateRequest.builder()
                .orderId(888L)
                .customerEmail("cliente888@exemplo.com")
                .carrier("Nexus Express Air")
                .serviceType("EXPRESS")
                .freightCost(new BigDecimal("29.90"))
                .estimatedDeliveryDays(2)
                .street("Av. Rio Branco, 100")
                .city("Rio de Janeiro")
                .state("RJ")
                .zipCode("20040-003")
                .build();

        mockMvc.perform(post("/api/v1/shipping/shipments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.trackingNumber").exists())
                .andExpect(jsonPath("$.orderId").value(888))
                .andExpect(jsonPath("$.status").value("CREATED"))
                .andExpect(jsonPath("$.events.length()").value(1));
    }

    @Test
    @DisplayName("PATCH /api/v1/shipping/shipments/{trackingNumber}/status deve atualizar status e adicionar evento")
    void shouldUpdateShipmentStatus() throws Exception {
        // First create shipment
        ShipmentCreateRequest createReq = ShipmentCreateRequest.builder()
                .orderId(777L)
                .customerEmail("cliente777@exemplo.com")
                .carrier("LogBrasil Rodoviário")
                .serviceType("STANDARD")
                .freightCost(new BigDecimal("18.50"))
                .estimatedDeliveryDays(3)
                .street("Rua do Ouvidor, 50")
                .city("Rio de Janeiro")
                .state("RJ")
                .zipCode("20040-000")
                .build();

        String responseJson = mockMvc.perform(post("/api/v1/shipping/shipments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createReq)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();

        String trackingNumber = objectMapper.readTree(responseJson).get("trackingNumber").asText();

        // Now update status
        ShipmentStatusUpdateRequest updateReq = ShipmentStatusUpdateRequest.builder()
                .status(ShipmentStatus.DISPATCHED)
                .message("Pacote encaminhado para o centro de triagem.")
                .location("Hub Logístico Galeão / RJ")
                .build();

        mockMvc.perform(patch("/api/v1/shipping/shipments/" + trackingNumber + "/status")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateReq)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("DISPATCHED"))
                .andExpect(jsonPath("$.events.length()").value(2));
    }
}
