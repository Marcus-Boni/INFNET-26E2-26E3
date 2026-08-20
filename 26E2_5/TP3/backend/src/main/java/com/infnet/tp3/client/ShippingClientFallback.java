package com.infnet.tp3.client;

import com.infnet.tp3.client.dto.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Component
public class ShippingClientFallback implements ShippingClient {

    private static final Logger log = LoggerFactory.getLogger(ShippingClientFallback.class);

    @Override
    public ShippingCalculationResponse calculateRates(ShippingCalculationRequest request) {
        log.warn("FALLBACK ATIVADO: Falha na comunicação com o microsserviço de frete. Fornecendo cotação padrão de contingência.");

        List<ShippingOptionDto> fallbackOptions = new ArrayList<>();
        fallbackOptions.add(ShippingOptionDto.builder()
                .serviceType("STANDARD")
                .carrierName("LogBrasil Rodoviário (Contingência)")
                .description("Cotação padrão calculada pelo sistema de contingência.")
                .price(new BigDecimal("20.00"))
                .estimatedDays(5)
                .build());

        fallbackOptions.add(ShippingOptionDto.builder()
                .serviceType("EXPRESS")
                .carrierName("Nexus Express (Contingência)")
                .description("Cotação expressa de contingência.")
                .price(new BigDecimal("35.00"))
                .estimatedDays(2)
                .build());

        fallbackOptions.add(ShippingOptionDto.builder()
                .serviceType("ECONOMICAL")
                .carrierName("Eco Cargo (Contingência)")
                .description("Cotação econômica de contingência.")
                .price(new BigDecimal("15.00"))
                .estimatedDays(7)
                .build());

        return ShippingCalculationResponse.builder()
                .destinationZipCode(request.getZipCode())
                .destinationRegion("Região Padrão (Offline Mode)")
                .options(fallbackOptions)
                .build();
    }

    @Override
    public ShipmentDetailsDto createShipment(CreateShipmentRequest request) {
        log.warn("FALLBACK ATIVADO: Não foi possível registrar envio no microsserviço de frete para o pedido #{}. Gerando código de contingência offline.", request.getOrderId());

        String offlineTracking = "NX-OFFLINE-" + request.getOrderId() + "-BR";

        List<TrackingEventDto> events = new ArrayList<>();
        events.add(TrackingEventDto.builder()
                .id(0L)
                .status("CREATED")
                .statusDisplay("Criado (Offline)")
                .message("Envio gerado em modo de contingência. Aguardando sincronização com a central de logística.")
                .location("Central Nexus Store (Modo Offline)")
                .timestamp(LocalDateTime.now())
                .build());

        return ShipmentDetailsDto.builder()
                .id(0L)
                .orderId(request.getOrderId())
                .customerEmail(request.getCustomerEmail())
                .trackingNumber(offlineTracking)
                .carrier(request.getCarrier() != null ? request.getCarrier() : "Transportadora Padrão")
                .serviceType(request.getServiceType() != null ? request.getServiceType() : "STANDARD")
                .freightCost(request.getFreightCost() != null ? request.getFreightCost() : new BigDecimal("20.00"))
                .estimatedDeliveryDays(request.getEstimatedDeliveryDays() != null ? request.getEstimatedDeliveryDays() : 5)
                .destinationStreet(request.getStreet())
                .destinationCity(request.getCity())
                .destinationState(request.getState())
                .destinationZipCode(request.getZipCode())
                .status("CREATED")
                .statusDisplay("Criado (Aguardando Sincronização)")
                .createdAt(LocalDateTime.now())
                .lastModifiedAt(LocalDateTime.now())
                .events(events)
                .build();
    }

    @Override
    public ShipmentDetailsDto getShipmentByTrackingNumber(String trackingNumber) {
        log.warn("FALLBACK ATIVADO: Não foi possível obter detalhes do rastreio {} do microsserviço.", trackingNumber);

        List<TrackingEventDto> events = new ArrayList<>();
        events.add(TrackingEventDto.builder()
                .id(0L)
                .status("IN_TRANSIT")
                .statusDisplay("Em Processamento")
                .message("Informações de rastreamento temporariamente indisponíveis na transportadora parceira.")
                .location("Servidor de Logística Offline")
                .timestamp(LocalDateTime.now())
                .build());

        return ShipmentDetailsDto.builder()
                .trackingNumber(trackingNumber)
                .status("IN_TRANSIT")
                .statusDisplay("Em Processamento")
                .events(events)
                .build();
    }

    @Override
    public ShipmentDetailsDto getShipmentByOrderId(Long orderId) {
        log.warn("FALLBACK ATIVADO: Não foi possível buscar envio para o pedido #{} no microsserviço.", orderId);

        List<TrackingEventDto> events = new ArrayList<>();
        events.add(TrackingEventDto.builder()
                .id(0L)
                .status("CREATED")
                .statusDisplay("Registrado")
                .message("Pedido registrado. Rastreamento detalhado em processamento.")
                .location("CD Nexus Store")
                .timestamp(LocalDateTime.now())
                .build());

        return ShipmentDetailsDto.builder()
                .orderId(orderId)
                .trackingNumber("NX-ORDER-" + orderId + "-BR")
                .carrier("Transportadora Parceira")
                .status("CREATED")
                .statusDisplay("Registrado")
                .events(events)
                .build();
    }

    @Override
    public ShipmentDetailsDto updateShipmentStatus(String trackingNumber, UpdateShipmentStatusRequest request) {
        log.warn("FALLBACK ATIVADO: Falha ao sincronizar atualização de status do envio {} com o microsserviço.", trackingNumber);
        return ShipmentDetailsDto.builder()
                .trackingNumber(trackingNumber)
                .status(request.getStatus())
                .statusDisplay(request.getStatus())
                .build();
    }
}
