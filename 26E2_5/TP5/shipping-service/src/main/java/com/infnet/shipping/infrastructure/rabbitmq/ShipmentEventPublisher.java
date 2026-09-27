package com.infnet.shipping.infrastructure.rabbitmq;

import com.infnet.shipping.event.ShipmentCreatedEvent;
import com.infnet.shipping.event.ShipmentStatusUpdatedEvent;
import com.infnet.shipping.infrastructure.config.RabbitMqConfig;
import com.infnet.shipping.service.EventLogService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class ShipmentEventPublisher {

    private static final Logger log = LoggerFactory.getLogger(ShipmentEventPublisher.class);

    private final RabbitTemplate rabbitTemplate;
    private final EventLogService eventLogService;

    @Autowired
    public ShipmentEventPublisher(RabbitTemplate rabbitTemplate, EventLogService eventLogService) {
        this.rabbitTemplate = rabbitTemplate;
        this.eventLogService = eventLogService;
    }

    public void publishShipmentCreated(ShipmentCreatedEvent event) {
        if (event.getEventId() == null) {
            event.setEventId(UUID.randomUUID().toString());
        }
        if (event.getCorrelationId() == null) {
            event.setCorrelationId("corr-ship-" + UUID.randomUUID().toString().substring(0, 8));
        }

        try {
            rabbitTemplate.convertAndSend(
                    RabbitMqConfig.EXCHANGE_SHIPPING,
                    RabbitMqConfig.ROUTING_KEY_SHIPPING_CREATED,
                    event,
                    m -> {
                        m.getMessageProperties().setCorrelationId(event.getCorrelationId());
                        m.getMessageProperties().setMessageId(event.getEventId());
                        m.getMessageProperties().setHeader("eventType", event.getEventType());
                        return m;
                    }
            );

            eventLogService.logPublishedEvent(
                    event.getEventId(),
                    event.getCorrelationId(),
                    event.getEventType(),
                    RabbitMqConfig.EXCHANGE_SHIPPING,
                    RabbitMqConfig.ROUTING_KEY_SHIPPING_CREATED,
                    event,
                    "SUCCESS",
                    "Envio criado para pedido #" + event.getOrderId() + " com rastreio " + event.getTrackingNumber()
            );
            log.info("[EDA-SHIPPING] ShipmentCreatedEvent publicado para pedido #{}, rastreio {}",
                    event.getOrderId(), event.getTrackingNumber());
        } catch (Exception ex) {
            log.error("[EDA-SHIPPING] Falha ao publicar ShipmentCreatedEvent: {}", ex.getMessage());
            eventLogService.logPublishedEvent(
                    event.getEventId(),
                    event.getCorrelationId(),
                    event.getEventType(),
                    RabbitMqConfig.EXCHANGE_SHIPPING,
                    RabbitMqConfig.ROUTING_KEY_SHIPPING_CREATED,
                    event,
                    "FAILED",
                    "Erro ao publicar: " + ex.getMessage()
            );
        }
    }

    public void publishShipmentStatusUpdated(ShipmentStatusUpdatedEvent event) {
        if (event.getEventId() == null) {
            event.setEventId(UUID.randomUUID().toString());
        }
        if (event.getCorrelationId() == null) {
            event.setCorrelationId("corr-status-" + UUID.randomUUID().toString().substring(0, 8));
        }

        try {
            rabbitTemplate.convertAndSend(
                    RabbitMqConfig.EXCHANGE_SHIPPING,
                    RabbitMqConfig.ROUTING_KEY_SHIPPING_STATUS_UPDATED,
                    event,
                    m -> {
                        m.getMessageProperties().setCorrelationId(event.getCorrelationId());
                        m.getMessageProperties().setMessageId(event.getEventId());
                        m.getMessageProperties().setHeader("eventType", event.getEventType());
                        return m;
                    }
            );

            eventLogService.logPublishedEvent(
                    event.getEventId(),
                    event.getCorrelationId(),
                    event.getEventType(),
                    RabbitMqConfig.EXCHANGE_SHIPPING,
                    RabbitMqConfig.ROUTING_KEY_SHIPPING_STATUS_UPDATED,
                    event,
                    "SUCCESS",
                    "Status do envio " + event.getTrackingNumber() + " alterado para " + event.getNewStatus()
            );
            log.info("[EDA-SHIPPING] ShipmentStatusUpdatedEvent publicado para rastreio {}, novoStatus: {}",
                    event.getTrackingNumber(), event.getNewStatus());
        } catch (Exception ex) {
            log.error("[EDA-SHIPPING] Falha ao publicar ShipmentStatusUpdatedEvent: {}", ex.getMessage());
            eventLogService.logPublishedEvent(
                    event.getEventId(),
                    event.getCorrelationId(),
                    event.getEventType(),
                    RabbitMqConfig.EXCHANGE_SHIPPING,
                    RabbitMqConfig.ROUTING_KEY_SHIPPING_STATUS_UPDATED,
                    event,
                    "FAILED",
                    "Erro ao publicar: " + ex.getMessage()
            );
        }
    }
}
