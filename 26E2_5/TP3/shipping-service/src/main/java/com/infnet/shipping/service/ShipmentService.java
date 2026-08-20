package com.infnet.shipping.service;

import com.infnet.shipping.domain.model.Shipment;
import com.infnet.shipping.domain.model.ShipmentStatus;
import com.infnet.shipping.domain.model.TrackingEvent;
import com.infnet.shipping.domain.repository.ShipmentRepository;
import com.infnet.shipping.domain.repository.TrackingEventRepository;
import com.infnet.shipping.dto.*;
import com.infnet.shipping.infrastructure.exception.ResourceNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

@Service
@Transactional
public class ShipmentService {

    private final ShipmentRepository shipmentRepository;
    private final TrackingEventRepository trackingEventRepository;
    private final Random random = new Random();

    @Autowired
    public ShipmentService(ShipmentRepository shipmentRepository, TrackingEventRepository trackingEventRepository) {
        this.shipmentRepository = shipmentRepository;
        this.trackingEventRepository = trackingEventRepository;
    }

    public ShipmentResponse createShipment(ShipmentCreateRequest request) {
        // Se já existe um envio para este orderId, retorna o existente
        var existing = shipmentRepository.findByOrderId(request.getOrderId());
        if (existing.isPresent()) {
            return toResponse(existing.get());
        }

        String trackingNumber = generateTrackingNumber();

        Shipment shipment = Shipment.builder()
                .orderId(request.getOrderId())
                .customerEmail(request.getCustomerEmail())
                .trackingNumber(trackingNumber)
                .carrier(request.getCarrier() != null ? request.getCarrier() : "LogBrasil Padrão")
                .serviceType(request.getServiceType() != null ? request.getServiceType() : "STANDARD")
                .freightCost(request.getFreightCost())
                .estimatedDeliveryDays(request.getEstimatedDeliveryDays() != null ? request.getEstimatedDeliveryDays() : 3)
                .destinationStreet(request.getStreet())
                .destinationCity(request.getCity())
                .destinationState(request.getState())
                .destinationZipCode(request.getZipCode())
                .status(ShipmentStatus.CREATED)
                .build();

        Shipment saved = shipmentRepository.save(shipment);

        // Cria o primeiro evento de rastreio
        TrackingEvent initialEvent = TrackingEvent.builder()
                .shipmentId(saved.getId())
                .trackingNumber(trackingNumber)
                .status(ShipmentStatus.CREATED)
                .message("Envio registrado e etiqueta emitida. Aguardando coleta pelo operador logístico.")
                .location("Centro de Distribuição Principal - CD Nexus Store (SP)")
                .timestamp(LocalDateTime.now())
                .build();

        trackingEventRepository.save(initialEvent);

        return toResponse(saved);
    }

    public ShipmentResponse updateShipmentStatus(String trackingNumber, ShipmentStatusUpdateRequest request) {
        Shipment shipment = shipmentRepository.findByTrackingNumber(trackingNumber)
                .orElseThrow(() -> new ResourceNotFoundException("Envio não encontrado para o código de rastreamento: " + trackingNumber));

        shipment.setStatus(request.getStatus());
        Shipment updated = shipmentRepository.save(shipment);

        TrackingEvent event = TrackingEvent.builder()
                .shipmentId(updated.getId())
                .trackingNumber(trackingNumber)
                .status(request.getStatus())
                .message(request.getMessage())
                .location(request.getLocation())
                .timestamp(LocalDateTime.now())
                .build();

        trackingEventRepository.save(event);

        return toResponse(updated);
    }

    @Transactional(readOnly = true)
    public ShipmentResponse getShipmentByTrackingNumber(String trackingNumber) {
        Shipment shipment = shipmentRepository.findByTrackingNumber(trackingNumber)
                .orElseThrow(() -> new ResourceNotFoundException("Envio não encontrado para o código de rastreamento: " + trackingNumber));

        return toResponse(shipment);
    }

    @Transactional(readOnly = true)
    public ShipmentResponse getShipmentByOrderId(Long orderId) {
        Shipment shipment = shipmentRepository.findByOrderId(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Nenhum envio associado ao Pedido #" + orderId));

        return toResponse(shipment);
    }

    @Transactional(readOnly = true)
    public List<ShipmentResponse> getAllShipments() {
        return shipmentRepository.findAllByOrderByCreatedAtDesc().stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<ShipmentResponse> getShipmentsByStatus(ShipmentStatus status) {
        return shipmentRepository.findByStatusOrderByCreatedAtDesc(status).stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    private ShipmentResponse toResponse(Shipment shipment) {
        List<TrackingEvent> events = trackingEventRepository.findByShipmentIdOrderByTimestampAsc(shipment.getId());
        List<TrackingEventDto> eventDtos = events.stream()
                .map(e -> TrackingEventDto.builder()
                        .id(e.getId())
                        .status(e.getStatus())
                        .statusDisplay(e.getStatus().getDisplayName())
                        .message(e.getMessage())
                        .location(e.getLocation())
                        .timestamp(e.getTimestamp())
                        .build())
                .collect(Collectors.toList());

        return ShipmentResponse.builder()
                .id(shipment.getId())
                .orderId(shipment.getOrderId())
                .customerEmail(shipment.getCustomerEmail())
                .trackingNumber(shipment.getTrackingNumber())
                .carrier(shipment.getCarrier())
                .serviceType(shipment.getServiceType())
                .freightCost(shipment.getFreightCost())
                .estimatedDeliveryDays(shipment.getEstimatedDeliveryDays())
                .destinationStreet(shipment.getDestinationStreet())
                .destinationCity(shipment.getDestinationCity())
                .destinationState(shipment.getDestinationState())
                .destinationZipCode(shipment.getDestinationZipCode())
                .status(shipment.getStatus())
                .statusDisplay(shipment.getStatus().getDisplayName())
                .createdAt(shipment.getCreatedAt())
                .lastModifiedAt(shipment.getLastModifiedAt())
                .events(eventDtos)
                .build();
    }

    private String generateTrackingNumber() {
        int code = 100000 + random.nextInt(900000);
        return "NX-" + code + "-BR";
    }
}
