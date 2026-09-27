package com.infnet.shipping.infrastructure.rabbitmq;

import com.infnet.shipping.domain.model.ShipmentStatus;
import com.infnet.shipping.dto.ShipmentCreateRequest;
import com.infnet.shipping.dto.ShipmentResponse;
import com.infnet.shipping.dto.ShipmentStatusUpdateRequest;
import com.infnet.shipping.event.*;
import com.infnet.shipping.infrastructure.config.RabbitMqConfig;
import com.infnet.shipping.service.EventLogService;
import com.infnet.shipping.service.ShipmentService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Component
public class OrderEventListener {

    private static final Logger log = LoggerFactory.getLogger(OrderEventListener.class);

    private final ShipmentService shipmentService;
    private final ShipmentEventPublisher shipmentEventPublisher;
    private final EventLogService eventLogService;

    @Autowired
    public OrderEventListener(ShipmentService shipmentService,
                              ShipmentEventPublisher shipmentEventPublisher,
                              EventLogService eventLogService) {
        this.shipmentService = shipmentService;
        this.shipmentEventPublisher = shipmentEventPublisher;
        this.eventLogService = eventLogService;
    }

    @RabbitListener(queues = RabbitMqConfig.QUEUE_SHIPPING_ORDER_CREATED)
    @Transactional
    public void handleOrderCreated(OrderCreatedEvent event) {
        log.info("[EDA-SHIPPING] Consumindo OrderCreatedEvent do RabbitMQ: orderId={}, email={}, correlationId={}",
                event.getOrderId(), event.getCustomerEmail(), event.getCorrelationId());

        try {
            ShipmentCreateRequest request = ShipmentCreateRequest.builder()
                    .orderId(event.getOrderId())
                    .customerEmail(event.getCustomerEmail())
                    .carrier(event.getCarrier())
                    .serviceType(event.getServiceType())
                    .freightCost(event.getShippingCost())
                    .estimatedDeliveryDays(event.getEstimatedDeliveryDays())
                    .street(event.getStreet())
                    .city(event.getCity())
                    .state(event.getState())
                    .zipCode(event.getZipCode())
                    .build();

            ShipmentResponse shipment = shipmentService.createShipment(request);

            eventLogService.logReceivedEvent(
                    event.getEventId(),
                    event.getCorrelationId(),
                    event.getEventType(),
                    RabbitMqConfig.EXCHANGE_ORDER,
                    RabbitMqConfig.ROUTING_KEY_ORDER_CREATED,
                    event,
                    "SUCCESS",
                    "Envio criado com sucesso: trackingNumber=" + shipment.getTrackingNumber()
            );

            // Publica o evento de confirmação e sincronização de rastreamento de volta no RabbitMQ
            ShipmentCreatedEvent responseEvent = ShipmentCreatedEvent.builder()
                    .eventId(UUID.randomUUID().toString())
                    .correlationId(event.getCorrelationId())
                    .eventType("SHIPMENT_CREATED")
                    .timestamp(LocalDateTime.now())
                    .shipmentId(shipment.getId())
                    .orderId(shipment.getOrderId())
                    .trackingNumber(shipment.getTrackingNumber())
                    .carrier(shipment.getCarrier())
                    .status(shipment.getStatus().name())
                    .estimatedDeliveryDays(shipment.getEstimatedDeliveryDays())
                    .freightCost(shipment.getFreightCost())
                    .customerEmail(shipment.getCustomerEmail())
                    .build();

            shipmentEventPublisher.publishShipmentCreated(responseEvent);

        } catch (Exception ex) {
            log.error("[EDA-SHIPPING] Erro ao processar OrderCreatedEvent para pedido #{}: {}",
                    event.getOrderId(), ex.getMessage(), ex);
            eventLogService.logReceivedEvent(
                    event.getEventId(),
                    event.getCorrelationId(),
                    event.getEventType(),
                    RabbitMqConfig.EXCHANGE_ORDER,
                    RabbitMqConfig.ROUTING_KEY_ORDER_CREATED,
                    event,
                    "FAILED",
                    "Falha ao gerar envio: " + ex.getMessage()
            );
            throw ex;
        }
    }

    @RabbitListener(queues = RabbitMqConfig.QUEUE_SHIPPING_ORDER_DISPATCHED)
    @Transactional
    public void handleOrderDispatched(OrderDispatchedEvent event) {
        log.info("[EDA-SHIPPING] Consumindo OrderDispatchedEvent do RabbitMQ: orderId={}, trackingNumber={}",
                event.getOrderId(), event.getTrackingNumber());

        try {
            if (event.getTrackingNumber() != null && !event.getTrackingNumber().startsWith("AGUARDANDO")) {
                ShipmentStatusUpdateRequest updateReq = ShipmentStatusUpdateRequest.builder()
                        .status(ShipmentStatus.DISPATCHED)
                        .message(event.getMessage() != null ? event.getMessage() : "Pedido despachado da central")
                        .location(event.getLocation() != null ? event.getLocation() : "CD Nexus Store")
                        .build();

                shipmentService.updateShipmentStatus(event.getTrackingNumber(), updateReq);
            }

            eventLogService.logReceivedEvent(
                    event.getEventId(),
                    event.getCorrelationId(),
                    event.getEventType(),
                    RabbitMqConfig.EXCHANGE_ORDER,
                    RabbitMqConfig.ROUTING_KEY_ORDER_DISPATCHED,
                    event,
                    "SUCCESS",
                    "Envio marcado como DESPACHADO para pedido #" + event.getOrderId()
            );
        } catch (Exception ex) {
            log.error("[EDA-SHIPPING] Erro ao processar OrderDispatchedEvent: {}", ex.getMessage(), ex);
            eventLogService.logReceivedEvent(
                    event.getEventId(),
                    event.getCorrelationId(),
                    event.getEventType(),
                    RabbitMqConfig.EXCHANGE_ORDER,
                    RabbitMqConfig.ROUTING_KEY_ORDER_DISPATCHED,
                    event,
                    "FAILED",
                    "Falha: " + ex.getMessage()
            );
            throw ex;
        }
    }

    @RabbitListener(queues = RabbitMqConfig.QUEUE_SHIPPING_ORDER_CANCELLED)
    @Transactional
    public void handleOrderCancelled(OrderCancelledEvent event) {
        log.info("[EDA-SHIPPING] Consumindo OrderCancelledEvent do RabbitMQ: orderId={}, trackingNumber={}",
                event.getOrderId(), event.getTrackingNumber());

        try {
            if (event.getTrackingNumber() != null && !event.getTrackingNumber().startsWith("AGUARDANDO")) {
                ShipmentStatusUpdateRequest updateReq = ShipmentStatusUpdateRequest.builder()
                        .status(ShipmentStatus.CANCELLED)
                        .message("Envio cancelado. Motivo: " + event.getReason())
                        .location("Central de Distribuição")
                        .build();

                shipmentService.updateShipmentStatus(event.getTrackingNumber(), updateReq);
            }

            eventLogService.logReceivedEvent(
                    event.getEventId(),
                    event.getCorrelationId(),
                    event.getEventType(),
                    RabbitMqConfig.EXCHANGE_ORDER,
                    RabbitMqConfig.ROUTING_KEY_ORDER_CANCELLED,
                    event,
                    "SUCCESS",
                    "Envio cancelado para pedido #" + event.getOrderId()
            );
        } catch (Exception ex) {
            log.error("[EDA-SHIPPING] Erro ao processar OrderCancelledEvent: {}", ex.getMessage(), ex);
            eventLogService.logReceivedEvent(
                    event.getEventId(),
                    event.getCorrelationId(),
                    event.getEventType(),
                    RabbitMqConfig.EXCHANGE_ORDER,
                    RabbitMqConfig.ROUTING_KEY_ORDER_CANCELLED,
                    event,
                    "FAILED",
                    "Falha: " + ex.getMessage()
            );
            throw ex;
        }
    }
}
