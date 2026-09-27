package com.infnet.tp4.infrastructure.rabbitmq;

import com.infnet.tp4.event.OrderCancelledEvent;
import com.infnet.tp4.event.OrderCreatedEvent;
import com.infnet.tp4.event.OrderDispatchedEvent;
import com.infnet.tp4.infrastructure.config.RabbitMqConfig;
import com.infnet.tp4.service.EventLogService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class OrderEventPublisher {

    private static final Logger log = LoggerFactory.getLogger(OrderEventPublisher.class);

    private final RabbitTemplate rabbitTemplate;
    private final EventLogService eventLogService;

    @Autowired
    public OrderEventPublisher(RabbitTemplate rabbitTemplate, EventLogService eventLogService) {
        this.rabbitTemplate = rabbitTemplate;
        this.eventLogService = eventLogService;
    }

    public void publishOrderCreated(OrderCreatedEvent event) {
        if (event.getEventId() == null) {
            event.setEventId(UUID.randomUUID().toString());
        }
        if (event.getCorrelationId() == null) {
            event.setCorrelationId("corr-" + UUID.randomUUID().toString().substring(0, 8));
        }

        try {
            rabbitTemplate.convertAndSend(
                    RabbitMqConfig.EXCHANGE_ORDER,
                    RabbitMqConfig.ROUTING_KEY_ORDER_CREATED,
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
                    RabbitMqConfig.EXCHANGE_ORDER,
                    RabbitMqConfig.ROUTING_KEY_ORDER_CREATED,
                    event,
                    "SUCCESS",
                    "Pedido #" + event.getOrderId() + " emitido para processamento assíncrono de logística"
            );
            log.info("[EDA] OrderCreatedEvent emitido para o pedido #{} com correlationId={}",
                    event.getOrderId(), event.getCorrelationId());
        } catch (Exception ex) {
            log.error("[EDA] Falha ao publicar OrderCreatedEvent no RabbitMQ para pedido #{}: {}",
                    event.getOrderId(), ex.getMessage());
            eventLogService.logPublishedEvent(
                    event.getEventId(),
                    event.getCorrelationId(),
                    event.getEventType(),
                    RabbitMqConfig.EXCHANGE_ORDER,
                    RabbitMqConfig.ROUTING_KEY_ORDER_CREATED,
                    event,
                    "FAILED",
                    "Falha ao conectar com RabbitMQ: " + ex.getMessage()
            );
        }
    }

    public void publishOrderDispatched(OrderDispatchedEvent event) {
        if (event.getEventId() == null) {
            event.setEventId(UUID.randomUUID().toString());
        }
        if (event.getCorrelationId() == null) {
            event.setCorrelationId("corr-" + UUID.randomUUID().toString().substring(0, 8));
        }

        try {
            rabbitTemplate.convertAndSend(
                    RabbitMqConfig.EXCHANGE_ORDER,
                    RabbitMqConfig.ROUTING_KEY_ORDER_DISPATCHED,
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
                    RabbitMqConfig.EXCHANGE_ORDER,
                    RabbitMqConfig.ROUTING_KEY_ORDER_DISPATCHED,
                    event,
                    "SUCCESS",
                    "Despacho do Pedido #" + event.getOrderId() + " emitido via RabbitMQ"
            );
            log.info("[EDA] OrderDispatchedEvent emitido para pedido #{}", event.getOrderId());
        } catch (Exception ex) {
            log.error("[EDA] Falha ao publicar OrderDispatchedEvent no RabbitMQ: {}", ex.getMessage());
            eventLogService.logPublishedEvent(
                    event.getEventId(),
                    event.getCorrelationId(),
                    event.getEventType(),
                    RabbitMqConfig.EXCHANGE_ORDER,
                    RabbitMqConfig.ROUTING_KEY_ORDER_DISPATCHED,
                    event,
                    "FAILED",
                    "Erro ao publicar: " + ex.getMessage()
            );
        }
    }

    public void publishOrderCancelled(OrderCancelledEvent event) {
        if (event.getEventId() == null) {
            event.setEventId(UUID.randomUUID().toString());
        }
        if (event.getCorrelationId() == null) {
            event.setCorrelationId("corr-" + UUID.randomUUID().toString().substring(0, 8));
        }

        try {
            rabbitTemplate.convertAndSend(
                    RabbitMqConfig.EXCHANGE_ORDER,
                    RabbitMqConfig.ROUTING_KEY_ORDER_CANCELLED,
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
                    RabbitMqConfig.EXCHANGE_ORDER,
                    RabbitMqConfig.ROUTING_KEY_ORDER_CANCELLED,
                    event,
                    "SUCCESS",
                    "Cancelamento do Pedido #" + event.getOrderId() + " emitido via RabbitMQ"
            );
            log.info("[EDA] OrderCancelledEvent emitido para pedido #{}", event.getOrderId());
        } catch (Exception ex) {
            log.error("[EDA] Falha ao publicar OrderCancelledEvent no RabbitMQ: {}", ex.getMessage());
            eventLogService.logPublishedEvent(
                    event.getEventId(),
                    event.getCorrelationId(),
                    event.getEventType(),
                    RabbitMqConfig.EXCHANGE_ORDER,
                    RabbitMqConfig.ROUTING_KEY_ORDER_CANCELLED,
                    event,
                    "FAILED",
                    "Erro ao publicar: " + ex.getMessage()
            );
        }
    }
}
