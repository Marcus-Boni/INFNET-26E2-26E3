package com.infnet.tp5.infrastructure.rabbitmq;

import com.infnet.tp5.domain.model.Order;
import com.infnet.tp5.domain.model.OrderStatus;
import com.infnet.tp5.domain.repository.OrderRepository;
import com.infnet.tp5.event.ShipmentCreatedEvent;
import com.infnet.tp5.event.ShipmentStatusUpdatedEvent;
import com.infnet.tp5.infrastructure.config.RabbitMqConfig;
import com.infnet.tp5.service.AuditLogService;
import com.infnet.tp5.service.EventLogService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class ShipmentEventListener {

    private static final Logger log = LoggerFactory.getLogger(ShipmentEventListener.class);

    private final OrderRepository orderRepository;
    private final AuditLogService auditLogService;
    private final EventLogService eventLogService;

    @Autowired
    public ShipmentEventListener(OrderRepository orderRepository,
                                 AuditLogService auditLogService,
                                 EventLogService eventLogService) {
        this.orderRepository = orderRepository;
        this.auditLogService = auditLogService;
        this.eventLogService = eventLogService;
    }

    @RabbitListener(queues = RabbitMqConfig.QUEUE_ORDER_SHIPMENT_CREATED)
    @Transactional
    public void handleShipmentCreated(ShipmentCreatedEvent event) {
        log.info("[EDA] Consumindo ShipmentCreatedEvent do RabbitMQ: orderId={}, trackingNumber={}, correlationId={}",
                event.getOrderId(), event.getTrackingNumber(), event.getCorrelationId());

        try {
            Order order = orderRepository.findById(event.getOrderId()).orElse(null);
            if (order != null) {
                String oldTracking = order.getTrackingNumber();
                order.setTrackingNumber(event.getTrackingNumber());
                orderRepository.save(order);

                auditLogService.logChange(
                        "Order",
                        order.getId(),
                        "SHIPPING_PROVISIONED_VIA_RABBITMQ",
                        "Código de rastreamento emitido assincronamente pelo RabbitMQ: " + event.getTrackingNumber(),
                        oldTracking,
                        "Transportadora: " + event.getCarrier() + ", Status: " + event.getStatus() +
                                ", Correlação: " + event.getCorrelationId()
                );
            } else {
                log.warn("[EDA] Pedido #{} não encontrado ao processar ShipmentCreatedEvent", event.getOrderId());
            }

            eventLogService.logReceivedEvent(
                    event.getEventId(),
                    event.getCorrelationId(),
                    event.getEventType(),
                    RabbitMqConfig.EXCHANGE_SHIPPING,
                    RabbitMqConfig.ROUTING_KEY_SHIPPING_CREATED,
                    event,
                    "SUCCESS",
                    "Envio associado ao pedido #" + event.getOrderId() + " com rastreio " + event.getTrackingNumber()
            );
        } catch (Exception ex) {
            log.error("[EDA] Erro ao processar ShipmentCreatedEvent para pedido #{}: {}", event.getOrderId(), ex.getMessage(), ex);
            eventLogService.logReceivedEvent(
                    event.getEventId(),
                    event.getCorrelationId(),
                    event.getEventType(),
                    RabbitMqConfig.EXCHANGE_SHIPPING,
                    RabbitMqConfig.ROUTING_KEY_SHIPPING_CREATED,
                    event,
                    "FAILED",
                    "Falha no processamento: " + ex.getMessage()
            );
            throw ex; // Relança para acionar mecanismo de retry/DLQ do RabbitMQ
        }
    }

    @RabbitListener(queues = RabbitMqConfig.QUEUE_ORDER_SHIPMENT_STATUS_UPDATED)
    @Transactional
    public void handleShipmentStatusUpdated(ShipmentStatusUpdatedEvent event) {
        log.info("[EDA] Consumindo ShipmentStatusUpdatedEvent do RabbitMQ: trackingNumber={}, newStatus={}, correlationId={}",
                event.getTrackingNumber(), event.getNewStatus(), event.getCorrelationId());

        try {
            if (event.getOrderId() != null) {
                Order order = orderRepository.findById(event.getOrderId()).orElse(null);
                if (order != null) {
                    if ("DELIVERED".equalsIgnoreCase(event.getNewStatus())) {
                        order.setStatus(OrderStatus.DELIVERED);
                        orderRepository.save(order);
                        auditLogService.logChange(
                                "Order",
                                order.getId(),
                                "DELIVERED_NOTIFICATION",
                                "Notificação de entrega confirmada pela transportadora via RabbitMQ",
                                "SHIPPED",
                                "DELIVERED"
                        );
                    } else if ("CANCELLED".equalsIgnoreCase(event.getNewStatus()) && order.getStatus() != OrderStatus.CANCELLED) {
                        order.setStatus(OrderStatus.CANCELLED);
                        orderRepository.save(order);
                    }
                }
            }

            eventLogService.logReceivedEvent(
                    event.getEventId(),
                    event.getCorrelationId(),
                    event.getEventType(),
                    RabbitMqConfig.EXCHANGE_SHIPPING,
                    RabbitMqConfig.ROUTING_KEY_SHIPPING_STATUS_UPDATED,
                    event,
                    "SUCCESS",
                    "Status do envio " + event.getTrackingNumber() + " atualizado para " + event.getNewStatus()
            );
        } catch (Exception ex) {
            log.error("[EDA] Erro ao processar ShipmentStatusUpdatedEvent: {}", ex.getMessage(), ex);
            eventLogService.logReceivedEvent(
                    event.getEventId(),
                    event.getCorrelationId(),
                    event.getEventType(),
                    RabbitMqConfig.EXCHANGE_SHIPPING,
                    RabbitMqConfig.ROUTING_KEY_SHIPPING_STATUS_UPDATED,
                    event,
                    "FAILED",
                    "Falha no processamento: " + ex.getMessage()
            );
            throw ex;
        }
    }
}

